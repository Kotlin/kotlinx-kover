/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.jacoco

import groovy.lang.Closure
import groovy.lang.GroovyObject
import kotlinx.kover.api.CounterType
import kotlinx.kover.api.VerificationTarget
import kotlinx.kover.api.VerificationValueType
import kotlinx.kover.engines.commons.ONE_HUNDRED
import kotlinx.kover.engines.commons.ReportVerificationRule
import kotlinx.kover.tasks.ProjectFiles
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.internal.reflect.JavaMethod
import java.io.File
import java.math.BigDecimal
import java.util.*

internal fun Task.jacocoReport(
    projectFiles: ProjectFiles,
    xmlFile: File?,
    htmlDir: File?,
    classpath: FileCollection
) {
    callJacocoAntReportTask(projectFiles, classpath) {
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
    projectFiles: ProjectFiles,
    rules: List<ReportVerificationRule>,
    classpath: FileCollection
): String? {
    callJacocoAntReportTask(projectFiles, classpath) {
        invokeWithBody("check", mapOf("failonviolation" to "false", "violationsproperty" to "jacocoErrors")) {
            rules.forEach {
                val entityType = when (it.target) {
                    VerificationTarget.ALL -> "BUNDLE"
                    VerificationTarget.CLASS -> "CLASS"
                    VerificationTarget.PACKAGE -> "PACKAGE"
                }
                invokeWithBody("rule", mapOf("element" to entityType)) {
                    it.bounds.forEach { b ->
                        val limitArgs = mutableMapOf<String, String>()
                        limitArgs["counter"] = when (b.metric) {
                            CounterType.LINE -> "LINE"
                            CounterType.INSTRUCTION -> "INSTRUCTION"
                            CounterType.BRANCH -> "BRANCH"
                        }

                        var min: BigDecimal? = b.minValue
                        var max: BigDecimal? = b.maxValue
                        when (b.valueType) {
                            VerificationValueType.COVERED_COUNT -> {
                                limitArgs["value"] = "COVEREDCOUNT"
                            }

                            VerificationValueType.MISSED_COUNT -> {
                                limitArgs["value"] = "MISSEDCOUNT"
                            }

                            VerificationValueType.COVERED_PERCENTAGE -> {
                                limitArgs["value"] = "COVEREDRATIO"
                                min = min?.divide(ONE_HUNDRED)
                                max = max?.divide(ONE_HUNDRED)
                            }

                            VerificationValueType.MISSED_PERCENTAGE -> {
                                limitArgs["value"] = "MISSEDRATIO"
                                min = min?.divide(ONE_HUNDRED)
                                max = max?.divide(ONE_HUNDRED)
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

    return ant.violations?.orderViolations()
}

private fun Task.callJacocoAntReportTask(
    projectFiles: ProjectFiles,
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

    builder.invokeWithBody("jacocoReport") {
        invokeWithBody("executiondata") {
            project.files(projectFiles.binaryReportFiles).addToAntBuilder(this, "resources")
        }
        invokeWithBody("structure", mapOf("name" to project.path)) {
            invokeWithBody("sourcefiles") {
                project.files(projectFiles.sources).addToAntBuilder(this, "resources")
            }
            invokeWithBody("classfiles") {
                project.files(projectFiles.outputs).addToAntBuilder(this, "resources")
            }
        }
        block()
    }
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

private fun String.orderViolations(): String {
    val treeSet = TreeSet<String>()
    this.lineSequence().forEach { treeSet += it }
    return treeSet.joinToString("\n")
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

