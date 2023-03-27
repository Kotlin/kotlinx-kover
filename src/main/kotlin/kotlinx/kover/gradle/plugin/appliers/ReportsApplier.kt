/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.KoverNames.DEPENDENCY_CONFIGURATION_NAME
import kotlinx.kover.gradle.plugin.dsl.internal.*
import kotlinx.kover.gradle.plugin.tasks.*
import kotlinx.kover.gradle.plugin.tools.*
import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.*

/**
 * Gradle Plugin applier for creating Kover reports.
 */
internal class ReportsApplier(
    private val artifact: Artifact,
    private val project: Project,
    private val tool: CoverageTool,
    private val reportClasspath: Configuration,
) {

    fun createReports(
        reportConfig: KoverReportExtensionImpl?,
        generalReportConfig: KoverGeneralAndroidReportImpl? = null,
        verifyOnCheck: Boolean = true
    ) {
        val runOnCheck = mutableListOf<TaskProvider<*>>()

        val buildDir = project.layout.buildDirectory
        val htmlTask = project.tasks.createReportTask<KoverHtmlTask>(htmlReportTaskName(artifact.name)) {
            onlyIf { printPath() }

            //
            val reportDirV = if (reportConfig != null && reportConfig.html.reportDirProperty.isPresent) {
                project.layout.dir(reportConfig.html.reportDirProperty)
            } else {
                buildDir.dir(htmlReportPath(artifact.name))
            }

            //custom defined title takes precedence over default title. Project name by default
            val titleV = reportConfig?.html?.title ?: generalReportConfig?.html?.title ?: project.name

            // custom filters are in priority, html block priority over common filters. No filters by default
            val generalFilters = generalReportConfig?.html?.filters ?: generalReportConfig?.commonFilters
            val reportFilters = reportConfig?.html?.filters ?: reportConfig?.commonFilters
            val resultFiltersV = (reportFilters ?: generalFilters)?.convert() ?: emptyFilters

            reportDir.convention(reportDirV)
            title.convention(titleV)
            filters.set(resultFiltersV)
        }
        // false by default
        if (reportConfig?.html?.onCheck == true) {
            runOnCheck += htmlTask
        }

        val xmlTask = project.tasks.createReportTask<KoverXmlTask>(xmlReportTaskName(artifact.name)) {
            //
            val reportFileV = if (reportConfig != null && reportConfig.xml.reportFileProperty.isPresent) {
                project.layout.file(reportConfig.xml.reportFileProperty)
            } else {
                buildDir.file(xmlReportPath(artifact.name))
            }

            // custom filters are in priority, html block priority over common filters. No filters by default
            val generalFilters = generalReportConfig?.xml?.filters ?: generalReportConfig?.commonFilters
            val reportFilters = reportConfig?.xml?.filters ?: reportConfig?.commonFilters
            val resultFiltersV = (reportFilters ?: generalFilters)?.convert() ?: emptyFilters

            reportFile.convention(reportFileV)
            filters.set(resultFiltersV)
        }
        // false by default
        if (reportConfig?.xml?.onCheck == true) {
            runOnCheck += xmlTask
        }

        val verifyTask = project.tasks.createReportTask<KoverVerifyTask>(verifyTaskName(artifact.name)) {
            // custom filters are in priority, html block priority over common filters. No filters by default
            val generalFiltersV =
                (reportConfig?.commonFilters ?: generalReportConfig?.commonFilters)?.convert() ?: emptyFilters

            val rulesV =
                reportConfig?.verify?.definedRules() ?: generalReportConfig?.verify?.definedRules() ?: emptyList()

            // path can't be changed
            resultFile.convention(project.layout.buildDirectory.file(verificationErrorsPath(artifact.name)))
            filters.set(generalFiltersV)
            rules.addAll(rulesV.map { it.convert() })

            shouldRunAfter(htmlTask)
            shouldRunAfter(xmlTask)
        }
        if (reportConfig?.verify?.onCheck ?: verifyOnCheck) {
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

            dependsOn(artifact.localArtifactGenerationTask)
            dependsOn(artifact.dependencies)

            // task can't be executed if where is no raw report files (no any executed test task)
            onlyIf { hasRawReportsAndLog() }

            localArtifact.set(artifact.localArtifact)
            this.externalArtifacts.from(artifact.dependencies)
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

    private fun KoverReportFiltersImpl.convert(): ReportFilters {
        return ReportFilters(
            includesIntern.classes, includesIntern.annotations,
            excludesIntern.classes, excludesIntern.annotations
        )
    }
}

private val emptyFilters = ReportFilters()
