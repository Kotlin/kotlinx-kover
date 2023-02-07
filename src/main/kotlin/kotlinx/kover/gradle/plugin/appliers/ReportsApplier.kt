/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.dsl.internal.*
import kotlinx.kover.gradle.plugin.tasks.*
import kotlinx.kover.gradle.plugin.tasks.internal.KoverArtifactGenerationTask
import kotlinx.kover.gradle.plugin.tools.*
import kotlinx.kover.gradle.plugin.util.*
import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.*


internal class ReportsApplier(
    private val project: Project,
    private val tool: CoverageTool,
    private val localArtifactGenTask: Provider<KoverArtifactGenerationTask>,
    private val reportConfig: Configuration,
    private val setupId: SetupId
) {

    fun createReports(
        defaultExtension: KoverReportExtensionImpl,
        customExtension: KoverReportExtensionImpl?
    ) {
        val extReportContext = createExternalReportContext()

        val runOnCheck = mutableListOf<TaskProvider<*>>()
        val extension = customExtension ?: defaultExtension

        val buildDir = project.layout.buildDirectory
        val htmlTask = project.tasks.createReportTask<KoverHtmlTask>(htmlReportTaskName(setupId), extReportContext) {
            //
            val reportDirT = if (setupId.isDefault) {
                defaultExtension.html.reportDir.isPresent.ifTrue { project.layout.dir(defaultExtension.html.reportDir) }
            } else {
                customExtension?.html?.reportDir?.isPresent?.ifTrue { project.layout.dir(customExtension.html.reportDir) }
            } ?: buildDir.dir(htmlReportPath(setupId))

            //custom defined title takes precedence over default title. Project name by default
            val titleT = customExtension?.html?.title ?: defaultExtension.html.title ?: project.name

            // custom filters are in priority, html block priority over common filters. No filters by default
            val customFilters = customExtension?.html?.filters ?: customExtension?.commonFilters
            val defaultFilters = defaultExtension.html.filters ?: defaultExtension.commonFilters
            val resultFilters = (customFilters ?: defaultFilters)?.convert() ?: emptyFilters

            reportDir.convention(reportDirT)
            title.convention(titleT)
            filters.set(resultFilters)
        }
        // false by default
        if (extension.html.onCheck == true) {
            runOnCheck += htmlTask
        }

        val xmlTask = project.tasks.createReportTask<KoverXmlTask>(xmlReportTaskName(setupId), extReportContext) {
            //
            val reportDirT = if (setupId.isDefault) {
                defaultExtension.xml.reportFile.isPresent.ifTrue { project.layout.file(defaultExtension.xml.reportFile) }
            } else {
                customExtension?.xml?.reportFile?.isPresent?.ifTrue { project.layout.file(customExtension.xml.reportFile) }
            } ?: buildDir.file(xmlReportPath(setupId))

            // custom filters are in priority, html block priority over common filters. No filters by default
            val customFilters = customExtension?.xml?.filters ?: customExtension?.commonFilters
            val defaultFilters = defaultExtension.xml.filters ?: defaultExtension.commonFilters
            val resultFilters = (customFilters ?: defaultFilters)?.convert() ?: emptyFilters

            reportFile.convention(reportDirT)
            filters.set(resultFilters)
        }
        // false by default
        if (extension.xml.onCheck == true) {
            runOnCheck += xmlTask
        }

        val verifyTask = project.tasks.createReportTask<KoverVerifyTask>(verifyTaskName(setupId), extReportContext) {
            // custom filters are in priority, html block priority over common filters. No filters by default
            val resultFilters = (customExtension?.commonFilters ?: defaultExtension.commonFilters)?.convert() ?: emptyFilters

            val rulesT = customExtension?.verify?.definedRules() ?: defaultExtension.verify.definedRules() ?: emptyList()

            // path can't be changed
            resultFile.convention(project.layout.buildDirectory.file(verificationErrorsPath(setupId)))
            filters.set(resultFilters)
            rules.addAll(rulesT.map { it.convert() })

            shouldRunAfter(htmlTask)
            shouldRunAfter(xmlTask)
        }
        if (extension.verify.onCheck) {
            runOnCheck += verifyTask
        }

        project.tasks
            .matching { it.name == LifecycleBasePlugin.CHECK_TASK_NAME }
            .configureEach { dependsOn(runOnCheck) }
    }

    private fun createExternalReportContext(): NamedDomainObjectProvider<Configuration> {
        return project.configurations.register(aggSetupConfigurationName(setupId)) {
            asConsumer()
            attributes {
                setupName(setupId.name, project.objects)
            }
            extendsFrom(project.configurations.getByName(DEPENDENCY_CONFIGURATION_NAME))
        }
    }

    private inline fun <reified T : AbstractKoverReportTask> TaskContainer.createReportTask(
        name: String,
        reportContext: Provider<Configuration>,
        crossinline config: T.() -> Unit
    ): TaskProvider<T> {
        val task = register<T>(name, tool)
        task.configure {
            group = LifecycleBasePlugin.VERIFICATION_GROUP

            dependsOn(localArtifactGenTask)
            dependsOn(reportContext)

            // task can't be executed if where is no raw report files (no any executed test task)
            onlyIf { hasRawReports() }

            localArtifact.set(localArtifactGenTask.flatMap { it.artifactFile })
            this.externalArtifacts.from(reportContext)
            reportClasspath.from(reportConfig)
            config()
        }
        return task
    }

    private fun KoverVerifyRuleImpl.convert(): VerificationRule {
        return VerificationRule(isEnabled, filters?.convert(), name, entity, bounds.map { it.convert() })
    }

    private fun KoverVerifyBoundImpl.convert(): VerificationBound {
        return VerificationBound(minValue?.toBigDecimal(), maxValue?.toBigDecimal(), metric, aggregation)
    }

    private fun KoverReportFiltersImpl.convert(): ReportFilters {
        return ReportFilters(
            includes.classes, includes.annotations,
            excludes.classes, excludes.annotations
        )
    }
}

private val emptyFilters = ReportFilters()
