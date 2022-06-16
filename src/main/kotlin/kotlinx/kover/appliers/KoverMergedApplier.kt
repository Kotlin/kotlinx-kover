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
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.*
import org.gradle.configurationcache.extensions.*

internal fun Project.applyMerged() {
    val extension = createMergedExtension()
    afterEvaluate(ProcessMergeExtensionAction(extension))
}

private fun Project.createMergedExtension(): KoverMergedConfig {
    val extension = extensions.create(KoverNames.MERGED_EXTENSION_NAME, KoverMergedConfig::class.java, objects)
    extension.isEnabled.set(false)
    extension.xmlReport.onCheck.set(false)
    extension.xmlReport.reportFile.set(layout.buildDirectory.file(MERGED_XML_REPORT_DEFAULT_PATH))
    extension.xmlReport.classes.set(extension.filters.classes)
    extension.htmlReport.onCheck.set(false)
    extension.htmlReport.reportDir.set(layout.buildDirectory.dir(MERGED_HTML_REPORT_DEFAULT_PATH))
    extension.htmlReport.classes.set(extension.filters.classes)
    extension.verify.onCheck.set(false)
    return extension
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
            extension.xmlReport.classes,
            extensionByProject,
            engineProvider,
            testsProvider,
            { e -> e.xmlReport.taskFilters.sourceSets.get() }
        ) {
            it.reportFile.set(extension.xmlReport.reportFile)
            it.description = "Generates code coverage XML report for all enabled test tasks in specified projects."
        }

        val htmlTask = container.createMergedTask<KoverHtmlTask>(
            MERGED_HTML_REPORT_TASK_NAME,
            extension.htmlReport.classes,
            extensionByProject,
            engineProvider,
            testsProvider,
            { e -> e.htmlReport.taskFilters.sourceSets.get() }
        ) {
            it.reportDir.set(extension.htmlReport.reportDir)
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
            it.rules.set(extension.verify.rules)
            it.resultFile.set(container.layout.buildDirectory.file(MERGED_VERIFICATION_REPORT_DEFAULT_PATH))
            it.description = "Verifies code coverage metrics of specified projects based on specified rules."
        }
        // TODO `onlyIf` block moved out from config lambda because of bug in Kotlin compiler - it implicitly adds closure on `Project` inside onlyIf's lambda
        verifyTask.onlyIf { extension.verify.hasActiveRules() }


        container.tasks.create(MERGED_REPORT_TASK_NAME) {
            it.group = VERIFICATION_GROUP
            it.dependsOn(xmlTask)
            it.dependsOn(htmlTask)
            it.description = "Generates code coverage HTML and XML reports for all enabled test tasks in one project."
        }

        container.tasks.configureEach {
            if (it.name == CHECK_TASK_NAME) {
                it.dependsOn(container.provider {
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
}

private inline fun <reified T : KoverReportTask> Project.createMergedTask(
    taskName: String,
    classFilters: Provider<KoverClassFilters>,
    extensionByProject: Provider<Map<Project, KoverProjectConfig>>,
    engineProvider: Provider<EngineDetails>,
    testsProvider: Provider<List<Test>>,
    crossinline filterExtractor: (KoverProjectConfig) -> KoverSourceSetFilters,
    crossinline block: (T) -> Unit
): T {
    val task = tasks.create(taskName, T::class.java) {
        it.files.set(mergedFilesProvider(extensionByProject, filterExtractor))
        it.engine.set(engineProvider)
        it.dependsOn(testsProvider)

        it.classFilters.set(classFilters)
        it.group = VERIFICATION_GROUP
        block(it)
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
            projects.filter { it.extensions.findByType(KoverProjectConfig::class.java) == null }.map { it.path }
        if (notAppliedProjects.isNotEmpty()) {
            throw GradleException("Can't create Kover merge tasks: Kover plugin not applied in projects $notAppliedProjects")
        }
        projects.associateWith { it.extensions.getByType(KoverProjectConfig::class.java) }
    }
}


private inline fun Project.mergedFilesProvider(
    extensionByProject: Provider<Map<Project, KoverProjectConfig>>,
    crossinline filterExtractor: (KoverProjectConfig) -> KoverSourceSetFilters
): Provider<Map<String, ProjectFiles>> {
    return provider {
        extensionByProject.get()
            .map { (project, extension) -> project.path to project.projectFiles(filterExtractor(extension), extension) }
            .associate { it }
    }
}


private fun filterProjects(filters: KoverProjectsFilters, allProjects: Iterable<Project>): List<Project> {
    if (filters.includes.isEmpty()) {
        return allProjects.toList()
    }

    val projectsByPath = allProjects.associateBy { p -> p.path }
    val pathsByName = allProjects.associate { it.name to mutableListOf<String>() }
    allProjects.forEach { pathsByName.getValue(it.name) += it.path }

    return filters.includes.map {
        if (it.startsWith(':')) {
            projectsByPath[it]
                ?: throw GradleException("Kover configuring error: not found project '$it' for merged tasks")
        } else {
            val paths = pathsByName[it]
                ?: throw GradleException("Kover configuring error: not found project '$it' for merged tasks")
            if (paths.size > 1) {
                throw GradleException("Kover configuring error: ambiguous name of the project '$it' for merged tasks: suitable projects with paths $paths. Consider using fully-qualified name starting with ':'")
            }
            projectsByPath[paths[0]]!!
        }
    }
}


private fun Project.engineProvider(extensionByProject: Provider<Map<Project, KoverProjectConfig>>): Provider<EngineDetails> {
    val archiveOperations: ArchiveOperations = this.serviceOf()
    // configuration already created in all projects at this moment because merge tasks creates in afterEvaluate step
    val config = configurations.getByName(CONFIGURATION_NAME)
    // the plugin is always applied to the containing project
    val containerEngine = extensions.getByType(KoverProjectConfig::class.java).engine

    val containerPath = path
    return provider {
        val variants = extensionByProject.get().map { ComparableEngineVariant(it.value.engine.get()) }.toSet()
        // check same engine variants are used
        if (variants.size > 1) {
            throw GradleException("Can't create Kover merge tasks: different coverage engines are used in projects")
        }
        val variant = variants.first()
        if (variant != ComparableEngineVariant(containerEngine.get())) {
            throw GradleException("Can't create Kover merge tasks: project engines are different from the engine from the containing project '$containerPath'")
        }
        engineByVariant(variant, config, archiveOperations)
    }
}


private fun Project.instrumentedTasksProvider(extensionByProject: Provider<Map<Project, KoverProjectConfig>>): Provider<List<Test>> {
    return provider { extensionByProject.get().flatMap { it.key.instrumentedTasks(it.value) } }
}


/**
 * Wrapper for [CoverageEngineVariant] to group applied engines.
 */
private class ComparableEngineVariant(origin: CoverageEngineVariant) : CoverageEngineVariant {
    override val vendor: CoverageEngineVendor = origin.vendor
    override val version: String = origin.version

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ComparableEngineVariant

        if (vendor != other.vendor) return false
        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vendor.hashCode()
        result = 31 * result + version.hashCode()
        return result
    }
}
