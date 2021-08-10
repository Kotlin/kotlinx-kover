/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover

import groovy.lang.Closure
import groovy.lang.GroovyObject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.process.CommandLineArgumentProvider
import java.io.File
import javax.inject.Inject

class KoverPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.repositories.maven {
            it.url = target.uri("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
        }

        val koverExtension = target.extensions.create("kover", KoverExtension::class.java, target.objects)
        koverExtension.intellijAgentVersion.set("1.0.598")
        koverExtension.jacocoAgentVersion.set("0.8.7")

        val configuration = target.createCoverageConfiguration(koverExtension)

        target.tasks.withType(Test::class.java).configureEach {
            it.applyCoverage(configuration)
        }
    }

    private fun Project.createCoverageConfiguration(koverExtension: KoverExtension): Configuration {
        val config = project.configurations.create("koverConfig")
        config.isVisible = false
        config.isTransitive = true
        config.description = "Kover - Kotlin Code Coverage Plugin Configuration"

        config.defaultDependencies { dependencies ->
            val usedIntellijAgent = tasks.withType(Test::class.java)
                .any { !(it.extensions.findByName("kover") as KoverTaskExtension).useJacoco }

            val usedJaCoCoAgent = tasks.withType(Test::class.java)
                .any { (it.extensions.findByName("kover") as KoverTaskExtension).useJacoco }

            if (usedIntellijAgent) {
                dependencies.add(
                    this.dependencies.create("org.jetbrains.intellij.deps:intellij-coverage-agent:${koverExtension.intellijAgentVersion.get()}")
                )
            }

            if (usedJaCoCoAgent) {
                dependencies.add(
                    this.dependencies.create("org.jacoco:org.jacoco.agent:${koverExtension.jacocoAgentVersion.get()}")
                )
                dependencies.add(
                    this.dependencies.create("org.jacoco:org.jacoco.ant:${koverExtension.jacocoAgentVersion.get()}")
                )
            }
        }
        return config
    }

    private fun Test.applyCoverage(configuration: Configuration) {
        val taskExtension = extensions.create("kover", KoverTaskExtension::class.java, project.objects)

        taskExtension.xmlReportFile.set(this.project.provider {
            project.layout.buildDirectory.get().file("reports/kover/$name.xml").asFile
        })

        taskExtension.binaryFile.set(this.project.provider {
            val suffix = if (taskExtension.useJacoco) ".exec" else ".ic"
            project.layout.buildDirectory.get().file("kover/$name$suffix").asFile
        })


        val reader = project.objects.newInstance(JaCoCoAgentReader::class.java, project, configuration)

        jvmArgumentProviders.add(CoverageArgumentProvider(taskExtension, configuration, reader))

        doLast {
            if (taskExtension.useJacoco) {
                (it.ant as GroovyObject).jacocoReport(it, taskExtension, configuration)
            } else {
                // TODO call report
            }
        }
    }
}

private fun GroovyObject.jacocoReport(
    task: Task,
    extension: KoverTaskExtension,
    configuration: Configuration
) {
    invokeMethod(
        "taskdef",
        mapOf(
            "name" to "jacocoReport",
            "classname" to "org.jacoco.ant.ReportTask",
            "classpath" to configuration.asPath
        )
    )

    val binaries = task.project.files(extension.binaryFile.get())
    val sourceSet = task.project.extensions.getByType(
        SourceSetContainer::class.java
    ).getByName("main") // TODO support multiplatform source sets

    val srcDirs = sourceSet.allSource.srcDirs.filter { it.exists() }
    val output = sourceSet.output.filter { it.exists() }
    val xmlFile = extension.xmlReportFile.get()
    xmlFile.parentFile.mkdirs()

    invokeWithBody("jacocoReport") {
        invokeWithBody("executiondata") {
            binaries.addToAntBuilder(this@jacocoReport, "resources")
        }
        invokeWithBody("structure", mapOf("name" to task.project.name)) {
            invokeWithBody("classfiles") {
                task.project.files(output).addToAntBuilder(this@jacocoReport, "resources")
            }
            invokeWithBody("sourcefiles") {
                task.project.files(srcDirs).addToAntBuilder(this@jacocoReport, "resources")
            }
        }
        invokeMethod("xml", mapOf("destfile" to xmlFile))
    }
}

private inline fun GroovyObject.invokeWithBody(
    name: String,
    args: Map<String, String> = emptyMap(),
    crossinline body: GroovyObject.() -> Unit
) {
    invokeMethod(
        name,
        listOf(
            args,
            object : Closure<Any?>(this) {
                fun doCall(ignore: Any?): Any? {
                    this@invokeWithBody.body()
                    return null
                }
            }
        )
    )
}


private open class JaCoCoAgentReader @Inject constructor(
    private val project: Project,
    private val configuration: Configuration
) {
    private var agentJar: File? = null

    fun get(): File {
        if (agentJar == null) {
            val containedJarFile = configuration.fileCollection { it.name == "org.jacoco.agent" }.singleFile
            agentJar = project.zipTree(containedJarFile).filter { it.name == "jacocoagent.jar" }.singleFile
        }
        return agentJar!!
    }
}


private class CoverageArgumentProvider(
    private val extension: KoverTaskExtension,
    private val configuration: Configuration,
    private val jacocoAgentReader: JaCoCoAgentReader
) : CommandLineArgumentProvider {

    override fun asArguments(): MutableIterable<String> {
        if (!extension.enabled) {
            return mutableListOf()
        }

        return if (extension.useJacoco) {
            jacocoAgent()
        } else {
            intellijAgent()
        }
    }

    private fun intellijAgent(): MutableList<String> {
        val jarFile = configuration.fileCollection { it.name == "intellij-coverage-agent" }.singleFile
        return mutableListOf(
            "-javaagent:${jarFile.canonicalPath}=${intellijAgentArgs()}",
            "-Didea.coverage.check.inline.signatures=true",
            "-Didea.new.sampling.coverage=true",
            "-Didea.new.tracing.coverage=true"
        )
    }

    private fun intellijAgentArgs(): String {
        val includesString = extension.includes.joinToString(" ")
        val excludesString = extension.excludes.let { if (it.isNotEmpty()) "-exclude ${it.joinToString(" ")}" else "" }

        return "\"${extension.xmlReportFile.get().canonicalPath}\" false false false false -xml $includesString $excludesString"
    }

    private fun jacocoAgent(): MutableList<String> {
        return mutableListOf("-javaagent:${jacocoAgentReader.get().canonicalPath}=${jacocoAgentArgs()}")
    }

    private fun jacocoAgentArgs(): String {
        return listOf(
            "destfile=${extension.binaryFile.get().canonicalPath}",
            "append=false", // Kover don't support parallel execution of one task
            "inclnolocationclasses=false",
            "dumponexit=true",
            "output=file",
            "jmx=false"
        ).joinToString(",")
    }

}
