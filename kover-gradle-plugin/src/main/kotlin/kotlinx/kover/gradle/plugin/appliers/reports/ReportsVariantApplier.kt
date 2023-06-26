/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers.reports

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.VariantNameAttr
import kotlinx.kover.gradle.plugin.commons.ProjectPathAttr
import kotlinx.kover.gradle.plugin.commons.ReportsVariantType
import kotlinx.kover.gradle.plugin.commons.artifactFilePath
import kotlinx.kover.gradle.plugin.commons.artifactGenerationTaskName
import kotlinx.kover.gradle.plugin.commons.asConsumer
import kotlinx.kover.gradle.plugin.commons.externalArtifactConfigurationName
import kotlinx.kover.gradle.plugin.commons.htmlReportTaskName
import kotlinx.kover.gradle.plugin.commons.artifactConfigurationName
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import kotlinx.kover.gradle.plugin.dsl.MetricType
import kotlinx.kover.gradle.plugin.dsl.internal.KoverReportFiltersImpl
import kotlinx.kover.gradle.plugin.dsl.internal.KoverReportsConfigImpl
import kotlinx.kover.gradle.plugin.dsl.internal.KoverVerifyBoundImpl
import kotlinx.kover.gradle.plugin.dsl.internal.KoverVerifyRuleImpl
import kotlinx.kover.gradle.plugin.tasks.reports.*
import kotlinx.kover.gradle.plugin.tasks.reports.AbstractKoverReportTask
import kotlinx.kover.gradle.plugin.tasks.reports.KoverHtmlTask
import kotlinx.kover.gradle.plugin.tasks.reports.KoverVerifyTask
import kotlinx.kover.gradle.plugin.tasks.reports.KoverXmlTask
import kotlinx.kover.gradle.plugin.tasks.services.KoverArtifactGenerationTask
import kotlinx.kover.gradle.plugin.tasks.services.KoverPrintLogTask
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin

