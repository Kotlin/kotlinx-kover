/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.appliers

import kotlinx.kover.api.*
import kotlinx.kover.api.KoverNames.CONFIGURATION_NAME
import kotlinx.kover.api.KoverNames.HTML_REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.VERIFY_TASK_NAME
import kotlinx.kover.api.KoverNames.XML_REPORT_TASK_NAME
import kotlinx.kover.tools.commons.*
import kotlinx.kover.lookup.*
import kotlinx.kover.tasks.*
import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.provider.*
import org.gradle.api.tasks.testing.*
import org.gradle.kotlin.dsl.*
import org.gradle.configurationcache.extensions.*
import java.io.*

internal fun Project.applyToProject() {
    val extension = createProjectExtension()
    val config = createConfig(extension.tool)

    val toolProvider = toolProvider(config, extension.tool)

    tasks.withType<Test>().configureEach {
        applyToTestTask(extension, toolProvider)
    }

    val testsProvider = instrumentedTasksProvider(extension)

    val xmlTask = createTask<KoverXmlTask>(
        XML_REPORT_TASK_NAME,
        extension.xmlReport.filters,
        extension,
        toolProvider,
        testsProvider,
    ) {
        it.reportFile.set(extension.xmlReport.reportFile)
        it.description = "Generates code coverage XML report for all enabled test tasks in one project."
    }

    val htmlTask = createTask<KoverHtmlTask>(
        HTML_REPORT_TASK_NAME,
        extension.htmlReport.taskFilters,
        extension,
        toolProvider,
        testsProvider,
    ) {
        it.reportDir.set(extension.htmlReport.reportDir)
        it.description = "Generates code coverage HTML report for all enabled test tasks in one project."
    }

    val verifyTask = createTask<KoverVerificationTask>(
        VERIFY_TASK_NAME,
        extension.filters,
        extension,
        toolProvider,
        testsProvider,
    ) {
        it.rules.set(extension.verify.rules)
        it.resultFile.set(layout.buildDirectory.file(KoverPaths.PROJECT_VERIFICATION_REPORT_DEFAULT_PATH))
        it.description = "Verifies code coverage metrics of one project based on specified rules."
    }
    // TODO `onlyIf` block moved out from config lambda because of bug in Kotlin compiler - it implicitly adds closure on `Project` inside onlyIf's lambda
    verifyTask.onlyIf { extension.verify.hasActiveRules() }

    // ordering of task calls, so that if a verification error occurs, reports are generated and the values can be viewed in them
    verifyTask.shouldRunAfter(xmlTask, htmlTask)

    tasks.create(KoverNames.REPORT_TASK_NAME) {
        group = KoverNames.VERIFICATION_GROUP
        dependsOn(xmlTask)
        dependsOn(htmlTask)
        description = "Generates code coverage HTML and XML reports for all enabled test tasks in one project."
    }


    tasks
        .matching { it.name == KoverNames.CHECK_TASK_NAME }
        .configureEach {
            dependsOn(provider {
                // don't add dependency if Kover is disabled
                if (extension.isDisabled.get()) {
                    return@provider emptyList<Task>()
                }

                val tasks = mutableListOf<Task>()
                if (extension.xmlReport.onCheck.get()) {
                    tasks += xmlTask
                }
                if (extension.htmlReport.onCheck.get()) {
                    tasks += htmlTask
                }
                if (extension.verify.onCheck.get() && extension.verify.hasActiveRules()) {
                    // don't add dependency if there is no active verification rules https://github.com/Kotlin/kotlinx-kover/issues/168
                    tasks += verifyTask
                }
                tasks
            })
    }
}

private inline fun <reified T : KoverReportTask> Project.createTask(
    taskName: String,
    filters: KoverProjectFilters,
    extension: KoverProjectConfig,
    toolProvider: Provider<ToolDetails>,
    testsProvider: Provider<List<Test>>,
    crossinline block: (T) -> Unit
): T {
    val task = tasks.create<T>(taskName) {
        files.put(path, projectFilesProvider(extension, filters.sourceSets))
        tool.set(toolProvider)
        dependsOn(testsProvider)
        classFilter.set(filters.classes)
        annotationFilter.set(filters.annotations)
        group = KoverNames.VERIFICATION_GROUP
        block(this)
    }

    // TODO `onlyIf` block moved out from config lambda because of bug in Kotlin compiler - it implicitly adds closure on `Project` inside onlyIf's lambda
    // execute task only if Kover not disabled for project
    task.onlyIf { !extension.isDisabled.get() }
    // execute task only if there is at least one binary report
    task.onlyIf { t -> (t as KoverReportTask).files.get().any { f -> !f.value.binaryReportFiles.isEmpty } }

    return task
}

