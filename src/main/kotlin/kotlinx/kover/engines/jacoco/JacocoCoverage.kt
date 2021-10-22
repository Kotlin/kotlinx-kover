/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.jacoco

import groovy.lang.*
import kotlinx.kover.adapters.*
import kotlinx.kover.api.*
import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.tasks.testing.*
import java.math.*


internal fun Project.createJacocoConfig(koverExtension: KoverExtension): Configuration {
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

internal fun Task.jacocoAntBuilder(configuration: Configuration): GroovyObject {
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

internal fun Task.callJacocoAntReportTask(
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

internal fun Task.jacocoReport(builder: GroovyObject, extension: KoverTaskExtension) {
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


internal fun Task.jacocoVerification(builder: GroovyObject, extension: KoverTaskExtension) {
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

