/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.appliers

import kotlinx.kover.api.*
import kotlinx.kover.api.KoverNames.CONFIGURATION_NAME
import kotlinx.kover.api.KoverNames.HTML_REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.VERIFY_TASK_NAME
import kotlinx.kover.api.KoverNames.XML_REPORT_TASK_NAME
import kotlinx.kover.engines.commons.*
import kotlinx.kover.lookup.*
import kotlinx.kover.tasks.*
import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.provider.*
import org.gradle.api.tasks.testing.*
import org.gradle.configurationcache.extensions.*
import java.io.*

internal fun Project.applyToProject() {
    val extension = createProjectExtension()
    val config = createEngineConfig(extension.engine)

    val engineProvider = engineProvider(config, extension.engine)

    tasks.withType(Test::class.java).configureEach { t ->
        t.applyToTestTask(extension, engineProvider)
    }

    val testsProvider = instrumentedTasksProvider(extension)

    val xmlTask = createTask<KoverXmlTask>(
        XML_REPORT_TASK_NAME,
        extension.xmlReport.taskFilters,
        extension,
        engineProvider,
        testsProvider,
    ) {
        it.reportFile.set(extension.xmlReport.reportFile)
        it.description = "Generates code coverage XML report for all enabled test tasks in one project."
    }

    val htmlTask = createTask<KoverHtmlTask>(
        HTML_REPORT_TASK_NAME,
        extension.htmlReport.taskFilters,
        extension,
        engineProvider,
        testsProvider,
    ) {
        it.reportDir.set(extension.htmlReport.reportDir)
        it.description = "Generates code coverage HTML report for all enabled test tasks in one project."
    }

    val verifyTask = createTask<KoverVerificationTask>(
        VERIFY_TASK_NAME,
        extension.filters,
        extension,
        engineProvider,
        testsProvider,
    ) {
        it.rules.set(extension.verify.rules)
        it.resultFile.set(layout.buildDirectory.file(KoverPaths.PROJECT_VERIFICATION_REPORT_DEFAULT_PATH))
        it.description = "Verifies code coverage metrics of one project based on specified rules."
    }
    // TODO `onlyIf` block moved out from config lambda because of bug in Kotlin compiler - it implicitly adds closure on `Project` inside onlyIf's lambda
    verifyTask.onlyIf { extension.verify.hasActiveRules() }


    tasks.create(KoverNames.REPORT_TASK_NAME) {
        it.group = KoverNames.VERIFICATION_GROUP
        it.dependsOn(xmlTask)
        it.dependsOn(htmlTask)
        it.description = "Generates code coverage HTML and XML reports for all enabled test tasks in one project."
    }


    tasks.configureEach {
        if (it.name == KoverNames.CHECK_TASK_NAME) {
            it.dependsOn(provider {
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
}

private inline fun <reified T : KoverReportTask> Project.createTask(
    taskName: String,
    filters: KoverProjectFilters,
    extension: KoverProjectConfig,
    engineProvider: Provider<EngineDetails>,
    testsProvider: Provider<List<Test>>,
    crossinline block: (T) -> Unit
): T {
    val task = tasks.create(taskName, T::class.java) {
        it.files.put(path, projectFilesProvider(extension, filters.sourceSets))
        it.engine.set(engineProvider)
        it.dependsOn(testsProvider)
        it.classFilters.set(filters.classes)
        it.group = KoverNames.VERIFICATION_GROUP
        block(it)
    }

    // TODO `onlyIf` block moved out from config lambda because of bug in Kotlin compiler - it implicitly adds closure on `Project` inside onlyIf's lambda
    // execute task only if Kover not disabled for project
    task.onlyIf { !extension.isDisabled.get() }
    // execute task only if there is at least one binary report
    task.onlyIf { t -> (t as KoverReportTask).files.get().any { f -> !f.value.binaryReportFiles.isEmpty } }

    return task
}

private fun Project.createProjectExtension(): KoverProjectConfig {
    val extension = extensions.create(KoverNames.PROJECT_EXTENSION_NAME, KoverProjectConfig::class.java, objects)
    // default values

    extension.isDisabled.set(false)
    extension.engine.set(DefaultIntellijEngine)
    extension.xmlReport.reportFile.set(layout.buildDirectory.file(KoverPaths.PROJECT_XML_REPORT_DEFAULT_PATH))
    extension.xmlReport.onCheck.set(false)
    extension.xmlReport.taskFilters.classes.set(extension.filters.classes)
    extension.xmlReport.taskFilters.sourceSets.set(extension.filters.sourceSets)
    extension.htmlReport.reportDir.set(layout.buildDirectory.dir(KoverPaths.PROJECT_HTML_REPORT_DEFAULT_PATH))
    extension.htmlReport.onCheck.set(false)
    extension.htmlReport.taskFilters.classes.set(extension.filters.classes)
    extension.htmlReport.taskFilters.sourceSets.set(extension.filters.sourceSets)
    extension.verify.onCheck.set(true)

    return extension
}

private fun Project.createEngineConfig(engineVariantProvider: Provider<CoverageEngineVariant>): Configuration {
    val config = project.configurations.create(CONFIGURATION_NAME)
    config.isVisible = false
    config.isTransitive = true
    config.description = "Kotlin Kover Plugin configuration for Coverage Engine"

    config.defaultDependencies { default ->
        val variant = engineVariantProvider.get()
        EngineManager.dependencies(variant).forEach {
            default.add(dependencies.create(it))
        }
    }
    return config
}

private fun Project.engineProvider(
    config: Configuration,
    engineVariantProvider: Provider<CoverageEngineVariant>
): Provider<EngineDetails> {
    val archiveOperations: ArchiveOperations = project.serviceOf()
    return project.provider { engineByVariant(engineVariantProvider.get(), config, archiveOperations) }
}

internal fun engineByVariant(
    variant: CoverageEngineVariant,
    config: Configuration,
    archiveOperations: ArchiveOperations
): EngineDetails {
    val jarFile = EngineManager.findJarFile(variant, config, archiveOperations)
    return EngineDetails(variant, jarFile, config)
}

internal fun Project.projectFilesProvider(
    extension: KoverProjectConfig,
    sourceSetFiltersProvider: Provider<KoverSourceSetFilters>
): Provider<ProjectFiles> {
    return provider { projectFiles(sourceSetFiltersProvider.get(), extension) }
}

internal fun Project.projectFiles(
    filters: KoverSourceSetFilters,
    extension: KoverProjectConfig
): ProjectFiles {
    val directories = DirsLookup.lookup(project, filters)
    val reportFiles = files(binaryReports(extension))
    return ProjectFiles(reportFiles, directories.sources, directories.outputs)
}

internal fun Project.instrumentedTasks(extension: KoverProjectConfig): List<Test> {
    return tasks.withType(Test::class.java).asSequence()
        // task can be disabled in the project extension
        .filterNot { extension.instrumentation.excludeTasks.contains(it.name) }
        .filterNot { t -> t.extensions.getByType(KoverTaskExtension::class.java).isDisabled.get() }
        .toList()
}

private fun Project.instrumentedTasksProvider(extension: KoverProjectConfig): Provider<List<Test>> {
    return provider { instrumentedTasks(extension) }
}


private fun Project.binaryReports(extension: KoverProjectConfig): List<File> {
    if (extension.isDisabled.get()) {
        return emptyList()
    }

    return tasks.withType(Test::class.java).asSequence()
        // task can be disabled in the project extension
        .filterNot { extension.instrumentation.excludeTasks.contains(it.name) }
        .map { t -> t.extensions.getByType(KoverTaskExtension::class.java) }
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
