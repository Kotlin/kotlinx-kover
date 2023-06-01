/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import kotlinx.kover.gradle.plugin.util.*
import java.io.*

/**
 * A flag to enable tracking per test coverage.
 */
private const val TRACKING_PER_TEST = false

/**
 * A flag to calculate coverage for unloaded classes.
 */
private const val CALCULATE_FOR_UNLOADED_CLASSES = false

/**
 * Use data file as initial coverage.
 *
 * `false` - to overwrite previous file content.
 */
private const val APPEND_TO_DATA_FILE = true


/**
 * Create hit block only for line - adds the ability to count branches
 */
private const val LINING_ONLY_MODE = false

/**
 * Enables saving the array in the /candy field,
 * without it there will be an appeal to the hash table foreach method, which very slow.
 */
private const val ENABLE_TRACING = "idea.new.tracing.coverage=true"

/**
 * Print errors to the Gradle stdout
 */
private const val PRINT_ONLY_ERRORS = "idea.coverage.log.level=error"

/**
 * Enables ignoring constructors in classes where all methods are static.
 */
private const val IGNORE_STATIC_CONSTRUCTORS = "coverage.ignore.private.constructor.util.class=true"

/**
 * Do not count amount hits of the line, only 0 or 1 will be place into int[] - reduce byte code size
 */
private const val DO_NOT_COUNT_HIT_AMOUNT = "idea.coverage.calculate.hits=false"

internal fun buildJvmAgentArgs(
    jarFile: File,
    tempDir: File,
    rawReportFile: File,
    excludedClasses: Set<String>
): List<String> {
    val argsFile = tempDir.resolve("kover-agent.args")
    argsFile.writeAgentArgs(rawReportFile, excludedClasses)

    return mutableListOf(
        "-javaagent:${jarFile.canonicalPath}=${argsFile.canonicalPath}",
        "-D$ENABLE_TRACING",
        "-D$PRINT_ONLY_ERRORS",
        "-D$IGNORE_STATIC_CONSTRUCTORS",
        "-D$DO_NOT_COUNT_HIT_AMOUNT"
    )
}


private fun File.writeAgentArgs(rawReportFile: File, excludedClasses: Set<String>) {
    rawReportFile.parentFile.mkdirs()
    val rawReportPath = rawReportFile.canonicalPath

    printWriter().use { pw ->
        pw.appendLine(rawReportPath)
        pw.appendLine(TRACKING_PER_TEST.toString())
        pw.appendLine(CALCULATE_FOR_UNLOADED_CLASSES.toString())
        pw.appendLine(APPEND_TO_DATA_FILE.toString())
        pw.appendLine(LINING_ONLY_MODE.toString())

        if (excludedClasses.isNotEmpty()) {
            pw.appendLine("-exclude")
            excludedClasses.forEach { e ->
                pw.appendLine(e.wildcardsToRegex())
            }
        }
    }
}
