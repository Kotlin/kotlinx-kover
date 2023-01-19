/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import groovy.lang.*
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.ReportFilters
import kotlinx.kover.gradle.plugin.util.*
import org.gradle.api.*
import org.gradle.api.file.*
import java.io.*


internal fun ReportContext.callAntReport(
    filters: ReportFilters,
    block: GroovyObject.() -> Unit
) {
    val builder = services.antBuilder
    builder.invokeMethod(
        "taskdef",
        mapOf(
            "name" to "jacocoReport",
            "classname" to "org.jacoco.ant.ReportTask",
            "classpath" to classpath.asPath
        )
    )


    val filteredOutput = if (filters.excludesClasses.isNotEmpty() || filters.includesClasses.isNotEmpty()) {
        val excludeRegexes = filters.excludesClasses.map { Regex(it.wildcardsToClassFileRegex()) }
        val includeRegexes = filters.includesClasses.map { Regex(it.wildcardsToClassFileRegex()) }
        services.objects.fileCollection().from(files.outputs).asFileTree.filter { file ->
            // the `canonicalPath` is used because a `File.separatorChar` was used to construct the class-file regex
            val path = file.canonicalPath
            // if the inclusion rules are declared, then the file must fit at least one of them
            (includeRegexes.isEmpty() || includeRegexes.any { regex -> path.matches(regex) })
                    // if the exclusion rules are declared, then the file should not fit any of them
                    && excludeRegexes.none { regex -> path.matches(regex) }
        }
    } else {
        services.objects.fileCollection().from(files.outputs)
    }


    builder.invokeWithBody("jacocoReport") {
        invokeWithBody("executiondata") {
            services.objects.fileCollection().from(files.reports).addToAntBuilder(this, "resources")
        }
        invokeWithBody("structure", mapOf("name" to projectPath)) {
            invokeWithBody("sourcefiles") {
                services.objects.fileCollection().from(files.sources).addToAntBuilder(this, "resources")
            }
            invokeWithBody("classfiles") {
                filteredOutput.addToAntBuilder(this, "resources")
            }
        }
        block()
    }
}


@Suppress("UNUSED_PARAMETER")
internal inline fun GroovyObject.invokeWithBody(
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
