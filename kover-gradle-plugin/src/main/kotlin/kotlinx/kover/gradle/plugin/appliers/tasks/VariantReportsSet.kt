/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers.tasks

import kotlinx.kover.gradle.plugin.appliers.artifacts.AbstractVariantArtifacts
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.KoverVerifyBound
import kotlinx.kover.gradle.plugin.dsl.internal.KoverReportFiltersConfigImpl
import kotlinx.kover.gradle.plugin.dsl.internal.KoverReportSetConfigImpl
import kotlinx.kover.gradle.plugin.dsl.internal.KoverVerifyRuleImpl
import kotlinx.kover.gradle.plugin.tasks.reports.*
import kotlinx.kover.gradle.plugin.tasks.services.KoverPrintLogTask
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin


internal class VariantReportsSet(
    private val project: Project,
    private val variantName: String,
    private val type: ReportVariantType,
    private val toolProvider: Provider<CoverageTool>,
    private val config: KoverReportSetConfigImpl,
    private val reporterConfiguration: Configuration,
    private val koverDisabled: Provider<Boolean>
) {
    private val htmlTask: TaskProvider<KoverHtmlTask>
    private val xmlTask: TaskProvider<KoverXmlTask>
    private val binTask: TaskProvider<KoverBinaryTask>
    private val verifyTask: TaskProvider<KoverVerifyTask>
    private val logTask: TaskProvider<KoverFormatCoverageTask>

    init {
        htmlTask = project.tasks.createReportTask<KoverHtmlTask>(
            htmlReportTaskName(variantName),
            "Task to generate HTML coverage report for ${variantSuffix()}"
        )
        xmlTask = project.tasks.createReportTask<KoverXmlTask>(
            xmlReportTaskName(variantName),
            "Task to generate XML coverage report for ${variantSuffix()}"
        )
        binTask = project.tasks.createReportTask<KoverBinaryTask>(
            binaryReportTaskName(variantName),
            "Task to generate binary coverage report in IntelliJ format for ${variantSuffix()}"
        )

        verifyTask = project.tasks.createReportTask<KoverVerifyTask>(
            verifyTaskName(variantName),
            "Task to validate coverage bounding rules for ${variantSuffix()}"
        )
        logTask = project.tasks.createReportTask<KoverFormatCoverageTask>(
            logTaskName(variantName),
            "Task to print coverage to log for ${variantSuffix()}"
        )
        val printCoverageTask = project.tasks.register<KoverPrintLogTask>(printLogTaskName(variantName))

        val runOnCheck = mutableListOf<Provider<List<Provider<out Task>>>>()

        htmlTask.configure {
            onlyIf { printPath(); true }

            reportDir.convention(config.html.htmlDir)
            title.convention(config.html.title.orElse(project.name))
            charset.convention(config.html.charset)
            filters.set((config.filters).convert())
        }
        runOnCheck += config.html.onCheck.map { run ->
            if (run) listOf(htmlTask) else emptyList()
        }

        xmlTask.configure {
            reportFile.convention(config.xml.xmlFile)
            title.convention(config.xml.title)
            filters.set((config.filters).convert())
        }
        runOnCheck += config.xml.onCheck.map { run ->
            if (run) listOf(xmlTask) else emptyList()
        }

        binTask.configure {
            file.convention(config.binary.file)
            filters.set((config.filters).convert())
        }
        runOnCheck += config.binary.onCheck.map { run ->
            if (run) listOf(binTask) else emptyList()
        }

        verifyTask.configure {
            val resultRules = config.verify.rules
            val converted = resultRules.map { rules -> rules.map { it.convert() } }

            // path can't be changed
            resultFile.convention(project.layout.buildDirectory.file(verificationErrorsPath(variantName)))

            filters.set((config.filters).convert())
            rules.addAll(converted)

            shouldRunAfter(htmlTask)
            shouldRunAfter(xmlTask)
            shouldRunAfter(binTask)
            shouldRunAfter(logTask)
        }
        runOnCheck += config.verify.onCheck.map { run ->
            if (run) listOf(verifyTask) else emptyList()
        }

        printCoverageTask.configure {
            fileWithMessage.convention(logTask.flatMap { it.outputFile })
            onlyIf {
                fileWithMessage.asFile.get().exists()
            }
        }

        logTask.configure {
            header.convention(config.log.header)
            lineFormat.convention(config.log.format)
            groupBy.convention(config.log.groupBy)
            coverageUnits.convention(config.log.coverageUnits)
            aggregationForGroup.convention(config.log.aggregationForGroup)

            outputFile.convention(project.layout.buildDirectory.file(coverageLogPath(variantName)))

            filters.set((config.filters).convert())

            finalizedBy(printCoverageTask)
        }
        runOnCheck += config.log.onCheck.map { run ->
            if (run) listOf(logTask) else emptyList()
        }


        project.tasks
            .matching { it.name == LifecycleBasePlugin.CHECK_TASK_NAME }
            .configureEach { dependsOn(runOnCheck) }
    }

    internal fun assign(variant: AbstractVariantArtifacts) {
        htmlTask.assign(variant)
        xmlTask.assign(variant)
        binTask.assign(variant)
        verifyTask.assign(variant)
        logTask.assign(variant)
    }

    private inline fun <reified T : AbstractKoverReportTask> TaskContainer.createReportTask(
        name: String,
        taskDescription: String
    ): TaskProvider<T> {
        val task = register<T>(name)
        // extract property to variable so as not to create a closure to `this`
        val koverDisabledProvider = koverDisabled
        task.configure {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = taskDescription
            tool.convention(toolProvider)
            reportClasspath.from(reporterConfiguration)

            onlyIf { !koverDisabledProvider.get() }
        }
        return task
    }

    private fun <T : AbstractKoverReportTask> TaskProvider<T>.assign(variant: AbstractVariantArtifacts) {
        configure {
            dependsOn(variant.artifactGenTask)
            dependsOn(variant.consumerConfiguration)

            localArtifact.set(variant.artifactGenTask.flatMap { task -> task.artifactFile })
            externalArtifacts.from(variant.consumerConfiguration)
        }
    }

    private fun variantSuffix(): String {
        return when (type) {
            ReportVariantType.TOTAL -> "all code."
            ReportVariantType.ANDROID -> "'$variantName' Android build variant"
            ReportVariantType.JVM -> "Kotlin JVM"
            ReportVariantType.CUSTOM -> "custom report variant '$variantName'"
        }
    }

    private fun KoverReportFiltersConfigImpl.convert(): Provider<ReportFilters> {
        return project.provider {
            ReportFilters(
                includesImpl.classes.get(), includesImpl.annotations.get(),
                excludesImpl.classes.get(), excludesImpl.annotations.get()
            )
        }
    }
}


private fun KoverVerifyRuleImpl.convert(): VerificationRule {
    return VerificationRule(!disabled.get(), name, groupBy.get(), bounds.map { it.convert() })
}

private fun KoverVerifyBound.convert(): VerificationBound {
    return VerificationBound(min.orNull?.toBigDecimal(), max.orNull?.toBigDecimal(), coverageUnits.get(), aggregationForGroup.get())
}
