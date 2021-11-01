/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.intellij

import kotlinx.kover.api.*
import org.gradle.api.*
import org.gradle.api.file.*
import java.io.*
import java.util.*

internal fun Task.intellijReport(
    binaryReportFiles: Iterable<File>,
    sources: Iterable<File>,
    outputs: Iterable<File>,
    xmlFile: File?,
    htmlDir: File?,
    classpath: FileCollection
) {
    val xmlFilePath = if (xmlFile != null) {
        xmlFile.parentFile.mkdirs()
        xmlFile.canonicalPath
    } else {
        ""
    }
    val htmlDirPath = if (htmlDir != null) {
        htmlDir.mkdirs()
        htmlDir.canonicalPath
    } else {
        ""
    }

    val argsFile = File(temporaryDir, "intellijreport.args")
    argsFile.printWriter().use { pw ->
        for (binary in binaryReportFiles) {
            pw.appendLine(binary.canonicalPath)
            pw.appendLine("${binary.canonicalPath}.smap")
        }
        pw.appendLine()
        sources.forEach { src -> pw.appendLine(src.canonicalPath) }
        pw.appendLine()
        outputs.forEach { out -> pw.appendLine(out.canonicalPath) }
        pw.appendLine()
        pw.appendLine(xmlFilePath)
        pw.appendLine(htmlDirPath)
    }

    project.javaexec { e ->
        e.mainClass.set("com.intellij.rt.coverage.report.Main")
        e.classpath = classpath
        e.args = mutableListOf(argsFile.canonicalPath)
    }
}

internal fun Task.intellijVerification(
    xmlFile: File,
    rules: Iterable<VerificationRule>
) {
    val counters = readCounterValuesFromXml(xmlFile)
    val violations = rules.mapNotNull { checkRule(counters, it) }

    if (violations.isNotEmpty()) {
        throw GradleException(violations.joinToString("\n"))
    }
}

private fun readCounterValuesFromXml(file: File): Map<VerificationValueType, Int> {
    val scanner = Scanner(file)
    var lineCounterLine: String? = null

    while (scanner.hasNextLine()) {
        val line = scanner.nextLine()
        if (line.startsWith("<counter type=\"LINE\"")) {
            lineCounterLine = line
        }
    }
    scanner.close()

    lineCounterLine ?: throw GradleException("No LINE counter in XML report")

    val coveredCount = lineCounterLine.substringAfter("covered=\"").substringBefore("\"").toInt()
    val missedCount = lineCounterLine.substringAfter("missed=\"").substringBefore("\"").toInt()
    val percentage = 100 * coveredCount / (coveredCount + missedCount)

    return mapOf(
        VerificationValueType.COVERED_LINES_COUNT to coveredCount,
        VerificationValueType.MISSED_LINES_COUNT to missedCount,
        VerificationValueType.COVERED_LINES_PERCENTAGE to percentage
    )
}


private fun Task.checkRule(counters: Map<VerificationValueType, Int>, rule: VerificationRule): String? {
    val boundsViolations = rule.bounds.mapNotNull { it.check(counters) }

    val ruleName = if (rule.name != null) "`${rule.name}` " else ""
    return if (boundsViolations.size > 1) {
        "Rule ${ruleName}violated for `${project.name}`:" + boundsViolations.joinToString("\n  ", "\n  ")
    } else if (boundsViolations.size == 1) {
        "Rule ${ruleName}violated for `${project.name}`: ${boundsViolations[0]}"
    } else {
        null
    }
}

private fun VerificationBound.check(counters: Map<VerificationValueType, Int>): String? {
    val minValue = this.minValue
    val maxValue = this.maxValue
    val valueType = this.valueType

    val value = counters[valueType] ?: throw GradleException("Not found value for counter `${valueType}`")

    val valueTypeName = when (valueType) {
        VerificationValueType.COVERED_LINES_COUNT -> "covered lines count"
        VerificationValueType.MISSED_LINES_COUNT -> "missed lines count"
        VerificationValueType.COVERED_LINES_PERCENTAGE -> "covered lines percentage"
    }

    return if (minValue != null && minValue > value) {
        "$valueTypeName is $value, but expected minimum is $minValue"
    } else if (maxValue != null && maxValue < value) {
        "$valueTypeName is $value, but expected maximum is $maxValue"
    } else {
        null
    }
}
