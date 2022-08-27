/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.appliers

import kotlinx.kover.api.*
import kotlinx.kover.api.KoverNames.CHECK_TASK_NAME
import kotlinx.kover.api.KoverNames.CONFIGURATION_NAME
import kotlinx.kover.api.KoverNames.MERGED_HTML_REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.MERGED_REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.MERGED_VERIFY_TASK_NAME
import kotlinx.kover.api.KoverNames.MERGED_XML_REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.VERIFICATION_GROUP
import kotlinx.kover.api.KoverPaths.MERGED_HTML_REPORT_DEFAULT_PATH
import kotlinx.kover.api.KoverPaths.MERGED_VERIFICATION_REPORT_DEFAULT_PATH
import kotlinx.kover.api.KoverPaths.MERGED_XML_REPORT_DEFAULT_PATH
import kotlinx.kover.tasks.*
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.configurationcache.extensions.serviceOf
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType

internal fun Project.applyMerged() {
    val extension = createMergedExtension()
    afterEvaluate(ProcessMergeExtensionAction(extension))
}

private fun Project.createMergedExtension(): KoverMergedConfig {
    return extensions.create<KoverMergedConfig>(KoverNames.MERGED_EXTENSION_NAME).apply {
        isEnabled.convention(false)
        filters.classes.convention(KoverClassFilter())
        filters.projects.convention(KoverProjectsFilter())
        xmlReport.onCheck.convention(false)
        xmlReport.reportFile.convention(layout.buildDirectory.file(MERGED_XML_REPORT_DEFAULT_PATH))
        xmlReport.classFilter.convention(filters.classes)
        htmlReport.onCheck.convention(false)
        htmlReport.reportDir.convention(layout.buildDirectory.dir(MERGED_HTML_REPORT_DEFAULT_PATH))
        htmlReport.classFilter.convention(filters.classes)
        verify.onCheck.convention(false)
    }
}

