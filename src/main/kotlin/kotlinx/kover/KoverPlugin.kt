/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover

import groovy.lang.Closure
import groovy.lang.GroovyObject
import kotlinx.kover.VerificationValueType.*
import kotlinx.kover.adapters.collectDirs
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.testing.Test
import org.gradle.process.CommandLineArgumentProvider
import java.io.File
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class KoverPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.repositories.maven {
            it.url = target.uri("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
        }

        val koverExtension = target.extensions.create("kover", KoverExtension::class.java, target.objects)
        koverExtension.intellijEngineVersion.set("1.0.611")
        koverExtension.jacocoEngineVersion.set("0.8.7")

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
                .any { (it.extensions.findByName("kover") as KoverTaskExtension).coverageEngine == CoverageEngine.INTELLIJ }

            if (usedIntellijAgent) {
                val agentVersion = koverExtension.intellijEngineVersion.get()
                dependencies.add(
                    this.dependencies.create("org.jetbrains.intellij.deps:intellij-coverage-agent:$agentVersion")
                )

                dependencies.add(
                    this.dependencies.create("org.jetbrains.intellij.deps:intellij-coverage-reporter:$agentVersion")
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
                .any { (it.extensions.findByName("kover") as KoverTaskExtension).coverageEngine == CoverageEngine.JACOCO }

            if (used) {
                dependencies.add(
                    this.dependencies.create("org.jacoco:org.jacoco.agent:${koverExtension.jacocoEngineVersion.get()}")
                )
                dependencies.add(
                    this.dependencies.create("org.jacoco:org.jacoco.ant:${koverExtension.jacocoEngineVersion.get()}")
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

        taskExtension.binaryReportFile.set(this.project.provider {
            val suffix = if (taskExtension.coverageEngine == CoverageEngine.JACOCO) ".exec" else ".ic"
            project.layout.buildDirectory.get().file("kover/$name$suffix").asFile
        })

        val reader = project.objects.newInstance(JaCoCoAgentReader::class.java, project, jacocoConfig)

        jvmArgumentProviders.add(CoverageArgumentProvider(taskExtension, reader, intellijConfig))

        doLast {

            taskExtension.generateXml = taskExtension.generateXml
                    // turn on XML report for intellij agent if verification rules are defined
                    || (coverageEngine == CoverageEngine.INTELLIJ && taskExtension.rules.isNotEmpty())

            if (!(taskExtension.isEnabled && (taskExtension.xmlReport || taskExtension.htmlReport))) {
                return@doLast
            }

            if (taskExtension.coverageEngine == CoverageEngine.JACOCO) {
                val builder = it.jacocoAntBuilder(jacocoConfig)
                it.jacocoReport(builder, taskExtension)
                it.jacocoVerification(builder, taskExtension)
            } else {
                it.intellijReport(taskExtension, intellijConfig)
                it.intellijVerification(taskExtension)
            }
        }
    }
}

private fun Task.jacocoAntBuilder(configuration: Configuration): GroovyObject {
    val builder = ant as GroovyObject
    builder.invokeMethod(
        "taskdef",
        mapOf(
            "name" to "jacocoReport",
            "classname" to "org.jacoco.ant.ReportTask",
            "classpath" to configuration.asPath
        )
    )
    return builder
}

private fun Task.callJacocoAntReportTask(
    builder: GroovyObject,
    extension: KoverTaskExtension,
    block: GroovyObject.() -> Unit
) {
    val dirs = project.collectDirs()

    builder.invokeWithBody("jacocoReport") {
        invokeWithBody("executiondata") {
            val binaries = project.files(extension.binaryReportFile.get())
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
        block()
    }
}

private fun Task.jacocoReport(builder: GroovyObject, extension: KoverTaskExtension) {
    callJacocoAntReportTask(builder, extension) {
        if (extension.generateXml) {
            val xmlFile = extension.xmlReportFile.get()
            xmlFile.parentFile.mkdirs()
            invokeMethod("xml", mapOf("destfile" to xmlFile))
        }
        if (extension.generateHtml) {
            val htmlDir = extension.htmlReportDir.get().asFile
            htmlDir.mkdirs()
            invokeMethod("html", mapOf("destdir" to htmlDir))
        }
    }
}


fun Task.jacocoVerification(builder: GroovyObject, extension: KoverTaskExtension) {
    if (extension.rules.isEmpty()) {
        return
    }

    callJacocoAntReportTask(builder, extension) {
        invokeWithBody("check", mapOf("failonviolation" to "true", "violationsproperty" to "jacocoErrors")) {
            extension.rules.forEach {
                invokeWithBody("rule", mapOf("element" to "BUNDLE")) {
                    val limitArgs = mutableMapOf("counter" to "LINE")
                    var min: BigDecimal? = it.minValue?.toBigDecimal()
                    var max: BigDecimal? = it.maxValue?.toBigDecimal()
                    when (it.valueType) {
                        COVERED_LINES_COUNT -> {
                            limitArgs["value"] = "COVEREDCOUNT"
                        }
                        MISSED_LINES_COUNT -> {
                            limitArgs["value"] = "MISSEDCOUNT"
                        }
                        COVERED_LINES_PERCENTAGE -> {
                            limitArgs["value"] = "COVEREDRATIO"
                            min = min?.divide(BigDecimal(100))
                            max = max?.divide(BigDecimal(100))
                        }
                    }

                    if (min != null) {
                        limitArgs["minimum"] = min.toPlainString()
                    }

                    if (max != null) {
                        limitArgs["maximum"] = max.toPlainString()
                    }
                    invokeMethod("limit", limitArgs)
                }
            }

        }
    }
}

private fun Task.intellijReport(
    extension: KoverTaskExtension,
    configuration: Configuration
) {
    val binary = extension.binaryReportFile.get()

    val dirs = project.collectDirs()
    val output = dirs.second.joinToString(",") { file -> file.canonicalPath }

    val args = mutableListOf(
        "reports=\"${binary.canonicalPath}\":\"${binary.canonicalPath}.smap\"",
        "output=$output"
    )

    if (extension.generateXml) {
        val xmlFile = extension.xmlReportFile.get()
        xmlFile.parentFile.mkdirs()
        args += "xml=${xmlFile.canonicalPath}"
    }
    if (extension.generateHtml) {
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

fun Task.intellijVerification(extension: KoverTaskExtension) {
    val counters = readCounterValuesFromXml(extension.xmlReportFile.get())
    val violations = extension.rules.mapNotNull { checkRule(counters, it) }

    if (violations.isNotEmpty()) {
        throw GradleException(violations.joinToString("\n"))
    }
}


private fun readCounterValuesFromXml(file: File): Map<VerificationValueType, Int> {
    val scanner = Scanner(file)
    var lineCounterLine: String? = null

    while (scanner.hasNextLine()) {
        val line = scanner.nextLine()
        if (line.startsWith("<counter type=\"LINE\"")) {
            lineCounterLine = line
        }
    }
    scanner.close()

    lineCounterLine ?: throw GradleException("No LINE counter in XML report")

    val coveredCount = lineCounterLine.substringAfter("covered=\"").substringBefore("\"").toInt()
    val missedCount = lineCounterLine.substringAfter("missed=\"").substringBefore("\"").toInt()
    val percentage = 100 * coveredCount / (coveredCount + missedCount)

    return mapOf(
        COVERED_LINES_COUNT to coveredCount,
        MISSED_LINES_COUNT to missedCount,
        COVERED_LINES_PERCENTAGE to percentage
    )
}


private fun checkRule(counters: Map<VerificationValueType, Int>, rule: VerificationRule): String? {
    val minValue = rule.minValue
    val maxValue = rule.maxValue

    val value = counters[rule.valueType] ?: throw GradleException("Not found value for counter `${rule.valueType}`")

    val ruleName = if (rule.name != null) "`${rule.name}` " else ""
    val valueTypeName = when (rule.valueType) {
        COVERED_LINES_COUNT -> "covered lines count"
        MISSED_LINES_COUNT -> "missed lines count"
        COVERED_LINES_PERCENTAGE -> "covered lines percentage"
    }

    return if (minValue != null && minValue > value) {
        "Rule ${ruleName}violated: $valueTypeName is $value, but expected minimum is $minValue"
    } else if (maxValue != null && maxValue < value) {
        "Rule ${ruleName}violated: $valueTypeName is $value, but expected maximum is $maxValue"
    } else {
        null
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
        if (!extension.isEnabled) {
            return mutableListOf()
        }

        return if (extension.coverageEngine == CoverageEngine.JACOCO) {
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

        val binary = extension.binaryReportFile.get()
        binary.parentFile.mkdirs()
        val binaryPath = binary.canonicalPath
        return "\"$binaryPath\" false true false false true \"$binaryPath.smap\"$includesString$excludesString"
    }

    private fun jacocoAgent(): MutableList<String> {
        return mutableListOf("-javaagent:${jacocoAgentReader.get().canonicalPath}=${jacocoAgentArgs()}")
    }

    private fun jacocoAgentArgs(): String {
        val binary = extension.binaryReportFile.get()
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
