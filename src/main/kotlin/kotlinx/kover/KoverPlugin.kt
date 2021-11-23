/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover

import kotlinx.kover.adapters.*
import kotlinx.kover.api.*
import kotlinx.kover.api.KoverNames.CHECK_TASK_NAME
import kotlinx.kover.api.KoverNames.COLLECT_TASK_NAME
import kotlinx.kover.api.KoverNames.HTML_REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.REPORT_TASK_NAME
import kotlinx.kover.api.KoverNames.ROOT_EXTENSION_NAME
import kotlinx.kover.api.KoverNames.TASK_EXTENSION_NAME
import kotlinx.kover.api.KoverNames.VERIFICATION_GROUP
import kotlinx.kover.api.KoverNames.VERIFY_TASK_NAME
import kotlinx.kover.api.KoverNames.XML_REPORT_TASK_NAME
import kotlinx.kover.engines.intellij.*
import kotlinx.kover.engines.jacoco.*
import kotlinx.kover.tasks.*
import org.gradle.api.*
import org.gradle.api.tasks.testing.*
import org.gradle.process.*
import kotlin.reflect.*

class KoverPlugin : Plugin<Project> {
    private val defaultJacocoVersion = "0.8.7"

    override fun apply(target: Project) {
        val koverExtension = target.createKoverExtension()
        val intellijAgent = target.createIntellijAgent(koverExtension)
        val jacocoAgent = target.createJacocoAgent(koverExtension)

        target.allprojects {
            it.applyToProject(koverExtension, intellijAgent, jacocoAgent)
        }
        target.createCollectingTask()
    }

    private fun Project.createCollectingTask() {
        tasks.create(COLLECT_TASK_NAME, KoverCollectingTask::class.java) {
            it.group = VERIFICATION_GROUP
            it.description = "Collects reports from all submodules in one directory."
            it.outputDir.set(project.layout.buildDirectory.dir("reports/kover/all"))
            // disable UP-TO-DATE check for task: it will be executed every time
            it.outputs.upToDateWhen { false }

            allprojects { proj ->
                val xmlReportTask = proj.tasks.withType(KoverXmlReportTask::class.java).getByName(XML_REPORT_TASK_NAME)
                val htmlReportTask =
                    proj.tasks.withType(KoverHtmlReportTask::class.java).getByName(HTML_REPORT_TASK_NAME)

                it.mustRunAfter(xmlReportTask)
                it.mustRunAfter(htmlReportTask)

                it.xmlFiles[proj.name] = xmlReportTask.xmlReportFile
                it.htmlDirs[proj.name] = htmlReportTask.htmlReportDir
            }
        }
    }