private class ProcessMergeExtensionAction(private val extension: KoverMergedConfig) : Action<Project> {
    override fun execute(container: Project) {
        // don't create tasks if merge wasn't enabled
        if (!extension.isEnabled.get()) {
            return
        }

        val extensionByProject = container.projectsExtensionsProvider(extension, container.allprojects)
        val engineProvider = container.engineProvider(extensionByProject)
        val testsProvider = container.instrumentedTasksProvider(extensionByProject)

        val xmlTask = container.createMergedTask<KoverXmlTask>(
            MERGED_XML_REPORT_TASK_NAME,
            extension.xmlReport.classFilter,
            extensionByProject,
            engineProvider,
            testsProvider,
            { e -> e.xmlReport.filters.sourceSets.get() }
        ) {
            it.reportFile.convention(extension.xmlReport.reportFile)
            it.description = "Generates code coverage XML report for all enabled test tasks in specified projects."
        }

        val htmlTask = container.createMergedTask<KoverHtmlTask>(
            MERGED_HTML_REPORT_TASK_NAME,
            extension.htmlReport.classFilter,
            extensionByProject,
            engineProvider,
            testsProvider,
            { e -> e.htmlReport.taskFilters.sourceSets.get() }
        ) {
            it.reportDir.convention(extension.htmlReport.reportDir)
            it.description = "Generates code coverage HTML report for all enabled test tasks in specified projects."
        }

        val verifyTask = container.createMergedTask<KoverVerificationTask>(
            MERGED_VERIFY_TASK_NAME,
            extension.filters.classes,
            extensionByProject,
            engineProvider,
            testsProvider,
            { e -> e.filters.sourceSets.get() }
        ) {
            it.rules.convention(extension.verify.rules)
            it.resultFile.convention(container.layout.buildDirectory.file(MERGED_VERIFICATION_REPORT_DEFAULT_PATH))
            it.description = "Verifies code coverage metrics of specified projects based on specified rules."
        }
        // TODO `onlyIf` block moved out from config lambda because of bug in Kotlin compiler - it implicitly adds closure on `Project` inside onlyIf's lambda
        verifyTask.onlyIf { extension.verify.hasActiveRules() }

        // ordering of task calls, so that if a verification error occurs, reports are generated and the values can be viewed in them
        verifyTask.shouldRunAfter(xmlTask, htmlTask)

        container.tasks.create(MERGED_REPORT_TASK_NAME) {
            group = VERIFICATION_GROUP
            dependsOn(xmlTask)
            dependsOn(htmlTask)
            description = "Generates code coverage HTML and XML reports for all enabled test tasks in one project."
        }

        container.tasks
            .matching { it.name == CHECK_TASK_NAME }
            .configureEach {
                dependsOn(container.provider {
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

private inline fun <reified T : KoverReportTask> Project.createMergedTask(
    taskName: String,
    classFilter: Provider<KoverClassFilter>,
    extensionByProject: Provider<Map<Project, KoverProjectConfig>>,
    engineProvider: Provider<EngineDetails>,
    testsProvider: Provider<List<Test>>,
    crossinline filterExtractor: (KoverProjectConfig) -> KoverSourceSetFilter,
    crossinline block: (T) -> Unit
): T {
    val task = tasks.create<T>(taskName) {
        files.convention(mergedFilesProvider(extensionByProject, filterExtractor))
        engine.convention(engineProvider)
        dependsOn(testsProvider)

        this@create.classFilter.convention(classFilter)
        group = VERIFICATION_GROUP
        block(this)
    }
    // TODO `onlyIf` block moved out from config lambda because of bug in Kotlin compiler - it implicitly adds closure on `Project` inside onlyIf's lambda
    // execute task only if there is at least one binary report
    task.onlyIf { t -> (t as KoverReportTask).files.get().any { f -> !f.value.binaryReportFiles.isEmpty } }

    return task
}

private fun Project.projectsExtensionsProvider(
    extension: KoverMergedConfig,
    allProjects: Iterable<Project>
): Provider<Map<Project, KoverProjectConfig>> {
    return provider {
        val projects = filterProjects(extension.filters.projects.get(), allProjects)

        val notAppliedProjects =
            projects.filter { it.extensions.findByType<KoverProjectConfig>() == null }.map { it.path }
        if (notAppliedProjects.isNotEmpty()) {
            throw GradleException("Can't create Kover merge tasks: Kover plugin not applied in projects $notAppliedProjects")
        }
        projects.associateWith { it.extensions.getByType<KoverProjectConfig>() }
    }
}


private inline fun Project.mergedFilesProvider(
    extensionByProject: Provider<Map<Project, KoverProjectConfig>>,
    crossinline filterExtractor: (KoverProjectConfig) -> KoverSourceSetFilter
): Provider<Map<String, ProjectFiles>> {
    return extensionByProject.map { extension ->
        extension.map { (project, extension) ->
            project.path to project.projectFiles(
                filterExtractor(extension),
                extension
            )
        }.associate { it }
    }
}


private fun filterProjects(filters: KoverProjectsFilter, allProjects: Iterable<Project>): List<Project> {

    val actualProjects = allProjects.filter { it.buildFile.exists() }

    if (filters.excludes.isEmpty()) {
        return actualProjects.toList()
    }

    val projectsByPath = actualProjects.associateBy { p -> p.path }.toMutableMap()
    val pathsByName = actualProjects.associate { it.name to mutableListOf<String>() }
    actualProjects.forEach { pathsByName.getValue(it.name) += it.path }

    val excludedPaths = filters.excludes.map {
        if (it.startsWith(':')) {
            projectsByPath[it]
                ?: throw GradleException("Kover configuring error: not found project '$it' for merged tasks")
            it
        } else {
            val paths = pathsByName[it]
                ?: throw GradleException("Kover configuring error: not found project '$it' for merged tasks")
            if (paths.size > 1) {
                throw GradleException("Kover configuring error: ambiguous name of the project '$it' for merged tasks: suitable projects with paths $paths. Consider using fully-qualified name starting with ':'")
            }
            paths[0]
        }
    }.toSet()

    return projectsByPath.filterNot { it.key in excludedPaths }.map { it.value }
}


private fun Project.engineProvider(extensionByProject: Provider<Map<Project, KoverProjectConfig>>): Provider<EngineDetails> {
    val archiveOperations: ArchiveOperations = this.serviceOf()
    // configuration already created in all projects at this moment because merge tasks creates in afterEvaluate step
    val config = configurations.getByName(CONFIGURATION_NAME)
    // the plugin is always applied to the containing project
    val containerEngine = extensions.getByType<KoverProjectConfig>().engine

    val containerPath = path
    return provider {
        val map: MutableMap<CoverageEngineVariant, MutableList<String>> = mutableMapOf()
        extensionByProject.get().forEach { (p, v) ->
            map.computeIfAbsent(v.engine.get()) { mutableListOf() } += p.path
        }

        // check same engine variants are used
        if (map.size > 1) {
            throw GradleException("Can't create Kover merge tasks: different coverage engines are used in projects.\n\tProjects by engines: $map")
        }
        val variant = map.keys.first()
        if (variant != containerEngine.get()) {
            throw GradleException("Can't create Kover merge tasks: child projects engines '$variant' are different from the engine '${containerEngine.get()}' from the containing project '$containerPath'")
        }
        engineByVariant(variant, config, archiveOperations)
    }
}


private fun Project.instrumentedTasksProvider(extensionByProject: Provider<Map<Project, KoverProjectConfig>>): Provider<List<Test>> {
    return provider { extensionByProject.get().flatMap { it.key.instrumentedTasks(it.value) } }
}