private fun Project.createProjectExtension(): KoverProjectConfig {
    val extension = extensions.create(KoverNames.PROJECT_EXTENSION_NAME, KoverProjectConfig::class)
    // default values

    extension.isDisabled.convention(false)
    extension.tool.convention(DefaultKoverTool)
    extension.filters.classes.convention(KoverClassFilter())
    extension.filters.annotations.convention(KoverAnnotationFilter())
    extension.filters.sourceSets.convention(KoverSourceSetFilter())
    extension.xmlReport.reportFile.convention(layout.buildDirectory.file(KoverPaths.PROJECT_XML_REPORT_DEFAULT_PATH))
    extension.xmlReport.onCheck.convention(false)
    extension.xmlReport.filters.classes.convention(extension.filters.classes)
    extension.xmlReport.filters.annotations.convention(extension.filters.annotations)
    extension.xmlReport.filters.sourceSets.convention(extension.filters.sourceSets)
    extension.htmlReport.reportDir.convention(layout.buildDirectory.dir(KoverPaths.PROJECT_HTML_REPORT_DEFAULT_PATH))
    extension.htmlReport.onCheck.convention(false)
    extension.htmlReport.taskFilters.classes.convention(extension.filters.classes)
    extension.htmlReport.taskFilters.annotations.convention(extension.filters.annotations)
    extension.htmlReport.taskFilters.sourceSets.convention(extension.filters.sourceSets)
    extension.verify.onCheck.convention(true)

    return extension
}

private fun Project.createConfig(toolVariantProvider: Provider<CoverageToolVariant>): Configuration {
    val config = project.configurations.create(CONFIGURATION_NAME)
    config.isVisible = false
    config.isTransitive = true
    config.description = "Kotlin Kover Plugin configuration"

    config.defaultDependencies {
        val variant = toolVariantProvider.get()
        ToolManager.dependencies(variant).forEach {
            add(dependencies.create(it))
        }
    }
    return config
}

private fun Project.toolProvider(
    config: Configuration,
    toolVariantProvider: Provider<CoverageToolVariant>
): Provider<ToolDetails> {
    val archiveOperations: ArchiveOperations = project.serviceOf()
    return project.provider { toolByVariant(toolVariantProvider.get(), config, archiveOperations) }
}

internal fun toolByVariant(
    variant: CoverageToolVariant,
    config: Configuration,
    archiveOperations: ArchiveOperations
): ToolDetails {
    val jarFile = ToolManager.findJarFile(variant, config, archiveOperations)
    return ToolDetails(variant, jarFile, config)
}

internal fun Project.projectFilesProvider(
    extension: KoverProjectConfig,
    sourceSetFiltersProvider: Provider<KoverSourceSetFilter>
): Provider<ProjectFiles> {
    return provider { projectFiles(sourceSetFiltersProvider.get(), extension) }
}

internal fun Project.projectFiles(
    filters: KoverSourceSetFilter,
    extension: KoverProjectConfig
): ProjectFiles {
    val directories = DirsLookup.lookup(project, filters)
    val reportFiles = files(binaryReports(extension))
    return ProjectFiles(reportFiles, directories.sources, directories.outputs)
}

internal fun Project.instrumentedTasks(extension: KoverProjectConfig): List<Test> {
    return tasks.withType<Test>().asSequence()
        // task can be disabled in the project extension
        .filterNot { extension.instrumentation.excludeTasks.contains(it.name) }
        .filterNot { t -> t.extensions.getByType<KoverTaskExtension>().isDisabled.get() }
        .toList()
}

private fun Project.instrumentedTasksProvider(extension: KoverProjectConfig): Provider<List<Test>> {
    return provider { instrumentedTasks(extension) }
}


private fun Project.binaryReports(extension: KoverProjectConfig): List<File> {
    if (extension.isDisabled.get()) {
        return emptyList()
    }

    return tasks.withType<Test>().asSequence()
        // task can be disabled in the project extension
        .filterNot { extension.instrumentation.excludeTasks.contains(it.name) }
        .map { t -> t.extensions.getByType<KoverTaskExtension>() }
        // process binary report only from tasks with enabled cover
        .filterNot { e -> e.isDisabled.get() }
        .map { e -> e.reportFile.get().asFile }
        // process binary report only from tasks with sources
        .filter { f -> f.exists() }
        .toList()
}


internal fun KoverVerifyConfig.hasActiveRules(): Boolean {
    return rules.get().any { rule -> rule.isEnabled }
}