    private fun Project.applyToProject(
        koverExtension: KoverExtension,
        intellijAgent: IntellijAgent,
        jacocoAgent: JacocoAgent
    ) {
        val xmlReportTask = createKoverCommonTask(
            XML_REPORT_TASK_NAME,
            KoverXmlReportTask::class,
            koverExtension,
            intellijAgent,
            jacocoAgent
        ) {
            it.xmlReportFile.set(provider {
                layout.buildDirectory.get().file("reports/kover/report.xml")
            })
        }

        val htmlReportTask = createKoverCommonTask(
            HTML_REPORT_TASK_NAME,
            KoverHtmlReportTask::class,
            koverExtension,
            intellijAgent,
            jacocoAgent
        ) {
            it.htmlReportDir.set(it.project.provider {
                it.project.layout.buildDirectory.get().dir("reports/kover/html")
            })
        }

        val verificationTask = createKoverCommonTask(
            VERIFY_TASK_NAME,
            KoverVerificationTask::class,
            koverExtension,
            intellijAgent,
            jacocoAgent
        ) {
            it.onlyIf { t -> (t as KoverVerificationTask).rules.isNotEmpty() }
            // kover takes counter values from XML file. Remove after reporter upgrade
            it.mustRunAfter(xmlReportTask)
        }

        val koverReportTask = tasks.create(REPORT_TASK_NAME) {
            it.group = VERIFICATION_GROUP
            it.description = "Generates code coverage HTML and XML reports for all module's test tasks."
            it.dependsOn(xmlReportTask)
            it.dependsOn(htmlReportTask)
        }

        tasks.configureEach {
            if (it.name == CHECK_TASK_NAME) {
                it.dependsOn(verificationTask)
                it.dependsOn(provider {
                    if (koverExtension.generateReportOnCheck.get()) {
                        koverReportTask
                    } else {
                        verificationTask
                    }
                })
            }
        }

        val srcProvider = provider { collectDirs().first }
        xmlReportTask.srcDirs.set(srcProvider)
        htmlReportTask.srcDirs.set(srcProvider)
        verificationTask.srcDirs.set(srcProvider)

        val outputProvider = provider { collectDirs().second }
        xmlReportTask.outputDirs.set(outputProvider)
        htmlReportTask.outputDirs.set(outputProvider)
        verificationTask.outputDirs.set(outputProvider)

        tasks.withType(Test::class.java).configureEach { t ->
            t.applyToTask(koverExtension, intellijAgent, jacocoAgent)
        }

        val binariesProvider = provider {
            // process binary report only from tasks with enabled cover
            val files = tasks.withType(Test::class.java)
                .map { t -> t.extensions.getByType(KoverTaskExtension::class.java) }
                .filter { e -> e.isEnabled }
                .map { e -> e.binaryReportFile.get() }
                .filter { f -> f.exists() }
            files(files)
        }
        xmlReportTask.binaryReportFiles.set(binariesProvider)
        htmlReportTask.binaryReportFiles.set(binariesProvider)
        verificationTask.binaryReportFiles.set(binariesProvider)

        val enabledTestsProvider = provider {
            tasks.withType(Test::class.java)
                .filter { t -> t.extensions.getByType(KoverTaskExtension::class.java).isEnabled }
        }
        xmlReportTask.dependsOn(enabledTestsProvider)
        htmlReportTask.dependsOn(enabledTestsProvider)
        verificationTask.dependsOn(enabledTestsProvider)

        xmlReportTask.description = "Generates code coverage XML report for all module's test tasks."
        htmlReportTask.description = "Generates code coverage HTML report for all module's test tasks."
        verificationTask.description = "Verifies code coverage metrics based on specified rules."
    }


    private fun <T : KoverCommonTask> Project.createKoverCommonTask(
        taskName: String,
        type: KClass<T>,
        koverExtension: KoverExtension,
        intellijAgent: IntellijAgent,
        jacocoAgent: JacocoAgent,
        block: (T) -> Unit
    ): T {
        return tasks.create(taskName, type.java) {
            it.group = VERIFICATION_GROUP

            it.coverageEngine.set(koverExtension.coverageEngine)
            it.classpath.set(provider {
                if (koverExtension.coverageEngine.get() == CoverageEngine.INTELLIJ) intellijAgent.config else jacocoAgent.config
            })

            block(it)
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

    private fun Test.applyToTask(
        koverExtension: KoverExtension,
        intellijAgent: IntellijAgent,
        jacocoAgent: JacocoAgent
    ): KoverTaskExtension {
        val taskExtension = extensions.create(TASK_EXTENSION_NAME, KoverTaskExtension::class.java, project.objects)

        taskExtension.isEnabled = true
        taskExtension.binaryReportFile.set(this.project.provider {
            val suffix = if (koverExtension.coverageEngine.get() == CoverageEngine.INTELLIJ) ".ic" else ".exec"
            project.layout.buildDirectory.get().file("kover/$name$suffix").asFile
        })
        jvmArgumentProviders.add(
            CoverageArgumentProvider(
                jacocoAgent,
                intellijAgent,
                koverExtension,
                taskExtension,
                this
            )
        )

        return taskExtension
    }
}

private class CoverageArgumentProvider(
    private val jacocoAgent: JacocoAgent,
    private val intellijAgent: IntellijAgent,
    private val koverExtension: KoverExtension,
    private val taskExtension: KoverTaskExtension,
    private val task: Task
) : CommandLineArgumentProvider {
    override fun asArguments(): MutableIterable<String> {
        if (!taskExtension.isEnabled || !koverExtension.isEnabled) {
            return mutableListOf()
        }

        return if (koverExtension.coverageEngine.get() == CoverageEngine.INTELLIJ) {
            intellijAgent.buildCommandLineArgs(taskExtension, task)
        } else {
            jacocoAgent.buildCommandLineArgs(taskExtension)
        }
    }
}
