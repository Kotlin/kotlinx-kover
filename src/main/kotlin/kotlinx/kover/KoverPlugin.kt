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
import kotlin.reflect.*

class KoverPlugin : Plugin<Project> {
    private val defaultJacocoVersion = "0.8.7"

    override fun apply(target: Project) {
        target.checkAlreadyApplied()

        val koverExtension = target.createKoverExtension()
        val agents = AgentsFactory.createAgents(target, koverExtension)

        val providers = target.createProviders(agents)

        target.allprojects {
            it.applyToProject(providers, agents)
        }
        target.createCollectingTask()

        target.createMergedTasks(providers)
    }

    private fun Project.applyToProject(providers: AllProviders, agents: Map<CoverageEngine, CoverageAgent>) {
        val projectProviders =
            providers.projects[name] ?: throw GradleException("Kover: Providers for project '$name' was not found")

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
            t.configTest(providers, agents)
        }
    }

    private fun Project.createMergedTasks(providers: AllProviders) {
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
        providers: AllProviders,
        block: (T) -> Unit
    ): T {
        return tasks.create(taskName, type.java) { task ->
            task.group = VERIFICATION_GROUP

            providers.projects.forEach { (projectName, m) ->
                task.binaryReportFiles.put(projectName, NestedFiles(task.project.objects, m.reports))
                task.srcDirs.put(projectName, NestedFiles(task.project.objects, m.sources))
                task.outputDirs.put(projectName, NestedFiles(task.project.objects, m.output))
            }

            task.coverageEngine.set(providers.engine)
            task.classpath.set(providers.classpath)
            task.dependsOn(providers.merged.tests)

            block(task)
        }
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

                task.xmlFiles[proj.name] = xmlReportTask.xmlReportFile
                task.htmlDirs[proj.name] = htmlReportTask.htmlReportDir
            }
        }
    }


    private fun <T : KoverProjectTask> Project.createKoverProjectTask(
        taskName: String,
        type: KClass<T>,
        providers: AllProviders,
        projectProviders: ProjectProviders,
        block: (T) -> Unit
    ): T {
        return tasks.create(taskName, type.java) { task ->
            task.group = VERIFICATION_GROUP

            task.coverageEngine.set(providers.engine)
            task.classpath.set(providers.classpath)
            task.srcDirs.set(projectProviders.sources)
            task.outputDirs.set(projectProviders.output)

            // it is necessary to read all binary reports because project's classes can be invoked in another project
            task.binaryReportFiles.set(projectProviders.reports)
            task.dependsOn(projectProviders.tests)

            task.onlyIf { !projectProviders.disabled.get() }
            task.onlyIf { !task.binaryReportFiles.get().isEmpty }

            block(task)
        }
    }

    private fun Project.createKoverExtension(): KoverExtension {
        val extension = extensions.create(ROOT_EXTENSION_NAME, KoverExtension::class.java, objects)
        extension.isDisabled = false
        extension.coverageEngine.set(CoverageEngine.INTELLIJ)
        extension.intellijEngineVersion.set(defaultIntellijVersion.toString())
        extension.jacocoEngineVersion.set(defaultJacocoVersion)
        return extension
    }

    private fun Test.configTest(
        providers: AllProviders,
        agents: Map<CoverageEngine, CoverageAgent>
    ) {
        val taskExtension = extensions.create(TASK_EXTENSION_NAME, KoverTaskExtension::class.java, project.objects)

        taskExtension.isDisabled = false
        taskExtension.binaryReportFile.set(this.project.provider {
            val koverExtension = providers.koverExtension.get()
            val suffix = if (koverExtension.coverageEngine.get() == CoverageEngine.INTELLIJ) ".ic" else ".exec"
            project.layout.buildDirectory.get().file("kover/$name$suffix").asFile
        })

        val excludeAndroidPackages =
            project.provider { project.androidPluginIsApplied && !providers.koverExtension.get().instrumentAndroidPackage }

        jvmArgumentProviders.add(
            CoverageArgumentProvider(
                this,
                agents,
                providers.koverExtension,
                excludeAndroidPackages
            )
        )

        doLast(IntellijErrorLogChecker(taskExtension))
    }

    private fun Project.checkAlreadyApplied() {
        var parent = parent

        while (parent != null) {
            if (parent.plugins.hasPlugin(KoverPlugin::class.java)) {
                throw GradleException("Kover plugin is applied in both parent project '${parent.name}' and child project '${this.name}'. Kover plugin should be applied only in parent project.")
            }
            parent = this.parent
        }
    }
}

private class IntellijErrorLogChecker(private val taskExtension: KoverTaskExtension) : Action<Task> {
    override fun execute(task: Task) {
        task.project.copyIntellijErrorLog(
            task.project.layout.buildDirectory.get().file("kover/errors/${task.name}.log").asFile,
            taskExtension.binaryReportFile.get().parentFile
        )
    }
}

private class CoverageArgumentProvider(
    private val task: Task,
    private val agents: Map<CoverageEngine, CoverageAgent>,
    @get:Nested val koverExtension: Provider<KoverExtension>,
    @get:Input val excludeAndroidPackage: Provider<Boolean>
) : CommandLineArgumentProvider, Named {

    @get:Nested
    val taskExtension: Provider<KoverTaskExtension> = task.project.provider {
        task.extensions.getByType(KoverTaskExtension::class.java)
    }

    @Internal
    override fun getName(): String {
        return "koverArgumentsProvider"
    }

    override fun asArguments(): MutableIterable<String> {
        val koverExtensionValue = koverExtension.get()
        val taskExtensionValue = taskExtension.get()

        if (taskExtensionValue.isDisabled
            || koverExtensionValue.isDisabled
            || koverExtensionValue.disabledProjects.contains(task.project.name)
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
            taskExtensionValue.excludes = taskExtensionValue.excludes + "android.*" + "com.android.*"
        }

        return agents.getFor(koverExtensionValue.coverageEngine.get()).buildCommandLineArgs(task, taskExtensionValue)
    }
}
