/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import kotlinx.kover.gradle.plugin.appliers.reports.AndroidVariantApplier
import kotlinx.kover.gradle.plugin.appliers.reports.DefaultVariantApplier
import kotlinx.kover.gradle.plugin.appliers.reports.androidReports
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.KoverNames.DEPENDENCY_CONFIGURATION_NAME
import kotlinx.kover.gradle.plugin.dsl.KoverNames.PROJECT_EXTENSION_NAME
import kotlinx.kover.gradle.plugin.dsl.KoverNames.REPORT_EXTENSION_NAME
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import kotlinx.kover.gradle.plugin.dsl.internal.KoverReportExtensionImpl
import kotlinx.kover.gradle.plugin.locators.CompilationsListener
import kotlinx.kover.gradle.plugin.locators.CompilationsListenerManager
import kotlinx.kover.gradle.plugin.tasks.services.KoverAgentJarTask
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import kotlinx.kover.gradle.plugin.tools.CoverageToolFactory
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

/**
 * Main Gradle Plugin applier of the project.
 */
internal class ProjectApplier(private val project: Project) {
    private lateinit var projectExtension: KoverProjectExtensionImpl
    private lateinit var reportExtension: KoverReportExtensionImpl

    private val androidAppliers: MutableMap<String, AndroidVariantApplier> = mutableMapOf()

    /**
     * The code executed right at the moment of applying of the plugin.
     */
    fun onApply() {
        val koverDependencies = project.configurations.create(DEPENDENCY_CONFIGURATION_NAME) {
            asBucket()
        }

        projectExtension = project.extensions.create(PROJECT_EXTENSION_NAME)
        reportExtension = project.extensions.create(REPORT_EXTENSION_NAME)

        val toolProvider = CoverageToolFactory.get(project, projectExtension)

        // DEPS
        val agentClasspath = project.configurations.create(JVM_AGENT_CONFIGURATION_NAME) {
            asTransitiveDependencies()
        }
        project.dependencies.add(JVM_AGENT_CONFIGURATION_NAME, toolProvider.map { tool -> tool.jvmAgentDependency })

        val reporterClasspath = project.configurations.create(JVM_REPORTER_CONFIGURATION_NAME) {
            asTransitiveDependencies()
        }
        project.dependencies.add(
            JVM_REPORTER_CONFIGURATION_NAME,
            toolProvider.map { tool -> tool.jvmReporterDependency })
        project.dependencies.add(
            JVM_REPORTER_CONFIGURATION_NAME,
            toolProvider.map { tool -> tool.jvmReporterExtraDependency })

        val defaultApplier = DefaultVariantApplier(
            project,
            koverDependencies,
            reporterClasspath,
            toolProvider
        )

        val instrData = collectInstrumentationData(toolProvider, agentClasspath)

        val listener = object : CompilationsListener {
            override fun onJvmCompilation(kit: JvmCompilationKit) {
                kit.tests.instrument(instrData)
                defaultApplier.applyCompilationKit(kit)
            }

            override fun onAndroidCompilations(kits: List<AndroidVariantCompilationKit>) {
                kits.forEach { kit ->
                    kit.tests.instrument(instrData)
                    val applier =
                        AndroidVariantApplier(project, kit.buildVariant, koverDependencies, reporterClasspath, toolProvider)

                    val configs =
                        reportExtension.namedReports[kit.buildVariant] ?: project.androidReports(kit.buildVariant)

                    applier.applyConfig(configs, reportExtension.filters)
                    applier.applyCompilationKit(kit)

                    androidAppliers[kit.buildVariant] = applier
                }
            }

            override fun onFinalize() {
                reportExtension.namedReports.keys.forEach { variantName ->
                    if (variantName !in androidAppliers) {
                        throw KoverIllegalConfigException("Build variant '$variantName' not found in project '${project.path}' - impossible to configure Android reports for it.\nAvailable variations: ${androidAppliers.keys}")
                    }
                }

                reportExtension.default.merged.forEach { variantName ->
                    val applier = androidAppliers[variantName] ?: throw KoverIllegalConfigException("Build variant '$variantName' not found in project '${project.path}' - impossible to merge default reports with its measurements.\n" +
                            "Available variations: ${androidAppliers.keys}")
                    defaultApplier.mergeWith(applier)
                }

                defaultApplier.applyConfig(reportExtension.default, reportExtension.filters)
            }
        }

        CompilationsListenerManager.locate(project, projectExtension, listener)
    }

    /**
     * Collect all configured data, required for online instrumentation.
     */
    private fun collectInstrumentationData(
        toolProvider: Provider<CoverageTool>,
        agentClasspath: Configuration
    ): InstrumentationData {
        /*
        * Uses lazy jar search for the agent, because an eager search will cause a resolution at the configuration stage,
        * which may affect performance.
        * See https://github.com/Kotlin/kotlinx-kover/issues/235
        */
        val findAgentJarTask = project.tasks.register<KoverAgentJarTask>(FIND_JAR_TASK)
        findAgentJarTask.configure {
            // depends on agent classpath to resolve it in execute-time
            dependsOn(agentClasspath)

            this.tool.convention(toolProvider)
            this.agentJar.set(project.layout.buildDirectory.map { dir -> dir.file(agentFilePath(toolProvider.get().variant)) })
            this.agentClasspath.from(agentClasspath)
        }

        return InstrumentationData(
            findAgentJarTask,
            toolProvider,
            projectExtension.instrumentation.classes
        )
    }
}

/**
 * All configured data used in online instrumentation.
 */
internal class InstrumentationData(
    val findAgentJarTask: TaskProvider<KoverAgentJarTask>,
    val toolProvider: Provider<CoverageTool>,
    val excludedClasses: Set<String>
)
