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
import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.*


internal class ReportsApplier(
    private val project: Project,
    private val tool: CoverageTool,
    private val localCollectTask: Provider<KoverArtifactGenerationTask>,
    private val reportConfig: Configuration,
    private val setupId: SetupId
) {

    fun createReports(
        defaultExtension: KoverReportExtensionImpl,
        customExtension: KoverReportExtensionImpl?
    ) {
        val aggSetup = project.configurations.register(aggSetupConfigurationName(setupId)) {
            asConsumer()
            attributes {
                setupName(setupId.name, project.objects)
            }
            extendsFrom(project.configurations.getByName(DEPENDENCY_CONFIGURATION_NAME))
        }

        val runOnCheck = mutableListOf<TaskProvider<*>>()
        val extension = customExtension ?: defaultExtension

        val htmlTask = project.tasks.createReportTask<KoverHtmlTask>(htmlReportTaskName(setupId), aggSetup) {
            reportDir.convention(project.layout.dir(extension.html.reportDir))
            title.convention(extension.html.title)

            val customFilters = customExtension?.html?.filters ?: customExtension?.commonFilters
            val defaultFilters = defaultExtension.html.filters ?: defaultExtension.commonFilters
            val resultFilters = (customFilters ?: defaultFilters)?.convert() ?: emptyFilters
            filters.set(resultFilters)
        }
        if (extension.html.onCheck) {
            runOnCheck += htmlTask
        }

        val xmlTask = project.tasks.createReportTask<KoverXmlTask>(xmlReportTaskName(setupId), aggSetup) {
            reportFile.convention(project.layout.file(extension.xml.reportFile))

            val customFilters = customExtension?.xml?.filters ?: customExtension?.commonFilters
            val defaultFilters = defaultExtension.xml.filters ?: defaultExtension.commonFilters
            val resultFilters = (customFilters ?: defaultFilters)?.convert() ?: emptyFilters
            filters.set(resultFilters)
        }
        if (extension.xml.onCheck) {
            runOnCheck += xmlTask
        }

        val verifyTask = project.tasks.createReportTask<KoverVerifyTask>(verifyTaskName(setupId), aggSetup) {
            resultFile.convention(project.layout.buildDirectory.file(verificationErrorsPath(setupId)))

            val customFilters = customExtension?.commonFilters
            val defaultFilters = defaultExtension.commonFilters
            val resultFilters = (customFilters ?: defaultFilters)?.convert() ?: emptyFilters
            filters.set(resultFilters)

            val rules = customExtension?.verify?.definedRules() ?: defaultExtension.verify.definedRules() ?: emptyList()
            this.rules.addAll(rules.map { it.convert() })

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


    private inline fun <reified T : AbstractKoverReportTask> TaskContainer.createReportTask(
        name: String,
        externalArtifacts: Provider<Configuration>,
        crossinline config: T.() -> Unit
    ): TaskProvider<T> {
        val task = register<T>(name, tool)
        task.configure {
            group = LifecycleBasePlugin.VERIFICATION_GROUP

            dependsOn(localCollectTask)
            dependsOn(externalArtifacts)

            // task can't be executed if where is no raw report files (no any executed test task)
            onlyIf { hasRawReports() }

            localArtifact.set(localCollectTask.flatMap { it.artifactFile })
            this.externalArtifacts.from(externalArtifacts)
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
