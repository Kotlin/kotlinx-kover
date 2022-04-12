/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover

import kotlinx.kover.adapters.*
import kotlinx.kover.api.*
import kotlinx.kover.api.KoverPaths.MERGED_HTML_REPORT_DEFAULT_PATH
import kotlinx.kover.api.KoverNames.CHECK_TASK_NAME
import kotlinx.kover.api.KoverNames.COLLECT_REPORTS_TASK_NAME
import kotlinx.kover.api.KoverNames.MERGED_HTML_REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.HTML_REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.VERIFY_TASK_NAME
import kotlinx.kover.api.KoverNames.MERGED_REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.XML_REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.ROOT_EXTENSION_NAME
import kotlinx.kover.api.KoverNames.TASK_EXTENSION_NAME
import kotlinx.kover.api.KoverNames.VERIFICATION_GROUP
import kotlinx.kover.api.KoverNames.MERGED_VERIFY_TASK_NAME
import kotlinx.kover.api.KoverNames.MERGED_XML_REPORT_TASK_NAME
import kotlinx.kover.api.KoverPaths.ALL_PROJECTS_REPORTS_DEFAULT_PATH
import kotlinx.kover.api.KoverPaths.PROJECT_HTML_REPORT_DEFAULT_PATH
import kotlinx.kover.api.KoverPaths.MERGED_XML_REPORT_DEFAULT_PATH
import kotlinx.kover.api.KoverPaths.PROJECT_XML_REPORT_DEFAULT_PATH
import kotlinx.kover.engines.commons.*
import kotlinx.kover.engines.commons.CoverageAgent
import kotlinx.kover.engines.intellij.*
import kotlinx.kover.tasks.*
import org.gradle.api.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.testing.*
import org.gradle.process.*
import java.io.File
import kotlin.reflect.*

class KoverPlugin : Plugin<Project> {
    private val defaultJacocoVersion = "0.8.7"

    override fun apply(target: Project) {
        val koverExtension = target.createKoverExtension()
        val agents = AgentsFactory.createAgents(target, koverExtension)

        val providers = target.createProviders(agents)

        target.allprojects {
            it.applyToProject(providers, agents)
        }
        target.createCollectingTask()

        target.createMergedTasks(providers)
    }

    private fun Project.applyToProject(providers: BuildProviders, agents: Map<CoverageEngine, CoverageAgent>) {
        val projectProviders =
            providers.projects[path]
                ?: throw GradleException("Kover: Providers for project '$name' ('$path') was not found")

        val xmlReportTask = createKoverProjectTask(
            XML_REPORT_TASK_NAME,
            KoverXmlReportTask::class,
            providers,
            projectProviders
        ) {
            it.xmlReportFile.set(layout.buildDirectory.file(PROJECT_XML_REPORT_DEFAULT_PATH))
            it.description = "Generates code coverage XML report for all enabled test tasks in one project."
        }

        val htmlReportTask = createKoverProjectTask(
            HTML_REPORT_TASK_NAME,
            KoverHtmlReportTask::class,
            providers,
            projectProviders
        ) {
            it.htmlReportDir.set(it.project.layout.buildDirectory.dir(PROJECT_HTML_REPORT_DEFAULT_PATH))
            it.description = "Generates code coverage HTML report for all enabled test tasks in one project."
        }

        val verifyTask = createKoverProjectTask(
            VERIFY_TASK_NAME,
            KoverVerificationTask::class,
            providers,
            projectProviders
        ) {
            it.onlyIf { t -> (t as KoverVerificationTask).rules.isNotEmpty() }
            it.description = "Verifies code coverage metrics of one project based on specified rules."
        }

        tasks.create(REPORT_TASK_NAME) {
            it.group = VERIFICATION_GROUP
            it.dependsOn(xmlReportTask)
            it.dependsOn(htmlReportTask)
            it.description = "Generates code coverage HTML and XML reports for all enabled test tasks in one project."
        }

        tasks.configureEach {
            if (it.name == CHECK_TASK_NAME) {
                it.dependsOn(verifyTask)
            }
        }

        tasks.withType(Test::class.java).configureEach { t ->
            t.configTestTask(providers, agents)
        }
    }

