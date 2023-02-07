/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import kotlinx.kover.gradle.plugin.util.*
import java.io.*

/**
 * A flag to enable tracking per test coverage
 */
private const val trackingPerTest = false

/**
 * A flag to calculate coverage for unloaded classes
 */
private const val calculateForUnloadedClasses = false

/**
 * Use data file as initial coverage.
 *
 * `false` - if overwrite TODO
 */
private const val appendToDataFile = true

//
/**
 * create hit block only for line,  - adds the ability to count branches
 */
private const val liningOnlyMode = false

/**
 * TODO
 */
private const val a2 = "idea.new.tracing.coverage=true"

/**
 *
 */
private const val a3 = "idea.coverage.log.level=error"

/**
 *
 */
private const val a4 = "coverage.ignore.private.constructor.util.class=true"

/**
 * Do not count amount hits of the line, only 0 or 1 will be place into int[] - reduce byte code size
 */
private const val doNotCountHitAmount = "idea.coverage.calculate.hits=false"

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
        "-D$a2",
        "-D$a3",
        "-D$a4",
        "-D$doNotCountHitAmount"
    )
}


private fun File.writeAgentArgs(rawReportFile: File, excludedClasses: Set<String>) {
    rawReportFile.parentFile.mkdirs()
    val rawReportPath = rawReportFile.canonicalPath

    printWriter().use { pw ->
        pw.appendLine(rawReportPath)
        pw.appendLine(trackingPerTest.toString())
        pw.appendLine(calculateForUnloadedClasses.toString())
        pw.appendLine(appendToDataFile.toString())
        pw.appendLine(liningOnlyMode.toString())

        if (excludedClasses.isNotEmpty()) {
            pw.appendLine("-exclude")
            excludedClasses.forEach { e ->
                pw.appendLine(e.wildcardsToRegex())
            }
        }
    }
}
