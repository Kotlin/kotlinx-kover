/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.dsl.internal.*
import kotlinx.kover.gradle.plugin.locators.*
import kotlinx.kover.gradle.plugin.tasks.internal.KoverAgentJarTask
import kotlinx.kover.gradle.plugin.tasks.internal.KoverArtifactGenerationTask
import kotlinx.kover.gradle.plugin.tools.*
import kotlinx.kover.gradle.plugin.tools.CoverageToolFactory
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.*

/**
 * Main Gradle Plugin applier of the project.
 */
internal class ProjectApplier(private val project: Project) {
    private lateinit var projectExtension: KoverProjectExtensionImpl
    private lateinit var androidExtension: KoverAndroidExtensionImpl
    private lateinit var regularReportExtension: KoverReportExtensionImpl

    /**
     * The code executed right at the moment of applying of the plugin.
     */
    fun onApply() {
        project.configurations.create(DEPENDENCY_CONFIGURATION_NAME) {
            asBucket()
        }

        projectExtension = project.extensions.create(PROJECT_EXTENSION_NAME, project.objects)
        regularReportExtension = project.extensions.create(REGULAR_REPORT_EXTENSION_NAME)
        androidExtension = project.extensions.create(ANDROID_EXTENSION_NAME, project.objects)
    }

    /**
     * The code executed after processing of all user's configs from build.gradle[.kts] file.
     */
    fun onAfterEvaluate() {
        val tool = CoverageToolFactory.get(projectExtension)

        val agentClasspath = project.configurations.create(JVM_AGENT_CONFIGURATION_NAME) {
            asTransitiveDependencies()
        }
        project.dependencies.add(JVM_AGENT_CONFIGURATION_NAME, tool.jvmAgentDependency)

        val reporterClasspath = project.configurations.create(JVM_REPORTER_CONFIGURATION_NAME) {
            asTransitiveDependencies()
        }
        tool.jvmReporterDependencies.forEach {
            project.dependencies.add(JVM_REPORTER_CONFIGURATION_NAME, it)
        }

        val instrData = collectInstrData(tool, agentClasspath)
        val locator = SetupLocatorFactory.get(project)

        if (locator.kotlinPlugin.type == KotlinPluginType.ANDROID) {
            androidProject(locator, instrData, reporterClasspath)
        } else {
            regularProject(locator, instrData, reporterClasspath)
        }
    }

    /**
     * Collect all configured data, required for online instrumentation.
     */
    private fun collectInstrData(tool: CoverageTool, agentClasspath: Configuration): InstrumentationData {
        /*
        * Uses lazy jar search for the agent, because an eager search will cause a resolution at the configuration stage,
        * which may affect performance.
        * See https://github.com/Kotlin/kotlinx-kover/issues/235
        */
        val findAgentJarTask = project.tasks.register<KoverAgentJarTask>(FIND_JAR_TASK, tool)
        findAgentJarTask.configure {
            // depends on agent classpath to resolve it in execute-time
            dependsOn(agentClasspath)

            this.agentJar.set(project.layout.buildDirectory.file(agentFilePath(tool.variant)))
            this.agentClasspath.from(agentClasspath)
        }

        return InstrumentationData(
            findAgentJarTask,
            tool,
            projectExtension.instrumentation.classes
        )
    }

    /**
     * Configure Kotlin JVM or Kotlin multi-platform project.
     */
    private fun regularProject(
        locator: SetupLocator,
        instrData: InstrumentationData,
        reporterClasspath: Configuration
    ) {
        if (androidExtension.configured) {
            throw KoverIllegalConfigException("It is unacceptable to configure Kover Android reports, they can only be configured if Android plugin is applied")
        }
        if (locator.kotlinPlugin.type != KotlinPluginType.JVM && projectExtension.sources.jvm.sourceSets.isNotEmpty()) {
            throw KoverIllegalConfigException("It is acceptable to add Kover JVM source sets exclusion only if kotlin JVM plugin is applied")
        }
        if (locator.kotlinPlugin.type != KotlinPluginType.MULTI_PLATFORM && projectExtension.sources.kmp.configured) {
            throw KoverIllegalConfigException("It is acceptable to add Kover KMP source sets exclusion only if kotlin multiplatform plugin is applied")
        }

        val setup = locator.locateRegular(projectExtension)
        setup.configureTests(instrData)
        val artifactGenTask = project.createSetupArtifactGenerator(setup, locator.kotlinPlugin, instrData.tool)
        ReportsApplier(project, instrData.tool, artifactGenTask, reporterClasspath, setup.id)
            .createReports(regularReportExtension)
    }