    private fun Project.createMergedTasks(providers: BuildProviders) {
        val xmlReportTask = createKoverMergedTask(
            MERGED_XML_REPORT_TASK_NAME,
            KoverMergedXmlReportTask::class,
            providers
        ) {
            it.xmlReportFile.set(layout.buildDirectory.file(MERGED_XML_REPORT_DEFAULT_PATH))
            it.description = "Generates code coverage XML report for all enabled test tasks in all projects."
        }

        val htmlReportTask = createKoverMergedTask(
            MERGED_HTML_REPORT_TASK_NAME,
            KoverMergedHtmlReportTask::class,
            providers
        ) {
            it.htmlReportDir.set(layout.buildDirectory.dir(MERGED_HTML_REPORT_DEFAULT_PATH))
            it.description = "Generates code coverage HTML report for all enabled test tasks in all projects."
        }

        val reportTask = tasks.create(MERGED_REPORT_TASK_NAME) {
            it.group = VERIFICATION_GROUP
            it.dependsOn(xmlReportTask)
            it.dependsOn(htmlReportTask)
            it.description = "Generates code coverage HTML and XML reports for all enabled test tasks in all projects."
        }

        val verifyTask = createKoverMergedTask(
            MERGED_VERIFY_TASK_NAME,
            KoverMergedVerificationTask::class,
            providers
        ) {
            it.onlyIf { t -> (t as KoverMergedVerificationTask).rules.isNotEmpty() }
            it.description = "Verifies code coverage metrics of all projects based on specified rules."
        }

        tasks.configureEach {
            if (it.name == CHECK_TASK_NAME) {
                it.dependsOn(provider {
                    val koverExtension = extensions.getByType(KoverExtension::class.java)
                    if (koverExtension.generateReportOnCheck) {
                        listOf(reportTask, verifyTask)
                    } else {
                        listOf(verifyTask)
                    }
                })
            }
        }
    }


    private fun <T : KoverMergedTask> Project.createKoverMergedTask(
        taskName: String,
        type: KClass<T>,
        providers: BuildProviders,
        block: (T) -> Unit
    ): T {
        val task = tasks.create(taskName, type.java)

        task.group = VERIFICATION_GROUP

        providers.projects.forEach { (projectPath, m) ->
            task.binaryReportFiles.put(projectPath, NestedFiles(task.project.objects, m.reports))
            task.srcDirs.put(projectPath, NestedFiles(task.project.objects, m.sources))
            task.outputDirs.put(projectPath, NestedFiles(task.project.objects, m.output))
        }

        task.coverageEngine.set(providers.engine)
        task.classpath.set(providers.classpath)
        task.dependsOn(providers.merged.tests)

        val disabledProvider = providers.merged.disabled
        task.onlyIf { !disabledProvider.get() }

        block(task)
        return task
    }

    private fun Project.createCollectingTask() {
        tasks.create(COLLECT_REPORTS_TASK_NAME, KoverCollectingTask::class.java) { task ->
            task.group = VERIFICATION_GROUP
            task.description = "Collects all projects reports into one directory."
            task.outputDir.set(project.layout.buildDirectory.dir(ALL_PROJECTS_REPORTS_DEFAULT_PATH))
            // disable UP-TO-DATE check for task: it will be executed every time
            task.outputs.upToDateWhen { false }

            allprojects { proj ->
                val xmlReportTask =
                    proj.tasks.withType(KoverXmlReportTask::class.java).getByName(XML_REPORT_TASK_NAME)
                val htmlReportTask =
                    proj.tasks.withType(KoverHtmlReportTask::class.java).getByName(HTML_REPORT_TASK_NAME)

                task.mustRunAfter(xmlReportTask)
                task.mustRunAfter(htmlReportTask)

                task.xmlFiles[proj.path] = xmlReportTask.xmlReportFile
                task.htmlDirs[proj.path] = htmlReportTask.htmlReportDir
            }
        }
    }


    private fun <T : KoverProjectTask> Project.createKoverProjectTask(
        taskName: String,
        type: KClass<T>,
        providers: BuildProviders,
        projectProviders: ProjectProviders,
        block: (T) -> Unit
    ): T {
        tasks.findByName(taskName)?.let {
            throw GradleException("Kover task '$taskName' already exist. Plugin should not be applied in child project if it has already been applied in one of the parent projects.")
        }

        val task = tasks.create(taskName, type.java)
        task.group = VERIFICATION_GROUP

        task.coverageEngine.set(providers.engine)
        task.classpath.set(providers.classpath)
        task.srcDirs.set(projectProviders.sources)
        task.outputDirs.set(projectProviders.output)

        // it is necessary to read all binary reports because project's classes can be invoked in another project
        task.binaryReportFiles.set(projectProviders.reports)
        task.dependsOn(projectProviders.tests)

        val disabledProvider = projectProviders.disabled
        task.onlyIf { !disabledProvider.get() }
        task.onlyIf { !(it as KoverProjectTask).binaryReportFiles.get().isEmpty }

        block(task)

        return task
    }

    private fun Project.createKoverExtension(): KoverExtension {
        val extension = extensions.create(ROOT_EXTENSION_NAME, KoverExtension::class.java, objects)
        extension.isDisabled = false
        extension.coverageEngine.set(CoverageEngine.INTELLIJ)
        extension.intellijEngineVersion.set(defaultIntellijVersion.toString())
        extension.jacocoEngineVersion.set(defaultJacocoVersion)

        afterEvaluate(CollectDisabledProjectsPathsAction(extension))

        return extension
    }

