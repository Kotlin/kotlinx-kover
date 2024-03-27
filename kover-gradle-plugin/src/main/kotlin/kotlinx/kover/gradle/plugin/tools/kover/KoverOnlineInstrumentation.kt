/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import java.io.File

internal fun buildJvmAgentArgs(
    jarFile: File,
    tempDir: File,
    binReportFile: File,
    excludedClasses: Set<String>
): List<String> {
    val argsFile = tempDir.resolve("kover-agent.args")
    argsFile.writeAgentArgs(binReportFile, excludedClasses)

    return mutableListOf("-javaagent:${jarFile.canonicalPath}=file:${argsFile.canonicalPath}")
}

private fun File.writeAgentArgs(binReportFile: File, excludedClasses: Set<String>) {
    binReportFile.parentFile.mkdirs()
    val binReportPath = binReportFile.canonicalPath

    printWriter().use { pw ->
        pw.append("report.file=").appendLine(binReportPath)
        excludedClasses.forEach { e ->
            pw.append("exclude=").appendLine(e)
        }
    }
}