internal sealed class ReportsVariantApplier(
    private val project: Project,
    private val variantName: String,
    private val type: ReportsVariantType,
    private val koverDependencies: Configuration,
    private val reportClasspath: Configuration,
    private val toolProvider: Provider<CoverageTool>
) {
    private val htmlTask: TaskProvider<KoverHtmlTask>
    private val xmlTask: TaskProvider<KoverXmlTask>
    private val verifyTask: TaskProvider<KoverVerifyTask>
    private val logTask: TaskProvider<KoverFormatCoverageTask>

    private val artifactGenTask: TaskProvider<KoverArtifactGenerationTask>
    protected val config: NamedDomainObjectProvider<Configuration>
    protected val dependencies: NamedDomainObjectProvider<Configuration>

    init {
        artifactGenTask = project.tasks.register<KoverArtifactGenerationTask>(artifactGenerationTaskName(variantName))
        artifactGenTask.configure {
            artifactFile.set(project.layout.buildDirectory.file(artifactFilePath(variantName)))
        }

        config = project.configurations.register(artifactConfigurationName(variantName)) {
            // disable generation of Kover artifacts on `assemble`, fix of https://github.com/Kotlin/kotlinx-kover/issues/353
            isVisible = false
            outgoing.artifact(artifactGenTask.map { task -> task.artifactFile }) {
                asProducer()
                attributes {
                    // common Kover artifact attributes
                    attribute(VariantNameAttr.ATTRIBUTE, project.objects.named(variantName))
                    attribute(ProjectPathAttr.ATTRIBUTE, project.objects.named(project.path))
                }
                // Before resolving this configuration, it is necessary to execute the task of generating an artifact
                builtBy(artifactGenTask)
            }
        }

        dependencies = project.configurations.register(externalArtifactConfigurationName(variantName)) {
            asConsumer()
            extendsFrom(koverDependencies)
        }


        htmlTask = project.tasks.createReportTask<KoverHtmlTask>(
            htmlReportTaskName(variantName),
            htmlTaskDescription()
        )
        xmlTask = project.tasks.createReportTask<KoverXmlTask>(
            xmlReportTaskName(variantName),
            xmlTaskDescription()
        )
        verifyTask = project.tasks.createReportTask<KoverVerifyTask>(
            verifyTaskName(variantName),
            verifyTaskDescription()
        )
        logTask = project.tasks.createReportTask<KoverFormatCoverageTask>(
            logTaskName(variantName),
            logTaskDescription()
        )
        val printCoverageTask = project.tasks.register<KoverPrintLogTask>(printLogTaskName(variantName))
        printCoverageTask.configure {
            fileWithMessage.convention(logTask.flatMap { it.outputFile })
            onlyIf {
                fileWithMessage.asFile.get().exists()
            }
        }
        logTask.configure {
            finalizedBy(printCoverageTask)
        }
    }


    fun applyConfig(reportConfig: KoverReportsConfigImpl, commonFilters: KoverReportFiltersImpl? = null) {
        val runOnCheck = mutableListOf<TaskProvider<*>>()

        htmlTask.configure {
            onlyIf { printPath() }

            reportDir.convention(project.layout.dir(reportConfig.html.reportDirProperty))
            title.convention(reportConfig.html.title ?: project.name)
            charset.convention(reportConfig.html.charset)
            filters.set((reportConfig.html.filters ?: reportConfig.filters ?: commonFilters).convert())
        }
        if (reportConfig.html.onCheck) {
            runOnCheck += htmlTask
        }

        xmlTask.configure {
            reportFile.convention(project.layout.file(reportConfig.xml.reportFileProperty))
            filters.set((reportConfig.xml.filters ?: reportConfig.filters ?: commonFilters).convert())
        }
        if (reportConfig.xml.onCheck) {
            runOnCheck += xmlTask
        }

        verifyTask.configure {
            val converted = reportConfig.verify.rules.map { it.convert() }

            // path can't be changed
            resultFile.convention(project.layout.buildDirectory.file(verificationErrorsPath(variantName)))
            filters.set((reportConfig.verify.filters ?: reportConfig.filters ?: commonFilters).convert())
            rules.addAll(converted)

            shouldRunAfter(htmlTask)
            shouldRunAfter(xmlTask)
        }
        if (reportConfig.verify.onCheck) {
            runOnCheck += verifyTask
        }

        logTask.configure {
            header.convention(reportConfig.log.header)
            lineFormat.convention(reportConfig.log.format ?: "<entity> line coverage: <value>%")
            groupBy.convention(reportConfig.log.groupBy ?: GroupingEntityType.APPLICATION)
            coverageUnits.convention(reportConfig.log.coverageUnits ?: MetricType.LINE)
            aggregationForGroup.convention(reportConfig.log.aggregationForGroup ?: AggregationType.COVERED_PERCENTAGE)
            outputFile.convention(project.layout.buildDirectory.file(coverageLogPath(variantName)))

            filters.set((reportConfig.log.filters ?: reportConfig.filters ?: commonFilters).convert())
        }
        if (reportConfig.log.onCheck) {
            runOnCheck += logTask
        }

        project.tasks
            .matching { it.name == LifecycleBasePlugin.CHECK_TASK_NAME }
            .configureEach { dependsOn(runOnCheck) }
    }

    fun mergeWith(otherVariant: ReportsVariantApplier) {
        artifactGenTask.configure {
            additionalArtifacts.from(
                otherVariant.artifactGenTask.map { task -> task.artifactFile },
                otherVariant.dependencies
            )
            dependsOn(otherVariant.artifactGenTask, otherVariant.dependencies)
        }
    }

    protected fun applyCommonCompilationKit(kit: CompilationKit) {
        val tests = kit.tests
        val compilations = kit.compilations.map { it.values }

        // local files and compile tasks
        val compileTasks = compilations.map { unit -> unit.flatMap { it.compileTasks } }
        val outputs = compilations.map { unit -> unit.flatMap { it.outputs } }
        val sources = compilations.map { unit -> unit.flatMap { it.sources } }
        val binReportFiles = project.layout.buildDirectory.dir(binReportsRootPath())
            .map { dir -> tests.map { dir.file(binReportName(it.name, toolProvider.get().variant.vendor)) } }

        artifactGenTask.configure {
            // to generate an artifact, need to compile the entire project and perform all test tasks
            dependsOn(tests)
            dependsOn(compileTasks)

            this.sources.from(sources)
            this.outputDirs.from(outputs)
            this.reports.from(binReportFiles)
        }
    }

    private inline fun <reified T : AbstractKoverReportTask> TaskContainer.createReportTask(
        name: String,
        taskDescription: String
    ): TaskProvider<T> {
        val task = register<T>(name)
        task.configure {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = taskDescription
            tool.convention(toolProvider)
            reportClasspath.from(this@ReportsVariantApplier.reportClasspath)

            dependsOn(artifactGenTask)
            dependsOn(dependencies)

            localArtifact.set(artifactGenTask.flatMap { task -> task.artifactFile })
            externalArtifacts.from(dependencies)
        }
        return task
    }

    private fun xmlTaskDescription(): String {
        return when (type) {
            ReportsVariantType.DEFAULT -> "Task to generate XML coverage report for JVM project or Kotlin/MPP JVM targets. Android measurements for specific build variant can be merged"
            ReportsVariantType.ANDROID -> "Task to generate XML coverage report for '$variantName' Android build variant"
        }
    }

    private fun htmlTaskDescription(): String {
        return when (type) {
            ReportsVariantType.DEFAULT -> "Task to generate HTML coverage report for JVM project or Kotlin/MPP JVM targets. Android measurements for specific build variant can be merged"
            ReportsVariantType.ANDROID -> "Task to generate HTML coverage report for '$variantName' Android build variant"
        }
    }

    private fun verifyTaskDescription(): String {
        return when (type) {
            ReportsVariantType.DEFAULT -> "Task to validate coverage bounding rules for JVM project or Kotlin/MPP JVM targets. Android measurements for specific build variant can be merged"
            ReportsVariantType.ANDROID -> "Task to validate coverage bounding rules for '$variantName' Android build variant"
        }
    }

    private fun logTaskDescription(): String {
        return when (type) {
            ReportsVariantType.DEFAULT -> "Task to print coverage to log for JVM project or Kotlin/MPP JVM targets. Android measurements for specific build variant can be merged"
            ReportsVariantType.ANDROID -> "Task to print coverage to log for '$variantName' Android build variant"
        }
    }
}


private fun KoverVerifyRuleImpl.convert(): VerificationRule {
    return VerificationRule(isEnabled, filters?.convert(), internalName, entity, bounds.map { it.convert() })
}

private fun KoverVerifyBoundImpl.convert(): VerificationBound {
    return VerificationBound(minValue?.toBigDecimal(), maxValue?.toBigDecimal(), metric, aggregation)
}

private fun KoverReportFiltersImpl?.convert(): ReportFilters {
    this ?: return emptyFilters

    return ReportFilters(
        includesIntern.classes, includesIntern.annotations,
        excludesIntern.classes, excludesIntern.annotations
    )
}

private val emptyFilters = ReportFilters()
