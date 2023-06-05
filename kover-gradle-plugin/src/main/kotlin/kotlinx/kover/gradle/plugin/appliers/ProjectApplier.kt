/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.KoverNames.DEPENDENCY_CONFIGURATION_NAME
import kotlinx.kover.gradle.plugin.dsl.KoverNames.PROJECT_EXTENSION_NAME
import kotlinx.kover.gradle.plugin.dsl.KoverNames.REPORT_EXTENSION_NAME
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import kotlinx.kover.gradle.plugin.dsl.internal.KoverReportExtensionImpl
import kotlinx.kover.gradle.plugin.locators.CompilationsLocatorFactory
import kotlinx.kover.gradle.plugin.tasks.services.KoverAgentJarTask
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import kotlinx.kover.gradle.plugin.tools.CoverageToolFactory
import kotlinx.kover.gradle.plugin.tools.CoverageToolVariant
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

/**
 * Main Gradle Plugin applier of the project.
 */
internal class ProjectApplier(private val project: Project) {
    private lateinit var projectExtension: KoverProjectExtensionImpl
    private lateinit var reportExtension: KoverReportExtensionImpl

    /**
     * The code executed right at the moment of applying of the plugin.
     */
    fun onApply() {
        project.configurations.create(DEPENDENCY_CONFIGURATION_NAME) {
            asBucket()
        }

        projectExtension = project.extensions.create(PROJECT_EXTENSION_NAME)
        reportExtension = project.extensions.create(REPORT_EXTENSION_NAME)
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

        val instrData = collectInstrumentationData(tool, agentClasspath)
        val locator = CompilationsLocatorFactory.get(project)

        val locate = locator.locate(projectExtension)

        val androidVariants = locate.android.associate {
            it.buildVariant to project.createAndroidVariant(locate.kotlinPlugin, it, instrData, tool.variant)
        }

        val defaultVariant = createDefaultVariant(locate.kotlinPlugin, locate.jvm, instrData, tool.variant)

        reportExtension.default.merged.forEach { addedVariant ->
            val androidVariant = androidVariants[addedVariant] ?: throw KoverIllegalConfigException("Android build variant '$addedVariant' was not found - it is impossible to add it to the default report")

            defaultVariant.localArtifactGenerationTask.configure {
                additionalArtifacts.from(androidVariant.localArtifact, androidVariant.dependentArtifactsConfiguration)
                dependsOn(androidVariant.localArtifactGenerationTask, androidVariant.dependentArtifactsConfiguration)
            }
        }

        ReportsApplier(defaultVariant, project, instrData.tool, reporterClasspath)
            .createReports(reportExtension.default, reportExtension.filters)

        androidVariants.forEach { (variantName, androidVariant) ->
            val androidReport = reportExtension.namedReports[variantName] ?: project.objects.androidReports(variantName, project.layout)

            ReportsApplier(androidVariant, project, instrData.tool, reporterClasspath)
                .createReports(androidReport, reportExtension.filters)
        }
    }

    /**
     * Create default report variant.
     */
    private fun createDefaultVariant(
        kotlinPlugin: AppliedKotlinPlugin,
        kits: List<JvmCompilationKit>,
        instrData: InstrumentationData,
        toolVariant: CoverageToolVariant
    ): Variant {
        // local project tasks and files
        val tests = kits.map { kit ->
            kit.tests.configureTests(instrData)
        }
        val compilations = kits.map { kit ->
            kit.compilations.map { it.values }
        }

        val artifact = project.createArtifactGenerationTask(
            DEFAULT_KOVER_VARIANT_NAME,
            compilations,
            tests,
            toolVariant,
            kotlinPlugin
        )

        artifact.dependentArtifactsConfiguration.configure {
            attributes {
                attribute(ArtifactNameAttr.ATTRIBUTE, project.objects.named(DEFAULT_KOVER_VARIANT_NAME))
            }
        }

        return artifact
    }

    /**
     * Collect all configured data, required for online instrumentation.
     */
    private fun collectInstrumentationData(tool: CoverageTool, agentClasspath: Configuration): InstrumentationData {
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
}

/**
 * All configured data used in online instrumentation.
 */
internal class InstrumentationData(
    val findAgentJarTask: TaskProvider<KoverAgentJarTask>,
    val tool: CoverageTool,
    val excludedClasses: Set<String>
)
