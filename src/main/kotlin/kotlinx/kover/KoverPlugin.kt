/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover

import kotlinx.kover.api.*
import kotlinx.kover.api.KoverPaths.HTML_AGG_REPORT_DEFAULT_PATH
import kotlinx.kover.api.KoverNames.CHECK_TASK_NAME
import kotlinx.kover.api.KoverNames.COLLECT_MODULE_REPORTS_TASK_NAME
import kotlinx.kover.api.KoverNames.HTML_REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.HTML_MODULE_REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.MODULE_REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.MODULE_VERIFY_TASK_NAME
import kotlinx.kover.api.KoverNames.REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.XML_MODULE_REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.ROOT_EXTENSION_NAME
import kotlinx.kover.api.KoverNames.TASK_EXTENSION_NAME
import kotlinx.kover.api.KoverNames.VERIFICATION_GROUP
import kotlinx.kover.api.KoverNames.VERIFY_TASK_NAME
import kotlinx.kover.api.KoverNames.XML_REPORT_TASK_NAME
import kotlinx.kover.api.KoverPaths.ALL_MODULES_REPORTS_DEFAULT_PATH
import kotlinx.kover.api.KoverPaths.HTML_MODULE_REPORT_DEFAULT_PATH
import kotlinx.kover.api.KoverPaths.XML_AGG_REPORT_DEFAULT_PATH
import kotlinx.kover.api.KoverPaths.XML_MODULE_REPORT_DEFAULT_PATH
import kotlinx.kover.engines.commons.*
import kotlinx.kover.engines.commons.CoverageAgent
import kotlinx.kover.engines.intellij.*
import kotlinx.kover.tasks.*
import org.gradle.api.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.testing.*
import org.gradle.process.*
import java.io.*
import kotlin.reflect.*

class KoverPlugin : Plugin<Project> {
    private val defaultJacocoVersion = "0.8.7"

    override fun apply(target: Project) {
        target.checkAlreadyApplied()

        val koverExtension = target.createKoverExtension()
        val agents = AgentsFactory.createAgents(target, koverExtension)

        val providers = target.createProviders(agents)

        target.allprojects {
            it.applyToModule(providers, agents)
        }
        target.createCollectingTask()

        target.createAggregateTasks(providers)
    }

