/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.internal.*
import kotlinx.kover.gradle.plugin.tasks.*
import kotlinx.kover.gradle.plugin.tools.*
import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.*

/**
 * Gradle Plugin applier for creating Kover reports.
 */
internal class ReportsApplier(
    private val variant: Variant,
    private val project: Project,
    private val tool: CoverageTool,
    private val reportClasspath: Configuration,
) {

    fun createReports(
        reportConfig: KoverReportsConfigImpl,
        commonFilters: KoverReportFiltersImpl? = null
    ) {
        val runOnCheck = mutableListOf<TaskProvider<*>>()

        val htmlTask = project.tasks.createReportTask<KoverHtmlTask>(htmlReportTaskName(variant.name)) {
            onlyIf { printPath() }

            reportDir.convention(project.layout.dir(reportConfig.html.reportDirProperty))
            title.convention(reportConfig.html.title ?: project.name)
            charset.convention(reportConfig.html.charset)
            filters.set((reportConfig.html.filters ?: reportConfig.filters ?: commonFilters).convert())
        }
        if (reportConfig.html.onCheck) {
            runOnCheck += htmlTask
        }

        val xmlTask = project.tasks.createReportTask<KoverXmlTask>(xmlReportTaskName(variant.name)) {
            reportFile.convention(project.layout.file(reportConfig.xml.reportFileProperty))
            filters.set((reportConfig.xml.filters ?: reportConfig.filters ?: commonFilters).convert())
        }
        if (reportConfig.xml.onCheck) {
            runOnCheck += xmlTask
        }

        val verifyTask = project.tasks.createReportTask<KoverVerifyTask>(verifyTaskName(variant.name)) {
            val converted = reportConfig.verify.rules
                .map { it.convert() }

            // path can't be changed
            resultFile.convention(project.layout.buildDirectory.file(verificationErrorsPath(variant.name)))
            filters.set((reportConfig.verify.filters ?: reportConfig.filters ?: commonFilters).convert())
            rules.addAll(converted)

            shouldRunAfter(htmlTask)
            shouldRunAfter(xmlTask)
        }
        if (reportConfig.verify.onCheck) {
            runOnCheck += verifyTask
        }

        project.tasks
            .matching { it.name == LifecycleBasePlugin.CHECK_TASK_NAME }
            .configureEach { dependsOn(runOnCheck) }
    }

    private inline fun <reified T : AbstractKoverReportTask> TaskContainer.createReportTask(
        name: String,
        crossinline config: T.() -> Unit
    ): TaskProvider<T> {
        val task = register<T>(name, tool)
        task.configure {
            group = LifecycleBasePlugin.VERIFICATION_GROUP

            dependsOn(variant.localArtifactGenerationTask)
            dependsOn(variant.dependentArtifactsConfiguration)

            // task can't be executed if where is no raw report files (no any executed test task)
            onlyIf { hasRawReportsAndLog() }

            localArtifact.set(variant.localArtifact)
            this.externalArtifacts.from(variant.dependentArtifactsConfiguration)
            reportClasspath.from(this@ReportsApplier.reportClasspath)
            config()
        }
        return task
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
}

private val emptyFilters = ReportFilters()
