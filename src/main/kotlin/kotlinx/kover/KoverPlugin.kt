/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover

import groovy.lang.Closure
import groovy.lang.GroovyObject
import kotlinx.kover.adapters.collectDirs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.testing.Test
import org.gradle.process.CommandLineArgumentProvider
import java.io.File
import javax.inject.Inject

class KoverPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.repositories.maven {
            it.url = target.uri("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
        }
        // hotfix for reporter XML: version 0.255 placed in coverage repository
        // TODO remove after fixing reporter
        target.repositories.maven {
            it.url = target.uri("https://maven.pkg.jetbrains.space/public/p/jb-coverage/maven")
        }

        val koverExtension = target.extensions.create("kover", KoverExtension::class.java, target.objects)
        koverExtension.intellijAgentVersion.set("1.0.608")
        koverExtension.jacocoAgentVersion.set("0.8.7")

        val intellijConfig = target.createIntellijConfig(koverExtension)
        val jacocoConfig = target.createJacocoConfig(koverExtension)

        target.tasks.withType(Test::class.java).configureEach {
            it.applyCoverage(intellijConfig, jacocoConfig)
        }
    }

    private fun Project.createIntellijConfig(koverExtension: KoverExtension): Configuration {
        val config = project.configurations.create("IntellijKoverConfig")
        config.isVisible = false
        config.isTransitive = true
        config.description = "Kotlin Kover Plugin configuration for IntelliJ agent and reporter"

        config.defaultDependencies { dependencies ->
            val usedIntellijAgent = tasks.withType(Test::class.java)
                .any { !(it.extensions.findByName("kover") as KoverTaskExtension).useJacoco }

            if (usedIntellijAgent) {
                val agentVersion = koverExtension.intellijAgentVersion.get()
                dependencies.add(
                    this.dependencies.create("org.jetbrains.intellij.deps:intellij-coverage-agent:$agentVersion")
                )

                // hotfix for reporter XML: version 1.0.608 not generate INSTRUCTION counter, hotfixed in 0.255
                // TODO remove after fixing reporter
                val reporterVersion = if (agentVersion == "1.0.608") "0.255" else agentVersion

                dependencies.add(
                    this.dependencies.create("org.jetbrains.intellij.deps:intellij-coverage-reporter:$reporterVersion")
                )
            }
        }
        return config
    }

    private fun Project.createJacocoConfig(koverExtension: KoverExtension): Configuration {
        val config = project.configurations.create("JacocoKoverConfig")
        config.isVisible = false
        config.isTransitive = true
        config.description = "Kotlin Kover Plugin configuration for JaCoCo agent and reporter"

        config.defaultDependencies { dependencies ->
            val used = tasks.withType(Test::class.java)
                .any { (it.extensions.findByName("kover") as KoverTaskExtension).useJacoco }

            if (used) {
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

    private fun Test.applyCoverage(intellijConfig: Configuration, jacocoConfig: Configuration) {
        val taskExtension = extensions.create("kover", KoverTaskExtension::class.java, project.objects)

        taskExtension.xmlReportFile.set(this.project.provider {
            project.layout.buildDirectory.get().file("reports/kover/$name.xml").asFile
        })

        taskExtension.htmlReportDir.set(this.project.provider {
            project.layout.buildDirectory.get().dir("reports/kover/html/$name")
        })

        taskExtension.binaryFile.set(this.project.provider {
            val suffix = if (taskExtension.useJacoco) ".exec" else ".ic"
            project.layout.buildDirectory.get().file("kover/$name$suffix").asFile
        })

        val reader = project.objects.newInstance(JaCoCoAgentReader::class.java, project, jacocoConfig)

        jvmArgumentProviders.add(CoverageArgumentProvider(taskExtension, reader, intellijConfig))

        doLast {
            if (!(taskExtension.enabled && (taskExtension.xmlReport || taskExtension.htmlReport))) {
                return@doLast
            }

            if (taskExtension.useJacoco) {
                it.jacocoReport(taskExtension, jacocoConfig)
            } else {
                it.intellijReport(taskExtension, intellijConfig)
            }
        }
    }
}

private fun Task.jacocoReport(
    extension: KoverTaskExtension,
    configuration: Configuration
) {
    val dirs = project.collectDirs()

    val builder = ant as GroovyObject
    builder.invokeMethod(
        "taskdef",
        mapOf(
            "name" to "jacocoReport",
            "classname" to "org.jacoco.ant.ReportTask",
            "classpath" to configuration.asPath
        )
    )

    builder.invokeWithBody("jacocoReport") {
        invokeWithBody("executiondata") {
            val binaries = project.files(extension.binaryFile.get())
            binaries.addToAntBuilder(this, "resources")
        }
        invokeWithBody("structure", mapOf("name" to project.name)) {
            invokeWithBody("classfiles") {
                project.files(dirs.second).addToAntBuilder(this, "resources")
            }
            invokeWithBody("sourcefiles") {
                project.files(dirs.first).addToAntBuilder(this, "resources")
            }
        }

        if (extension.xmlReport) {
            val xmlFile = extension.xmlReportFile.get()
            xmlFile.parentFile.mkdirs()
            invokeMethod("xml", mapOf("destfile" to xmlFile))
        }
        if (extension.htmlReport) {
            val htmlDir = extension.htmlReportDir.get().asFile
            htmlDir.mkdirs()
            invokeMethod("html", mapOf("destdir" to htmlDir))
        }
    }
}

private fun Task.intellijReport(
    extension: KoverTaskExtension,
    configuration: Configuration
) {
    val binary = extension.binaryFile.get()

    val dirs = project.collectDirs()
    val output = dirs.second.joinToString(",") { file -> file.canonicalPath }

    val args = mutableListOf(
        "reports=\"${binary.canonicalPath}\":\"${binary.canonicalPath}.smap\"",
        "output=$output"
    )

    if (extension.xmlReport) {
        val xmlFile = extension.xmlReportFile.get()
        xmlFile.parentFile.mkdirs()
        args += "xml=${xmlFile.canonicalPath}"
    }
    if (extension.htmlReport) {
        val htmlDir = extension.htmlReportDir.get().asFile
        htmlDir.mkdirs()
        args += "html=${htmlDir.canonicalPath}"
        args += dirs.first.joinToString(",", "sources=") { file -> file.canonicalPath }
    }

    project.javaexec { e ->
        e.mainClass.set("com.intellij.rt.coverage.report.Main")
        e.classpath = configuration
        e.args = args
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
    private val config: Configuration
) {
    private var agentJar: File? = null

    fun get(): File {
        if (agentJar == null) {
            val containedJarFile = config.fileCollection { it.name == "org.jacoco.agent" }.singleFile
            agentJar = project.zipTree(containedJarFile).filter { it.name == "jacocoagent.jar" }.singleFile
        }
        return agentJar!!
    }
}


private class CoverageArgumentProvider(
    private val extension: KoverTaskExtension,
    private val jacocoAgentReader: JaCoCoAgentReader,
    private val intellijConfig: Configuration,
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
        val jarFile = intellijConfig.fileCollection { it.name == "intellij-coverage-agent" }.singleFile
        return mutableListOf(
            "-javaagent:${jarFile.canonicalPath}=${intellijAgentArgs()}",
            "-Didea.coverage.check.inline.signatures=true",
            "-Didea.new.sampling.coverage=true",
            "-Didea.new.tracing.coverage=true"
        )
    }

    private fun intellijAgentArgs(): String {
        val includesString = extension.includes.joinToString(" ", " ")
        val excludesString = extension.excludes.let { if (it.isNotEmpty()) it.joinToString(" ", " -exclude ") else "" }

        val binary = extension.binaryFile.get()
        binary.parentFile.mkdirs()
        val binaryPath = binary.canonicalPath
        return "\"$binaryPath\" false false false false true \"$binaryPath.smap\"$includesString$excludesString"
    }

    private fun jacocoAgent(): MutableList<String> {
        return mutableListOf("-javaagent:${jacocoAgentReader.get().canonicalPath}=${jacocoAgentArgs()}")
    }

    private fun jacocoAgentArgs(): String {
        val binary = extension.binaryFile.get()
        binary.parentFile.mkdirs()

        return listOf(
            "destfile=${binary.canonicalPath}",
            "append=false", // Kover don't support parallel execution of one task
            "inclnolocationclasses=false",
            "dumponexit=true",
            "output=file",
            "jmx=false"
        ).joinToString(",")
    }

}
