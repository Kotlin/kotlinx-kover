/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.intellij

import kotlinx.kover.api.*
import kotlinx.kover.engines.commons.*
import org.gradle.api.Task
import java.io.*

private const val trackingPerTest = false // a flag to enable tracking per test coverage
private const val calculateForUnloadedClasses = false // a flag to calculate coverage for unloaded classes
private const val appendToDataFile = true // a flag to use data file as initial coverage
private const val samplingMode = false //a flag to run coverage in sampling mode or in tracing mode otherwise

internal fun Task.buildIntellijAgentJvmArgs(jarFile: File, reportFile: File, classFilter: KoverClassFilter): MutableList<String> {
    val argsFile = File(temporaryDir, "intellijagent.args")
    argsFile.writeAgentArgs(reportFile, classFilter)

    return mutableListOf(
        "-javaagent:${jarFile.canonicalPath}=${argsFile.canonicalPath}",
        "-Didea.new.sampling.coverage=true",
        "-Didea.new.tracing.coverage=true",
        "-Didea.coverage.log.level=error",
        "-Dcoverage.ignore.private.constructor.util.class=true"
    )
}

private fun File.writeAgentArgs(reportFile: File, classFilter: KoverClassFilter) {
    reportFile.parentFile.mkdirs()
    val binaryPath = reportFile.canonicalPath

    printWriter().use { pw ->
        pw.appendLine(binaryPath)
        pw.appendLine(trackingPerTest.toString())
        pw.appendLine(calculateForUnloadedClasses.toString())
        pw.appendLine(appendToDataFile.toString())
        pw.appendLine(samplingMode.toString())
        classFilter.includes.forEach { i ->
            pw.appendLine(i.wildcardsToRegex())
        }

        if (classFilter.excludes.isNotEmpty()) {
            pw.appendLine("-exclude")
        }

        classFilter.excludes.forEach { e ->
            pw.appendLine(e.wildcardsToRegex())
        }
    }
}