    private fun Project.applyToModule(providers: ProjectProviders, agents: Map<CoverageEngine, CoverageAgent>) {
        val moduleProviders =
            providers.modules[name] ?: throw GradleException("Kover: Providers for module '$name' was not found")

        val xmlReportTask = createKoverModuleTask(
            XML_MODULE_REPORT_TASK_NAME,
            KoverXmlModuleReportTask::class,
            providers,
            moduleProviders
        ) {
            it.xmlReportFile.set(layout.buildDirectory.file(XML_MODULE_REPORT_DEFAULT_PATH))
            it.description = "Generates code coverage XML report for all enabled test tasks in one module."
        }

        val htmlReportTask = createKoverModuleTask(
            HTML_MODULE_REPORT_TASK_NAME,
            KoverHtmlModuleReportTask::class,
            providers,
            moduleProviders
        ) {
            it.htmlReportDir.set(it.project.layout.buildDirectory.dir(HTML_MODULE_REPORT_DEFAULT_PATH))
            it.description = "Generates code coverage HTML report for all enabled test tasks in one module."
        }

        val verifyTask = createKoverModuleTask(
            MODULE_VERIFY_TASK_NAME,
            KoverModuleVerificationTask::class,
            providers,
            moduleProviders
        ) {
            it.onlyIf { t -> (t as KoverModuleVerificationTask).rules.isNotEmpty() }
            // kover takes counter values from XML file. Remove after reporter upgrade
            it.mustRunAfter(xmlReportTask)
            it.description = "Verifies code coverage metrics of one module based on specified rules."
        }

        tasks.create(MODULE_REPORT_TASK_NAME) {
            it.group = VERIFICATION_GROUP
            it.dependsOn(xmlReportTask)
            it.dependsOn(htmlReportTask)
            it.description = "Generates code coverage HTML and XML reports for all enabled test tasks in one module."
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

    private fun Project.createAggregateTasks(providers: ProjectProviders) {
        val xmlReportTask = createKoverAggregateTask(
            XML_REPORT_TASK_NAME,
            KoverXmlReportTask::class,
            providers
        ) {
            it.xmlReportFile.set(layout.buildDirectory.file(XML_AGG_REPORT_DEFAULT_PATH))
            it.description = "Generates code coverage XML report for all enabled test tasks in all modules."
        }

        val htmlReportTask = createKoverAggregateTask(
            HTML_REPORT_TASK_NAME,
            KoverHtmlReportTask::class,
            providers
        ) {
            it.htmlReportDir.set(layout.buildDirectory.dir(HTML_AGG_REPORT_DEFAULT_PATH))
            it.description = "Generates code coverage HTML report for all enabled test tasks in all modules."
        }

        val reportTask = tasks.create(REPORT_TASK_NAME) {
            it.group = VERIFICATION_GROUP
            it.dependsOn(xmlReportTask)
            it.dependsOn(htmlReportTask)
            it.description = "Generates code coverage HTML and XML reports for all enabled test tasks in all modules."
        }

        val verifyTask = createKoverAggregateTask(
            VERIFY_TASK_NAME,
            KoverVerificationTask::class,
            providers
        ) {
            it.onlyIf { t -> (t as KoverVerificationTask).rules.isNotEmpty() }
            // kover takes counter values from XML file. Remove after reporter upgrade
            it.mustRunAfter(xmlReportTask)
            it.description = "Verifies code coverage metrics of all modules based on specified rules."
        }

        tasks.configureEach {
            if (it.name == CHECK_TASK_NAME) {
                it.dependsOn(provider {
                    val koverExtension = extensions.getByType(KoverExtension::class.java)
                    if (koverExtension.generateReportOnCheck.get()) {
                        listOf(reportTask, verifyTask)
                    } else {
                        listOf(verifyTask)
                    }
                })
            }
        }
    }


    private fun <T : KoverAggregateTask> Project.createKoverAggregateTask(
        taskName: String,
        type: KClass<T>,
        providers: ProjectProviders,
        block: (T) -> Unit
    ): T {
        return tasks.create(taskName, type.java) { task ->
            task.group = VERIFICATION_GROUP

            providers.modules.forEach { (moduleName, m) ->
                task.binaryReportFiles.put(moduleName, NestedFiles(task.project.objects, m.reports))
                task.smapFiles.put(moduleName, NestedFiles(task.project.objects, m.smap))
                task.srcDirs.put(moduleName, NestedFiles(task.project.objects, m.sources))
                task.outputDirs.put(moduleName, NestedFiles(task.project.objects, m.output))
            }

            task.coverageEngine.set(providers.engine)
            task.classpath.set(providers.classpath)
            task.dependsOn(providers.allModules.tests)

            block(task)
        }
    }

    private fun Project.createCollectingTask() {
        tasks.create(COLLECT_MODULE_REPORTS_TASK_NAME, KoverCollectingModulesTask::class.java) { task ->
            task.group = VERIFICATION_GROUP
            task.description = "Collects all modules reports into one directory."
            task.outputDir.set(project.layout.buildDirectory.dir(ALL_MODULES_REPORTS_DEFAULT_PATH))
            // disable UP-TO-DATE check for task: it will be executed every time
            task.outputs.upToDateWhen { false }

            allprojects { proj ->
                val xmlReportTask =
                    proj.tasks.withType(KoverXmlModuleReportTask::class.java).getByName(XML_MODULE_REPORT_TASK_NAME)
                val htmlReportTask =
                    proj.tasks.withType(KoverHtmlModuleReportTask::class.java).getByName(HTML_MODULE_REPORT_TASK_NAME)

                task.mustRunAfter(xmlReportTask)
                task.mustRunAfter(htmlReportTask)

                task.xmlFiles[proj.name] = xmlReportTask.xmlReportFile
                task.htmlDirs[proj.name] = htmlReportTask.htmlReportDir
            }
        }
    }


    private fun <T : KoverModuleTask> Project.createKoverModuleTask(
        taskName: String,
        type: KClass<T>,
        providers: ProjectProviders,
        moduleProviders: ModuleProviders,
        block: (T) -> Unit
    ): T {
        return tasks.create(taskName, type.java) { task ->
            task.group = VERIFICATION_GROUP

            task.coverageEngine.set(providers.engine)
            task.classpath.set(providers.classpath)
            task.srcDirs.set(moduleProviders.sources)
            task.outputDirs.set(moduleProviders.output)

            // it is necessary to read all binary reports because module's classes can be invoked in another module
            task.binaryReportFiles.set(providers.allModules.reports)
            task.smapFiles.set(providers.allModules.smap)
            task.dependsOn(providers.allModules.tests)

            task.onlyIf { !moduleProviders.disabled.get() }
            task.onlyIf { !task.binaryReportFiles.get().isEmpty }

            block(task)
        }
    }

    private fun Project.createKoverExtension(): KoverExtension {
        val extension = extensions.create(ROOT_EXTENSION_NAME, KoverExtension::class.java, objects)
        extension.isEnabled = true
        extension.coverageEngine.set(CoverageEngine.INTELLIJ)
        extension.intellijEngineVersion.set(defaultIntellijVersion.toString())
        extension.jacocoEngineVersion.set(defaultJacocoVersion)
        extension.generateReportOnCheck.set(true)
        return extension
    }

    private fun Test.configTest(
        providers: ProjectProviders,
        agents: Map<CoverageEngine, CoverageAgent>
    ): KoverTaskExtension {
        val taskExtension = extensions.create(TASK_EXTENSION_NAME, KoverTaskExtension::class.java, project.objects)

        taskExtension.isEnabled = true
        taskExtension.binaryReportFile.set(this.project.provider {
            val koverExtension = providers.koverExtension.get()
            val suffix = if (koverExtension.coverageEngine.get() == CoverageEngine.INTELLIJ) ".ic" else ".exec"
            project.layout.buildDirectory.get().file("kover/$name$suffix").asFile
        })
        taskExtension.smapFile.set(this.project.provider {
            val koverExtension = providers.koverExtension.get()
            if (koverExtension.coverageEngine.get() == CoverageEngine.INTELLIJ)
                File(taskExtension.binaryReportFile.get().canonicalPath + ".smap")
            else
                null
        })
        jvmArgumentProviders.add(CoverageArgumentProvider(this, agents, providers.koverExtension))

        return taskExtension
    }

    private fun Project.checkAlreadyApplied() {
        var parent = parent

        while (parent != null) {
            if (parent.plugins.hasPlugin(KoverPlugin::class.java)) {
                throw GradleException("Kover plugin is applied in both parent module '${parent.name}' and child module '${this.name}'. Kover plugin should be applied only in parent module.")
            }
            parent = this.parent
        }
    }
}

private class CoverageArgumentProvider(
    private val task: Task,
    private val agents: Map<CoverageEngine, CoverageAgent>,
    @get:Nested
    val koverExtension: Provider<KoverExtension>
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

        if (!taskExtensionValue.isEnabled
            || !koverExtensionValue.isEnabled
            || koverExtensionValue.disabledModules.contains(task.project.name)
        ) {
            return mutableListOf()
        }

        return agents.getFor(koverExtensionValue.coverageEngine.get()).buildCommandLineArgs(task, taskExtensionValue)
    }
}
