/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover

import kotlinx.kover.api.*
import kotlinx.kover.engines.intellij.*
import kotlinx.kover.engines.jacoco.*
import org.gradle.api.*
import org.gradle.api.tasks.testing.*
import org.gradle.process.*

class KoverPlugin : Plugin<Project> {
    private val defaultJacocoVersion = "0.8.7"

    override fun apply(target: Project) {
        target.repositories.maven {
            it.url = target.uri("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
        }

        val koverExtension = target.extensions.create("kover", KoverExtension::class.java, target.objects)
        koverExtension.intellijEngineVersion.set(defaultIntellijVersion.toString())
        koverExtension.jacocoEngineVersion.set(defaultJacocoVersion)

        val intellijConfig = target.createIntellijConfig(koverExtension)
        val jacocoConfig = target.createJacocoConfig(koverExtension)
        val jacocoAgent = JacocoAgent(jacocoConfig, target)
        val intellijAgent = IntellijAgent(intellijConfig)

        target.tasks.withType(Test::class.java).configureEach {
            it.applyCoverage(intellijAgent, jacocoAgent)
        }
    }

    private fun Test.applyCoverage(intellijAgent: IntellijAgent, jacocoAgent: JacocoAgent) {
        val taskExtension = extensions.create("kover", KoverTaskExtension::class.java, project.objects)

        taskExtension.xmlReportFile.set(this.project.provider {
            project.layout.buildDirectory.get().file("reports/kover/$name.xml").asFile
        })

        taskExtension.htmlReportDir.set(this.project.provider {
            project.layout.buildDirectory.get().dir("reports/kover/html/$name")
        })

        taskExtension.binaryReportFile.set(this.project.provider {
            val suffix = if (taskExtension.coverageEngine == CoverageEngine.JACOCO) ".exec" else ".ic"
            project.layout.buildDirectory.get().file("kover/$name$suffix").asFile
        })

        jvmArgumentProviders.add(CoverageArgumentProvider(jacocoAgent, intellijAgent, taskExtension, this))

        doLast {
            taskExtension.generateXml = taskExtension.generateXml
                    // turn on XML report for intellij agent if verification rules are defined
                    || (taskExtension.coverageEngine == CoverageEngine.INTELLIJ && taskExtension.rules.isNotEmpty())

            if (!(taskExtension.isEnabled && (taskExtension.generateXml || taskExtension.generateHtml || taskExtension.rules.isNotEmpty()))) {
                return@doLast
            }

            if (taskExtension.coverageEngine == CoverageEngine.JACOCO) {
                val builder = it.jacocoAntBuilder(jacocoAgent.config)
                it.jacocoReport(builder, taskExtension)
                it.jacocoVerification(builder, taskExtension)
            } else {
                it.intellijReport(taskExtension, intellijAgent.config, this)
                it.intellijVerification(taskExtension)
            }
        }
    }
}

private class CoverageArgumentProvider(
    private val jacocoAgent: JacocoAgent,
    private val intellijAgent: IntellijAgent,
    private val extension: KoverTaskExtension,
    private val task: Task
) : CommandLineArgumentProvider {
    override fun asArguments(): MutableIterable<String> {
        if (!extension.isEnabled) {
            return mutableListOf()
        }

        return if (extension.coverageEngine == CoverageEngine.INTELLIJ) {
            intellijAgent.buildCommandLineArgs(extension, task)
        } else {
            jacocoAgent.buildCommandLineArgs(extension)
        }
    }
}
