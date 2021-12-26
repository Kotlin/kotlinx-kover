/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.intellij

import kotlinx.kover.api.*
import kotlinx.kover.engines.commons.*
import kotlinx.kover.engines.commons.Report
import kotlinx.kover.engines.commons.ReportFiles
import org.gradle.api.*
import org.gradle.api.file.*
import java.io.*
import java.util.*

internal fun Task.intellijReport(
    report: Report,
    xmlFile: File?,
    htmlDir: File?,
    classpath: FileCollection
) {
    xmlFile?.let {
        xmlFile.parentFile.mkdirs()
    }

    htmlDir?.let {
        htmlDir.mkdirs()
    }

    val argsFile = File(temporaryDir, "intellijreport.json")
    argsFile.printWriter().use { pw ->
        pw.writeReportsJson(report, xmlFile, htmlDir)
    }

    project.javaexec { e ->
        e.mainClass.set("com.intellij.rt.coverage.report.Main")
        e.classpath = classpath
        e.args = mutableListOf(argsFile.canonicalPath)
    }

    project.copyIntellijErrorLog(project.layout.buildDirectory.get().file("kover/errors/$name.log").asFile)
}

internal fun Project.copyIntellijErrorLog(toFile: File, customDirectory: File? = null) {
    var errorLog = customDirectory?.let { File(it, "coverage-error.log") }

    if (errorLog == null || !errorLog.exists()) {
        errorLog = File(projectDir, "coverage-error.log")
    }

    if (errorLog.exists() && errorLog.isFile) {
        errorLog.copyTo(toFile, true)
        errorLog.delete()
    }
}

/*
JSON format:
```
{
  "modules": [
    { "reports": [
        {"ic": "path to ic binary file", "smap": "path to source map file"}
      ] [OPTIONAL, absence means that all classes were not covered],
      "output": ["outputRoot1", "outputRoot2"],
      "sources": ["sourceRoot1", "sourceRoot2"]
    }
  ],
  "xml": "path to xml file" [OPTIONAL],
  "html": "path to html directory" [OPTIONAL]
}
```


JSON example:
```
{
  "html": "/path/to/html",
  "modules": [
    { "reports": [
        {"ic": "/path/to/binary/report/result.ic", "smap": "/path/to/binary/report/result.ic.smap"}
      ],
      "output": [
        "/build/output"
      ],
      "sources": [
        "/sources/java",
        "/sources/kotlin"
      ]
    }
  ]
}
```
 */
private fun Writer.writeReportsJson(
    report: Report,
    xmlFile: File?,
    htmlDir: File?
) {
    appendLine("{")

    xmlFile?.also {
        appendLine("""  "xml": "${it.safePath()}",""")
    }
    htmlDir?.also {
        appendLine("""  "html": "${it.safePath()}",""")
    }
    appendLine("""  "modules": [""")
    report.projects.forEachIndexed { index, aProject ->
        writeProjectReportJson(report.files, aProject, index == (report.projects.size - 1))
    }
    appendLine("""  ]""")
    appendLine("}")
}

private fun Writer.writeProjectReportJson(reportFiles: Iterable<ReportFiles>, projectInfo: ProjectInfo, isLast: Boolean) {
    appendLine("""    { "reports": [ """)

    appendLine(reportFiles.joinToString(",\n        ", "        ") { f ->
        """{"ic": "${f.binary.safePath()}", "smap": "${f.smap!!.safePath()}"}"""
    })

    appendLine("""      ], """)
    appendLine("""      "output": [""")
    appendLine(
        projectInfo.outputs.joinToString(",\n        ", "        ") { f -> '"' + f.safePath() + '"' })
    appendLine("""      ],""")
    appendLine("""      "sources": [""")

    appendLine(
        projectInfo.sources.joinToString(",\n        ", "        ") { f -> '"' + f.safePath() + '"' })
    appendLine("""      ]""")
    appendLine("""    }${if (isLast) "" else ","}""")
}

private fun File.safePath(): String {
    return canonicalPath.replace("\\", "\\\\").replace("\"", "\\\"")
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
    val percentage = if ((coveredCount + missedCount) > 0) 100 * coveredCount / (coveredCount + missedCount) else 0

    return mapOf(
        VerificationValueType.COVERED_LINES_COUNT to coveredCount,
        VerificationValueType.MISSED_LINES_COUNT to missedCount,
        VerificationValueType.COVERED_LINES_PERCENTAGE to percentage
    )
}


private fun Task.checkRule(counters: Map<VerificationValueType, Int>, rule: VerificationRule): String? {
    val boundsViolations = rule.bounds.mapNotNull { it.check(counters) }

    val ruleName = if (rule.name != null) "'${rule.name}' " else ""
    return if (boundsViolations.size > 1) {
        "Rule ${ruleName}violated for '${project.name}':" + boundsViolations.joinToString("\n  ", "\n  ")
    } else if (boundsViolations.size == 1) {
        "Rule ${ruleName}violated for '${project.name}': ${boundsViolations[0]}"
    } else {
        null
    }
}

private fun VerificationBound.check(counters: Map<VerificationValueType, Int>): String? {
    val minValue = this.minValue
    val maxValue = this.maxValue
    val valueType = this.valueType

    val value = counters[valueType] ?: throw GradleException("Not found value for counter '${valueType}'")

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
