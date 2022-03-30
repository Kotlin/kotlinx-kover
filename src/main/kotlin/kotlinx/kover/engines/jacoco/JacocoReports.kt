/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.jacoco

import groovy.lang.Closure
import groovy.lang.GroovyObject
import kotlinx.kover.api.VerificationRule
import kotlinx.kover.api.VerificationValueType
import kotlinx.kover.engines.commons.Report
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.internal.reflect.JavaMethod
import java.io.File
import java.math.BigDecimal
import java.util.Hashtable

private fun Task.callJacocoAntReportTask(
    report: Report,
    classpath: FileCollection,
    block: GroovyObject.() -> Unit
) {
    val builder = ant
    builder.invokeMethod(
        "taskdef",
        mapOf(
            "name" to "jacocoReport",
            "classname" to "org.jacoco.ant.ReportTask",
            "classpath" to classpath.asPath
        )
    )

    val sources: MutableList<File> = mutableListOf()
    val outputs: MutableList<File> = mutableListOf()
    report.projects.forEach { projectInfo ->
        sources.addAll(projectInfo.sources)
        outputs.addAll(projectInfo.outputs)
    }

    builder.invokeWithBody("jacocoReport") {
        invokeWithBody("executiondata") {
            project.files(report.files).addToAntBuilder(this, "resources")
        }
        invokeWithBody("structure", mapOf("name" to project.name)) {
            invokeWithBody("sourcefiles") {
                project.files(sources).addToAntBuilder(this, "resources")
            }
            invokeWithBody("classfiles") {
                project.files(outputs).addToAntBuilder(this, "resources")
            }
        }
        block()
    }
}

internal fun Task.jacocoReport(
    report: Report,
    xmlFile: File?,
    htmlDir: File?,
    classpath: FileCollection
) {
    callJacocoAntReportTask(report, classpath) {
        if (xmlFile != null) {
            xmlFile.parentFile.mkdirs()
            invokeMethod("xml", mapOf("destfile" to xmlFile))
        }
        if (htmlDir != null) {
            htmlDir.mkdirs()
            invokeMethod("html", mapOf("destdir" to htmlDir))
        }
    }
}


internal fun Task.jacocoVerification(
    report: Report,
    rules: Iterable<VerificationRule>,
    classpath: FileCollection
) {
    callJacocoAntReportTask(report, classpath) {
        invokeWithBody("check", mapOf("failonviolation" to "false", "violationsproperty" to "jacocoErrors")) {
            rules.forEach {
                invokeWithBody("rule", mapOf("element" to "BUNDLE")) {
                    it.bounds.forEach { b ->
                        val limitArgs = mutableMapOf<String, String>()
                        var min: BigDecimal? = b.minValue?.toBigDecimal()
                        var max: BigDecimal? = b.maxValue?.toBigDecimal()
                        when (b.valueType) {
                            VerificationValueType.COVERED_LINES_COUNT -> {
                                limitArgs["value"] = "COVEREDCOUNT"
                                limitArgs["counter"] = "LINE"
                            }
                            VerificationValueType.MISSED_LINES_COUNT -> {
                                limitArgs["value"] = "MISSEDCOUNT"
                                limitArgs["counter"] = "LINE"
                            }
                            VerificationValueType.COVERED_LINES_PERCENTAGE -> {
                                limitArgs["value"] = "COVEREDRATIO"
                                limitArgs["counter"] = "LINE"
                                min = min?.divide(BigDecimal(100))
                                max = max?.divide(BigDecimal(100))
                            }
                            VerificationValueType.COVERED_BRANCHES_COUNT -> {
                                limitArgs["value"] = "COVEREDCOUNT"
                                limitArgs["counter"] = "BRANCH"
                            }
                            VerificationValueType.MISSED_BRANCHES_COUNT -> {
                                limitArgs["value"] = "MISSEDCOUNT"
                                limitArgs["counter"] = "BRANCH"
                            }
                            VerificationValueType.COVERED_BRANCHES_PERCENTAGE -> {
                                limitArgs["value"] = "COVEREDRATIO"
                                limitArgs["counter"] = "BRANCH"
                                min = min?.divide(BigDecimal(100))
                                max = max?.divide(BigDecimal(100))
                            }
                            VerificationValueType.COVERED_INSTRUCTIONS_COUNT -> {
                                limitArgs["value"] = "COVEREDCOUNT"
                                limitArgs["counter"] = "INSTRUCTION"
                            }
                            VerificationValueType.MISSED_INSTRUCTIONS_COUNT -> {
                                limitArgs["value"] = "MISSEDCOUNT"
                                limitArgs["counter"] = "INSTRUCTION"
                            }
                            VerificationValueType.COVERED_INSTRUCTIONS_PERCENTAGE -> {
                                limitArgs["value"] = "COVEREDRATIO"
                                limitArgs["counter"] = "INSTRUCTION"
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

    ant.violations?.let { throw GradleException(it) }
}

private val GroovyObject.violations: String?
    get() {
        val project = getProperty("project")
        val properties = JavaMethod.of(
            project,
            Hashtable::class.java, "getProperties"
        ).invoke(project, *arrayOfNulls(0))
        return properties["jacocoErrors"] as String?
    }

@Suppress("UNUSED_PARAMETER")
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

