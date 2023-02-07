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
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.*
import java.io.*


internal class ProjectApplier(private val project: Project) {
    private lateinit var projectExtension: KoverProjectExtensionImpl
    private lateinit var androidExtension: KoverAndroidExtensionImpl
    private lateinit var defaultReportExtension: KoverReportExtensionImpl

    fun onApply() {
        project.configurations.create(DEPENDENCY_CONFIGURATION_NAME) {
            asBucket()
        }

        projectExtension = project.extensions.create(PROJECT_SETUP_EXTENSION_NAME, project.objects)
        androidExtension = project.extensions.create(ANDROID_REPORTS_EXTENSION_NAME, project.objects)

        defaultReportExtension = project.extensions.create(COMMON_REPORTS_EXTENSION_NAME)
    }

    fun onAfterEvaluate() {
        val tool = CoverageToolFactory.get(projectExtension)

        val agentConfiguration = project.configurations.create(JVM_AGENT_CONFIGURATION_NAME) {
            asTransitiveDependencies()
        }
        project.dependencies.add(JVM_AGENT_CONFIGURATION_NAME, tool.jvmAgentDependency)

        val reporterConfig = project.configurations.create(JVM_REPORTER_CONFIGURATION_NAME) {
            asTransitiveDependencies()
        }
        tool.jvmReporterDependencies.forEach {
            project.dependencies.add(JVM_REPORTER_CONFIGURATION_NAME, it)
        }

        /*
        Uses lazy jar search for the agent, because an eager search will cause a resolution at the configuration stage,
        which may affect performance.
        See https://github.com/Kotlin/kotlinx-kover/issues/235
         */
        val findJarTask = project.tasks.register<KoverAgentJarTask>(FIND_JAR_TASK, tool)
        findJarTask.configure {
            dependsOn(agentConfiguration)

            agentJarPath.set(project.layout.buildDirectory.file(agentLinkFilePath()))
            agentClasspath.from(agentConfiguration)
        }

        val agentJarPath = findJarTask.map<File?> {
            val linkFile = it.agentJarPath.get().asFile
            if (linkFile.exists()) {
                File(linkFile.readText())
            } else {
                // TODO for configuration caches support, because provider may be invoked in time of invoke tree analyzing
                File("dummy")
            }
        }

        val instrumentationExcludedClasses = projectExtension.instrumentation.classes

        val locator = SetupLocatorFactory.get(project)

        val setups = locator.locateMultiple(projectExtension)

        checkAndroidReports(locator, setups)

        setups.forEach {
            it.tests.configureEach {
                JvmTestTaskApplier(this, findJarTask, agentJarPath, tool, instrumentationExcludedClasses).apply()
            }

            val artifactGenTask = project.createSetupConfiguration(it, locator.kotlinPlugin, tool)

            val androidReportExtension = if (locator.kotlinPlugin.type == KotlinPluginType.ANDROID) {
                androidExtension.reports[it.id.name]
            } else {
                null
            }

            ReportsApplier(project, tool, artifactGenTask, reporterConfig, it.id)
                .createReports(defaultReportExtension, androidReportExtension)
        }
    }

    private fun Project.createSetupConfiguration(
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

    /**
     * Checking Android report configuration errors in case build variant not found, or the Android plugin is not applied.
     */
    private fun checkAndroidReports(locator: SetupLocator, setups: List<KoverSetup<*>>) {
        if (locator.kotlinPlugin.type != KotlinPluginType.ANDROID && androidExtension.reports.isNotEmpty()) {
            throw KoverIllegalConfigException("It is unacceptable to configure Kover Android reports, they can only be configured if Android plugin is applied")
        }

        val buildVariantNames = setups.filter { !it.id.isDefault }.map { it.id.name }.toSet()
        val configuredNames = androidExtension.reports.map { it.key }.toSet()
        val unknownVariantNames = configuredNames.subtract(buildVariantNames)
        if (unknownVariantNames.isNotEmpty()) {
            throw KoverIllegalConfigException("Error in configuring Kover Android reports: build variants are not present in the project $unknownVariantNames")
        }
    }
}
