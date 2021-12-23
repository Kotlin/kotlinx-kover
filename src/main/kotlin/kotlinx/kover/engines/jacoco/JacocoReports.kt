/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.jacoco

import groovy.lang.*
import kotlinx.kover.api.*
import kotlinx.kover.engines.commons.Report
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.internal.reflect.*
import java.io.*
import java.math.*
import java.util.*

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

    val binaries: List<File> = report.files.map(kotlinx.kover.engines.commons.ReportFiles::binary)

    val sources: MutableList<File> = mutableListOf()
    val outputs: MutableList<File> = mutableListOf()
    report.projects.forEach { projectInfo ->
        sources.addAll(projectInfo.sources)
        outputs.addAll(projectInfo.outputs)
    }

    builder.invokeWithBody("jacocoReport") {
        invokeWithBody("executiondata") {
            project.files(binaries).addToAntBuilder(this, "resources")
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
                        val limitArgs = mutableMapOf("counter" to "LINE")
                        var min: BigDecimal? = b.minValue?.toBigDecimal()
                        var max: BigDecimal? = b.maxValue?.toBigDecimal()
                        when (b.valueType) {
                            VerificationValueType.COVERED_LINES_COUNT -> {
                                limitArgs["value"] = "COVEREDCOUNT"
                            }
                            VerificationValueType.MISSED_LINES_COUNT -> {
                                limitArgs["value"] = "MISSEDCOUNT"
                            }
                            VerificationValueType.COVERED_LINES_PERCENTAGE -> {
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

