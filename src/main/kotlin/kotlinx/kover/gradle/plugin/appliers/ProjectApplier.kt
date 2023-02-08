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
import java.io.*


internal class ProjectApplier(private val project: Project) {
    private lateinit var projectExtension: KoverProjectExtensionImpl
    private lateinit var androidExtension: KoverAndroidExtensionImpl
    private lateinit var simpleReportExtension: KoverReportExtensionImpl

    fun onApply() {
        project.configurations.create(DEPENDENCY_CONFIGURATION_NAME) {
            asBucket()
        }

        projectExtension = project.extensions.create(PROJECT_SETUP_EXTENSION_NAME, project.objects)
        androidExtension = project.extensions.create(ANDROID_EXTENSION_NAME, project.objects)

        simpleReportExtension = project.extensions.create(SIMPLE_REPORTS_EXTENSION_NAME)
    }

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

        val kotlinPluginType = locator.kotlinPlugin.type
        if (kotlinPluginType == KotlinPluginType.ANDROID) {
            androidProject(locator, instrData, reporterClasspath)
        } else {
            regularProject(locator, instrData, reporterClasspath)
        }
    }

    private fun collectInstrData(tool: CoverageTool, agentClasspath: Configuration): InstrumentationData {
        /*
        * Uses lazy jar search for the agent, because an eager search will cause a resolution at the configuration stage,
        * which may affect performance.
        * See https://github.com/Kotlin/kotlinx-kover/issues/235
        */
        val findAgentJarTask = project.tasks.register<KoverAgentJarTask>(FIND_JAR_TASK, tool)
        findAgentJarTask.configure {
            dependsOn(agentClasspath)

            this.agentJarPath.set(project.layout.buildDirectory.file(agentLinkFilePath()))
            this.agentClasspath.from(agentClasspath)
        }

        val agentJar = findAgentJarTask.map<File?> {
            val linkFile = it.agentJarPath.get().asFile
            if (linkFile.exists()) {
                File(linkFile.readText())
            } else {
                // TODO for configuration caches support, because provider may be invoked in time of invoke tree analyzing
                File("dummy")
            }
        }

        return InstrumentationData(
            findAgentJarTask,
            agentJar,
            tool,
            projectExtension.instrumentation.classes
        )
    }

    private fun regularProject(
        locator: SetupLocator,
        instrData: InstrumentationData,
        reporterClasspath: Configuration
    ) {
        if (androidExtension.reports.isNotEmpty()) {
            throw KoverIllegalConfigException("It is unacceptable to configure Kover Android reports, they can only be configured if Android plugin is applied")
        }

        val setup = locator.locateSingle(projectExtension)
        setup.configureTests(instrData)
        val artifactGenTask = project.createSetupArtifactGenerator(setup, locator.kotlinPlugin, instrData.tool)
        ReportsApplier(project, instrData.tool, artifactGenTask, reporterClasspath, setup.id)
            .createReports(simpleReportExtension)
    }

    private fun androidProject(
        locator: SetupLocator,
        instrData: InstrumentationData,
        reporterClasspath: Configuration
    ) {
        val setups = locator.locateMultiple(projectExtension)

        // Checking Android report configuration errors in case build variant not found, or the Android plugin is not applied.
        val buildVariantNames = setups.filter { !it.id.isDefault }.map { it.id.name }.toSet()
        val configuredNames = androidExtension.reports.map { it.key }.toSet()
        val unknownVariantNames = configuredNames.subtract(buildVariantNames)
        if (unknownVariantNames.isNotEmpty()) {
            throw KoverIllegalConfigException("Error in configuring Kover Android reports: build variants are not present in the project $unknownVariantNames")
        }

        val common = androidExtension.common
        setups.forEach { setup ->
            setup.configureTests(instrData)
            val artifactGenTask = project.createSetupArtifactGenerator(setup, locator.kotlinPlugin, instrData.tool)

            val androidReportExtension = androidExtension.reports[setup.id.name]
            ReportsApplier(project, instrData.tool, artifactGenTask, reporterClasspath, setup.id)
                .createReports(androidReportExtension, common)
        }
    }

    private fun KoverSetup<*>.configureTests(data: InstrumentationData) {
        tests.configureEach {
            JvmTestTaskApplier(this, data).apply()
        }
    }

    private fun Project.createSetupArtifactGenerator(
        setup: KoverSetup<*>,
        kotlinPlugin: AppliedKotlinPlugin,
        tool: CoverageTool
    ): Provider<KoverArtifactGenerationTask> {
        val artifactGenTask = tasks.register<KoverArtifactGenerationTask>(setupGenerationTask(setup.id)) {
            val tests = setup.tests
            val reportFiles = project.layout.buildDirectory.dir(rawReportsRootPath())
                .map { dir -> tests.map { dir.file(rawReportName(it.name, tool.variant.vendor)) } }

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
                builtBy(artifactGenTask)
            }
        }

        return artifactGenTask
    }
}

internal class InstrumentationData(
    val findAgentJarTask: TaskProvider<KoverAgentJarTask>,
    val agentJar: Provider<File>,
    val tool: CoverageTool,
    val excludedClasses: Set<String>
)
