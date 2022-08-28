/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.appliers

import kotlinx.kover.api.*
import kotlinx.kover.api.KoverNames.CONFIGURATION_ENGINE_NAME
import kotlinx.kover.api.KoverNames.HTML_REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.VERIFY_TASK_NAME
import kotlinx.kover.api.KoverNames.XML_REPORT_TASK_NAME
import kotlinx.kover.engines.commons.EngineManager
import kotlinx.kover.lookup.DirsLookup
import kotlinx.kover.tasks.*
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.FileTree
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*

internal class KoverProjectApplier(
    private val objects: ObjectFactory,
    private val providers: ProviderFactory,
    private val archiveOperations: ArchiveOperations,
    private val layout: ProjectLayout,

    private val sourceFilesProducer: NamedDomainObjectProvider<Configuration>,
    private val binaryReportFilesProducer: NamedDomainObjectProvider<Configuration>,
) {

    fun applyToProject(target: Project) = with(target) {
        val extension = createProjectExtension()
        val config = createEngineConfig(extension.engine)

        val engineProvider = engineProvider(config, extension.engine)

        tasks.withType<Test>().configureEach {
            applyToTestTask(extension, engineProvider, objects, layout)
        }

        val instrumentedTasks = instrumentedTasks(extension)

        val xmlTask = createTask<KoverXmlTask>(
            XML_REPORT_TASK_NAME,
            extension.xmlReport.filters,
            extension,
            engineProvider,
            instrumentedTasks,
        ) {
            it.reportFile.convention(extension.xmlReport.reportFile)
            it.description = "Generates code coverage XML report for all enabled test tasks in one project."
        }

        val htmlTask = createTask<KoverHtmlTask>(
            HTML_REPORT_TASK_NAME,
            extension.htmlReport.taskFilters,
            extension,
            engineProvider,
            instrumentedTasks,
        ) {
            it.reportDir.convention(extension.htmlReport.reportDir)
            it.description = "Generates code coverage HTML report for all enabled test tasks in one project."
        }

        val verifyTask = createTask<KoverVerificationTask>(
            VERIFY_TASK_NAME,
            extension.filters,
            extension,
            engineProvider,
            instrumentedTasks,
        ) {
            it.rules.convention(extension.verify.rules)
            it.resultFile.convention(layout.buildDirectory.file(KoverPaths.PROJECT_VERIFICATION_REPORT_DEFAULT_PATH))
            it.description = "Verifies code coverage metrics of one project based on specified rules."

            it.onlyIf { t -> (t as? KoverVerificationTask)?.rules?.orNull?.hasActiveRules() == true }

            // ordering of task calls, so that if a verification error occurs, reports are generated and the values can be viewed in them
            it.shouldRunAfter(xmlTask, htmlTask)
        }
        // TODO `onlyIf` block moved out from config lambda because of bug in Kotlin compiler - it implicitly adds closure on `Project` inside onlyIf's lambda
//        it.onlyIf { extension.verify.hasActiveRules() }


        val koverReportTask = tasks.create(KoverNames.REPORT_TASK_NAME) {
            group = KoverNames.VERIFICATION_GROUP
            dependsOn(xmlTask)
            dependsOn(htmlTask)
            description = "Generates code coverage HTML and XML reports for all enabled test tasks in one project."
        }

        val koverReportTasks = tasks.withType<KoverReportTask>()

        sourceFilesProducer.configure {
            // TODO fix underlying concurrent list modification error, and remove .toList() workaround
            koverReportTasks.toList().forEach { task ->
                task.files.sources.files.toList().forEach { file ->
                    outgoing.artifact(file) {
                        builtBy(task)
                    }
                }
            }
        }

        binaryReportFilesProducer.configure {
            koverReportTasks.forEach { task ->
                logger.lifecycle("binaryReportFilesConsumer ${task.name}")
                logger.lifecycle("binaryReportFilesConsumer ${task.name} ${task.files.binaryReportFiles.files.joinToString()}")
                task.files.binaryReportFiles.files.forEach { file ->
                    logger.lifecycle("binaryReportFilesConsumer getting $file")
                    outgoing.artifact(file) { builtBy(task) }
                }
            }
        }

        tasks.matching { it.name == KoverNames.CHECK_TASK_NAME }
            .configureEach {
                dependsOn(koverReportTask)
            }

//    tasks
//        .matching { it.name == KoverNames.CHECK_TASK_NAME }
//        .configureEach {
//            dependsOn(provider {
//                // don't add dependency if Kover is disabled
//                if (extension.isDisabled.get()) {
//                    return@provider emptyList<Task>()
//                }
//
//                val tasks = mutableListOf<Task>()
//                if (extension.xmlReport.onCheck.get()) {
//                    tasks += xmlTask
//                }
//                if (extension.htmlReport.onCheck.get()) {
//                    tasks += htmlTask
//                }
//                if (extension.verify.onCheck.get() && extension.verify.hasActiveRules()) {
//                    // don't add dependency if there is no active verification rules https://github.com/Kotlin/kotlinx-kover/issues/168
//                    tasks += verifyTask
//                }
//                tasks
//            })
//        }
    }

    private inline fun <reified T : KoverReportTask> Project.createTask(
        taskName: String,
        filters: KoverProjectFilters,
        extension: KoverProjectConfig,
        engineProvider: Provider<EngineDetails>,
        instrumentedTasks: TaskCollection<Test>,
        crossinline block: (T) -> Unit
    ): Provider<T> {
        val dirsLookup = filters.sourceSets.map {
            DirsLookup.lookup(project, it)
        }
        val task = tasks.register<T>(taskName) {

            // TODO fix 'path must not be null/empty' error when collecting these reports
//            files.binaryReportFiles.from(
//                { binaryReports(extension) }
//            )
            files.sources.from(dirsLookup.map { it.sources })
            files.outputs.from(dirsLookup.map { it.outputs })
//            files.put(path, projectFilesProvider(extension, filters.sourceSets))
            engine.convention(engineProvider)
            dependsOn(instrumentedTasks)
            classFilter.convention(filters.classes)
            group = KoverNames.VERIFICATION_GROUP
            block(this)

            enabled = extension.isDisabled.get()
        }

        // TODO `onlyIf` block moved out from config lambda because of bug in Kotlin compiler - it implicitly adds closure on `Project` inside onlyIf's lambda
        // execute task only if Kover not disabled for project
//        task.onlyIf { !extension.isDisabled.get() }
        // execute task only if there is at least one binary report
//        task.onlyIf { t -> (t as KoverReportTask).files.get().any { f -> !f.value.binaryReportFiles.isEmpty } }

        logger.lifecycle("[KoverProjectApplier] finished registering task ${task.name}")

        return task
    }

    private fun Project.createProjectExtension(): KoverProjectConfig {
        return extensions.create(KoverNames.PROJECT_EXTENSION_NAME, KoverProjectConfig::class).apply {
            // default values
            isDisabled.convention(false)
            engine.convention(DefaultIntellijEngine)
            filters.classes.convention(KoverClassFilter())
            filters.sourceSets.convention(KoverSourceSetFilter())
            xmlReport.reportFile.convention(layout.buildDirectory.file(KoverPaths.PROJECT_XML_REPORT_DEFAULT_PATH))
            xmlReport.onCheck.convention(false)
            xmlReport.filters.classes.convention(filters.classes)
            xmlReport.filters.sourceSets.convention(filters.sourceSets)
            htmlReport.reportDir.convention(layout.buildDirectory.dir(KoverPaths.PROJECT_HTML_REPORT_DEFAULT_PATH))
            htmlReport.onCheck.convention(false)
            htmlReport.taskFilters.classes.convention(filters.classes)
            htmlReport.taskFilters.sourceSets.convention(filters.sourceSets)
            verify.onCheck.convention(true)
        }
    }

    private fun Project.createEngineConfig(engineVariantProvider: Provider<CoverageEngineVariant>): Configuration {
        val config = project.configurations.create(CONFIGURATION_ENGINE_NAME)
        config.isVisible = false
        config.isTransitive = true
        config.description = "Kotlin Kover Plugin configuration for Coverage Engine"

        config.defaultDependencies {
            val variant = engineVariantProvider.get()
            EngineManager.dependencies(variant).forEach {
                add(dependencies.create(it))
            }
        }
        return config
    }

    private fun Project.engineProvider(
        config: Configuration,
        engineVariantProvider: Provider<CoverageEngineVariant>
    ): Provider<EngineDetails> {
        return project.provider { engineByVariant(engineVariantProvider.get(), config, archiveOperations) }
    }

//    internal fun Project.projectFilesProvider(
//        extension: KoverProjectConfig,
//        sourceSetFiltersProvider: Provider<KoverSourceSetFilter>
//    ): Provider<ProjectFiles> {
//        return provider { projectFiles(sourceSetFiltersProvider.get(), extension) }
//    }

//    internal fun Project.projectFiles(
//        filters: KoverSourceSetFilter,
//        extension: KoverProjectConfig
//    ): ProjectFiles {
//        val directories = DirsLookup.lookup(project, filters)
//        val reportFiles = files(binaryReports(extension))
//        return ProjectFiles(reportFiles, directories.sources, directories.outputs)
//    }


    private fun Project.binaryReports(extension: KoverProjectConfig): FileTree {
        return fileTree(
            tasks
                .matching { extension.isDisabled.orNull == false }
                .withType<Test>()
                .matching {
                    val ext = it.extensions.findByType<KoverTaskExtension>()

                    when {
                        it.name in extension.instrumentation.excludeTasks -> false
                        ext == null -> false
                        ext.disabled.orNull == true -> false
                        ext.reportFile.orNull?.asFile?.exists() == false -> false

                        else -> true
                    }
                }.map {
                    it.extensions.getByType<KoverTaskExtension>().reportFile.asFile
                }
        )
    }

//    private fun Project.binaryReports(extension: KoverProjectConfig): List<File> {
//        if (extension.isDisabled.get()) {
//            return emptyList()
//        }
//
//        return tasks.withType<Test>().asSequence()
//            // task can be disabled in the project extension
//            .filterNot { extension.instrumentation.excludeTasks.contains(it.name) }
//            .map { t -> t.extensions.getByType<KoverTaskExtension>() }
//            // process binary report only from tasks with enabled cover
//            .filterNot { e -> e.isDisabled.get() }
//            .map { e -> e.reportFile.get().asFile }
//            // process binary report only from tasks with sources
//            .filter { f -> f.exists() }
//            .toList()
//    }


    internal fun KoverVerifyConfig.hasActiveRules(): Boolean {
        return rules.get().hasActiveRules()
    }

    internal fun List<VerificationRule>.hasActiveRules(): Boolean =
        any { rule -> rule.isEnabled }

}


internal fun engineByVariant(
    variant: CoverageEngineVariant,
    config: Configuration,
    archiveOperations: ArchiveOperations
): EngineDetails {
    val jarFile = EngineManager.findJarFile(variant, config, archiveOperations)
    return EngineDetails(variant, jarFile, config)
}

internal fun Project.instrumentedTasks(extension: KoverProjectConfig): TaskCollection<Test> {
    return tasks.withType<Test>()
        // task can be disabled in the project extension
        .matching { it.name !in extension.instrumentation.excludeTasks }
        .matching { it.extensions.findByType<KoverTaskExtension>()?.disabled?.orNull == true }
}