    private fun Test.configTestTask(
        providers: BuildProviders,
        agents: Map<CoverageEngine, CoverageAgent>
    ) {
        val taskExtension = extensions.create(TASK_EXTENSION_NAME, KoverTaskExtension::class.java, project.objects)

        taskExtension.isDisabled = false
        taskExtension.binaryReportFile.set(project.provider {
            val koverExtension = providers.koverExtension.get()
            val suffix = if (koverExtension.coverageEngine.get() == CoverageEngine.INTELLIJ) ".ic" else ".exec"
            project.layout.buildDirectory.get().file("kover/$name$suffix").asFile
        })

        val pluginContainer = project.plugins
        val excludeAndroidPackages =
            project.provider { pluginContainer.androidPluginIsApplied && !providers.koverExtension.get().instrumentAndroidPackage }

        jvmArgumentProviders.add(
            CoverageArgumentProvider(
                this,
                agents,
                taskExtension,
                providers.koverExtension,
                excludeAndroidPackages
            )
        )

        val sourceErrorProvider = project.provider {
            File(taskExtension.binaryReportFile.get().parentFile, "coverage-error.log")
        }
        val targetErrorProvider = project.layout.buildDirectory.file("kover/errors/$name.log").map { it.asFile }

        doFirst(BinaryReportCleanupAction(project.path, providers.koverExtension, taskExtension))
        doLast(MoveIntellijErrorLogAction(sourceErrorProvider, targetErrorProvider))
    }
}

/*
  To support parallel tests, both Coverage Engines work in append to data file mode.
  For this reason, before starting the tests, it is necessary to clear the file from the results of previous runs.
*/
private class BinaryReportCleanupAction(
    private val projectPath: String,
    private val koverExtensionProvider: Provider<KoverExtension>,
    private val taskExtension: KoverTaskExtension
) : Action<Task> {
    override fun execute(task: Task) {
        val koverExtension = koverExtensionProvider.get()
        val file = taskExtension.binaryReportFile.get()

        // always delete previous data file
        file.delete()

        if (!taskExtension.isDisabled
            && !koverExtension.isDisabled
            && !koverExtension.disabledProjectsPaths.contains(projectPath)
            && koverExtension.coverageEngine.get() == CoverageEngine.INTELLIJ
        ) {
            // IntelliJ engine expected empty file for parallel test execution.
            // Since it is impossible to know in advance whether the tests will be run in parallel, we always create an empty file.
            file.createNewFile()
        }
    }
}

private class CollectDisabledProjectsPathsAction(
    private val koverExtension: KoverExtension,
) : Action<Project> {
    override fun execute(project: Project) {
        val allProjects = project.allprojects
        val paths = allProjects.associate { it.name to mutableListOf<String>() }
        allProjects.forEach { paths.getValue(it.name) += it.path }

        val result: MutableSet<String> = mutableSetOf()

        koverExtension.disabledProjects.map {
            if (it.startsWith(':')) {
                result += it
            } else {
                val projectPaths = paths[it] ?: return@map
                if (projectPaths.size > 1) {
                    throw GradleException("Cover configuring error: ambiguous name of the excluded project '$it': suitable projects with paths $projectPaths")
                }
                result += projectPaths
            }
        }

        koverExtension.disabledProjectsPaths = result
    }
}

private class MoveIntellijErrorLogAction(
    private val sourceFile: Provider<File>,
    private val targetFile: Provider<File>
) : Action<Task> {
    override fun execute(task: Task) {
        val origin = sourceFile.get()
        if (origin.exists() && origin.isFile) {
            origin.copyTo(targetFile.get(), true)
            origin.delete()
        }
    }
}

private class CoverageArgumentProvider(
    private val task: Task,
    private val agents: Map<CoverageEngine, CoverageAgent>,
    @get:Nested val taskExtension: KoverTaskExtension,
    @get:Nested val koverExtension: Provider<KoverExtension>,
    @get:Input val excludeAndroidPackage: Provider<Boolean>
) : CommandLineArgumentProvider, Named {

    private val projectPath: String = task.project.path

    @Internal
    override fun getName(): String {
        return "koverArgumentsProvider"
    }

    override fun asArguments(): MutableIterable<String> {
        val koverExtensionValue = koverExtension.get()

        if (taskExtension.isDisabled
            || koverExtensionValue.isDisabled
            || koverExtensionValue.disabledProjectsPaths.contains(projectPath)
        ) {
            return mutableListOf()
        }

        if (excludeAndroidPackage.get()) {
            /*
            The instrumentation of android classes often causes errors when using third-party
            frameworks (see https://github.com/Kotlin/kotlinx-kover/issues/89).

            Because android classes are not part of the project, in any case they do not get into the report,
            and they can be excluded from instrumentation.

            FIXME Remove this code if the IntelliJ Agent stops changing project classes during instrumentation
             */
            taskExtension.excludes = taskExtension.excludes + "android.*" + "com.android.*"
        }

        return agents.getFor(koverExtensionValue.coverageEngine.get()).buildCommandLineArgs(task, taskExtension)
    }
}
