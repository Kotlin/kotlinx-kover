/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import groovy.lang.Closure
import groovy.lang.GroovyObject
import kotlinx.kover.features.jvm.KoverFeatures.koverWildcardToRegex
import kotlinx.kover.gradle.plugin.commons.ReportContext
import java.io.File


internal fun ReportContext.callAntReport(
    reportName: String,
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

    val filesByClassName = mutableMapOf<String, File>()
    files.outputs.forEach { output ->
        output.walk().forEach { file ->
            if (file.isFile && file.name.endsWith(CLASS_FILE_EXTENSION)) {
                val className = file.toRelativeString(output).filenameToClassname()
                filesByClassName[className] = file
            }
        }
    }

    val classes = if (filters.excludesClasses.isNotEmpty() || filters.includesClasses.isNotEmpty()) {
        val excludeRegexes = filters.excludesClasses.map { koverWildcardToRegex(it).toRegex() }
        val includeRegexes = filters.includesClasses.map { koverWildcardToRegex(it).toRegex() }

        filesByClassName.filterKeys { className ->
            ((includeRegexes.isEmpty() || includeRegexes.any { regex -> className.matches(regex) })
                    // if the exclusion rules are declared, then the file should not fit any of them
                    && excludeRegexes.none { regex -> className.matches(regex) })
        }.values
    } else {
        filesByClassName.values
    }


    builder.invokeWithBody("jacocoReport") {
        invokeWithBody("executiondata") {
            services.objects.fileCollection().from(files.reports).addToAntBuilder(this, "resources")
        }
        invokeWithBody("structure", mapOf("name" to reportName)) {
            invokeWithBody("sourcefiles") {
                services.objects.fileCollection().from(files.sources).addToAntBuilder(this, "resources")
            }
            invokeWithBody("classfiles") {
                services.objects.fileCollection().from(classes).addToAntBuilder(this, "resources")
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

/**
 * Replaces characters `|` or `\` to `.` and remove postfix `.class`.
 */
private fun String.filenameToClassname(): String {
    return this.replace(File.separatorChar, '.').removeSuffix(CLASS_FILE_EXTENSION)
}

/**
 * Extension of class-files.
 */
private const val CLASS_FILE_EXTENSION = ".class"