    /**
     * Configure Android project.
     */
    private fun androidProject(
        locator: SetupLocator,
        instrData: InstrumentationData,
        reporterClasspath: Configuration
    ) {
        val setups = locator.locateAll(projectExtension)

        // Checking Android report configuration errors in case build variant not found, or the Android plugin is not applied.
        val buildVariantNames = setups.map { it.id.name }.toSet()
        val customNames = androidExtension.reports.map { it.key }.toSet()
        val unknownVariantNames = customNames.subtract(buildVariantNames)
        if (unknownVariantNames.isNotEmpty()) {
            throw KoverIllegalConfigException("Error in configuring Kover Android reports: build variants are not present in the project $unknownVariantNames")
        }

        // Check regular report was configured in Android
        if (regularReportExtension.configured) {
            throw KoverIllegalConfigException("Error in configuring Kover: it is not allowed to configure regular report ('$REGULAR_REPORT_EXTENSION_NAME { }' extension) in Android application")
        }

        val general = androidExtension.common
        setups.forEach { setup ->
            setup.configureTests(instrData)
            val artifactGenTask = project.createSetupArtifactGenerator(setup, locator.kotlinPlugin, instrData.tool)

            val androidReportExtension = androidExtension.reports[setup.id.name]
            ReportsApplier(project, instrData.tool, artifactGenTask, reporterClasspath, setup.id)
                .createReports(androidReportExtension, general)
        }
    }

    /**
     * Add online instrumentation to all JVM test tasks of Kover setup.
     */
    private fun KoverSetup<*>.configureTests(data: InstrumentationData) {
        tests.configureEach {
            JvmTestTaskApplier(this, data).apply()
        }
    }

    /**
     * Create a task to generate an artifact and a configuration in which this output artifact will be placed.
     */
    private fun Project.createSetupArtifactGenerator(
        setup: KoverSetup<*>,
        kotlinPlugin: AppliedKotlinPlugin,
        tool: CoverageTool
    ): Provider<KoverArtifactGenerationTask> {
        val artifactGenTask = tasks.register<KoverArtifactGenerationTask>(setupGenerationTask(setup.id)) {
            val tests = setup.tests
            val reportFiles = project.layout.buildDirectory.dir(rawReportsRootPath())
                .map { dir -> tests.map { dir.file(rawReportName(it.name, tool.variant.vendor)) } }

            // to generate an artifact, need to compile the entire project and perform all test tasks
            dependsOn(tests)
            dependsOn(setup.lazyInfo.map { it.compileTasks })

            sources.from(setup.lazyInfo.map { it.sources })
            outputs.from(setup.lazyInfo.map { it.outputs })
            reports.from(reportFiles)
            artifactFile.set(project.layout.buildDirectory.file(setupArtifactFile(setup.id)))
        }

        configurations.register(setupConfigurationName(setup.id)) {
            asProducer()
            attributes {
                setupName(setup.id.name, project.objects)
                kotlinType(kotlinPlugin, project.objects)
                projectPath(project.path, project.objects)
            }
            outgoing.artifact(artifactGenTask.flatMap { it.artifactFile }) {
                // Before resolving this configuration, it is necessary to execute the task of generating an artifact
                builtBy(artifactGenTask)
            }
        }

        return artifactGenTask
    }
}

/**
 * All configured data used in online instrumentation.
 */
internal class InstrumentationData(
    val findAgentJarTask: TaskProvider<KoverAgentJarTask>,
    val tool: CoverageTool,
    val excludedClasses: Set<String>
)
