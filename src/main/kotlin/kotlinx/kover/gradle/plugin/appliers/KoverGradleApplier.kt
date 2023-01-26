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
import kotlinx.kover.gradle.plugin.util.*
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.*
import java.io.*


internal class KoverGradleApplier(private val project: Project) {
    private lateinit var projectExtension: KoverProjectExtensionImpl
    private lateinit var defaultReportExtension: KoverReportExtensionImpl

    fun onApply() {
        project.configurations.create(DEPENDENCY_CONFIGURATION_NAME) {
            asBucket()
        }

        projectExtension = project.extensions.create<KoverProjectExtensionImpl>(
            PROJECT_SETUP_EXTENSION,
            project.objects
        ).apply {
            toolVariant = KoverToolDefaultVariant
        }

        defaultReportExtension = createReportExtension(SetupId.Default)
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

        locator.locate(projectExtension).forEach {
            it.tests.configureEach {
                JvmTestTaskApplier(this, findJarTask, agentJarPath, tool, instrumentationExcludedClasses).apply()
            }
            val collectTask = project.addArtifactGenerationTask(it, tool)
            project.addSetupConfiguration(it, locator.kotlinPlugin, collectTask)

            val customReportExtension = it.id.isDefault.ifFalse { createReportExtension(it.id) }
            ReportsApplier(project, tool, collectTask, reporterConfig, it.id)
                .createReports(defaultReportExtension, customReportExtension)
        }
    }

    private fun Project.addArtifactGenerationTask(
        setup: KoverSetup<*>,
        tool: CoverageTool
    ): Provider<KoverArtifactGenerationTask> {
        return tasks.register<KoverArtifactGenerationTask>(setupGenerationTask(setup.id)) {
            val tests = setup.tests
            val reportFiles = project.layout.buildDirectory.dir(rawReportsRootPath())
                .map { dir -> tests.map { dir.file(rawReportName(it.name, tool.variant.vendor)) } }

            dependsOn(tests)
            dependsOn(setup.build.map { it.compileTasks })

            sources.from(setup.build.map { it.sources })
            outputs.from(setup.build.map { it.outputs })
            reports.from(reportFiles)
            artifactFile.set(project.layout.buildDirectory.file(setupArtifactFile(setup.id)))
        }
    }

    private fun Project.addSetupConfiguration(
        setup: KoverSetup<*>,
        kotlinPlugin: AppliedKotlinPlugin,
        collectTask: Provider<KoverArtifactGenerationTask>
    ) {
        configurations.create(setupConfigurationName(setup.id)) {
            asProducer()
            attributes {
                setupName(setup.id.name, project.objects)
                kotlinType(kotlinPlugin, project.objects)
                projectPath(project.path, project.objects)
            }
            outgoing.artifact(collectTask.flatMap { it.artifactFile }) {
                builtBy(collectTask)
            }
        }
    }

    private fun createReportExtension(setupId: SetupId): KoverReportExtensionImpl {
        val extension = project.extensions.create<KoverReportExtensionImpl>(reportExtensionName(setupId))

        extension.commonFilters = null

        val buildDir = project.layout.buildDirectory

        extension.html.reportDir.convention(buildDir.dir(htmlReportPath(setupId)).map { it.asFile })
        extension.html.onCheck = false

        extension.xml.reportFile.convention(buildDir.file(xmlReportPath(setupId)).map { it.asFile })
        extension.xml.onCheck = false

        extension.verify.onCheck = setupId.isDefault

        return extension
    }
}
