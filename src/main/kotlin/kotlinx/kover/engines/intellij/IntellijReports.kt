/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.intellij

import kotlinx.kover.api.*
import kotlinx.kover.engines.commons.*
import kotlinx.kover.engines.commons.Report
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.process.ExecOperations
import java.io.*
import java.util.*

internal fun Task.intellijReport(
    exec: ExecOperations,
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

    exec.javaexec { e ->
        e.mainClass.set("com.intellij.rt.coverage.report.Main")
        e.classpath = classpath
        e.args = mutableListOf(argsFile.canonicalPath)
    }
}

/*
JSON format:
```
{
  reports: [{ic: "path", smap: "path" [OPTIONAL]}, ...],
  modules: [{output: ["path1", "path2"], sources: ["source1", â€¦]}, {â€¦}],
  xml: "path" [OPTIONAL],
  html: "directory" [OPTIONAL],
  include: {
        classes: ["regex1", "regex2"] [OPTIONAL]
   } [OPTIONAL],
  exclude: {
        classes: ["regex1", "regex2"] [OPTIONAL]
   } [OPTIONAL],
}
```


JSON example:
```
{
  "reports": [
        {"ic": "/path/to/binary/report/result.ic"}
  ],
  "html": "/path/to/html",
  "modules": [
    {
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

    appendLine("""  "reports": [ """)
    appendLine(report.files.joinToString(",\n        ", "        ") { f ->
        """{"ic": ${f.jsonString}}"""
    })
    appendLine("""    ], """)

    xmlFile?.also {
        appendLine("""  "xml": ${it.jsonString},""")
    }
    htmlDir?.also {
        appendLine("""  "html": ${it.jsonString},""")
    }

    val includes = report.includes
    val excludes = report.excludes

    if (includes.isNotEmpty()) {
        appendLine("""  "include": {""")
        appendLine(includes.joinToString(", ", """    "classes": [""", "]") { i -> i.wildcardsToRegex().jsonString })
        appendLine("""  },""")
    }

    if (excludes.isNotEmpty()) {
        appendLine("""  "exclude": {""")
        appendLine(excludes.joinToString(", ", """    "classes": [""", "]") { e -> e.wildcardsToRegex().jsonString })
        appendLine("""  },""")
    }

    appendLine("""  "modules": [""")
    report.projects.forEachIndexed { index, aProject ->
        writeProjectReportJson(aProject, index == (report.projects.size - 1))
    }
    appendLine("""  ]""")
    appendLine("}")
}

private fun Writer.writeProjectReportJson(projectInfo: ProjectInfo, isLast: Boolean) {
    appendLine("""    {""")
    appendLine("""      "output": [""")
    appendLine(
        projectInfo.outputs.joinToString(",\n        ", "        ") { f -> f.jsonString })
    appendLine("""      ],""")
    appendLine("""      "sources": [""")

    appendLine(
        projectInfo.sources.joinToString(",\n        ", "        ") { f -> f.jsonString })
    appendLine("""      ]""")
    appendLine("""    }${if (isLast) "" else ","}""")
}

private val File.jsonString: String
    get() {
        return canonicalPath.jsonString
    }

private val String.jsonString: String
    get() {
        return '"' + replace("\\", "\\\\").replace("\"", "\\\"") + '"'
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
    var branchCounterLine: String? = null
    var instructionCounterLine: String? = null

    while (scanner.hasNextLine()) {
        val line = scanner.nextLine()
        if (line.startsWith("<counter type=\"LINE\"")) {
            lineCounterLine = line
        }
        if (line.startsWith("<counter type=\"INSTRUCTION\"")) {
            instructionCounterLine = line
        }
        if (line.startsWith("<counter type=\"BRANCH\"")) {
            branchCounterLine = line
        }
    }
    scanner.close()

    lineCounterLine ?: throw GradleException("No LINE counter in XML report")
    branchCounterLine ?: throw GradleException("No BRANCH counter in XML report")
    instructionCounterLine ?: throw GradleException("No INSTRUCTION counter in XML report")

    val (coveredLinesCount, missedLinesCount, linesPercentage) = parseCoverage(lineCounterLine)
    val (coveredBranchesCount, missedBranchesCount, branchesPercentage) = parseCoverage(branchCounterLine)
    val (coveredInstructionsCount, missedInstructionsCount, instructionsPercentage) = parseCoverage(instructionCounterLine)

    return mapOf(
        VerificationValueType.COVERED_LINES_COUNT to coveredLinesCount,
        VerificationValueType.MISSED_LINES_COUNT to missedLinesCount,
        VerificationValueType.COVERED_LINES_PERCENTAGE to linesPercentage,
        VerificationValueType.COVERED_BRANCHES_COUNT to coveredBranchesCount,
        VerificationValueType.MISSED_BRANCHES_COUNT to missedBranchesCount,
        VerificationValueType.COVERED_BRANCHES_PERCENTAGE to branchesPercentage,
        VerificationValueType.COVERED_INSTRUCTIONS_COUNT to coveredInstructionsCount,
        VerificationValueType.MISSED_INSTRUCTIONS_COUNT to missedInstructionsCount,
        VerificationValueType.COVERED_INSTRUCTIONS_PERCENTAGE to instructionsPercentage,
    )
}

private fun parseCoverage(line: String): Triple<Int, Int, Int> {
    val coveredCount = line.substringAfter("covered=\"").substringBefore("\"").toInt()
    val missedCount = line.substringAfter("missed=\"").substringBefore("\"").toInt()
    val percentage = if ((coveredCount + missedCount) > 0) 100 * coveredCount / (coveredCount + missedCount) else 0
    return Triple(coveredCount, missedCount, percentage)
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
        VerificationValueType.COVERED_BRANCHES_COUNT -> "covered branches count"
        VerificationValueType.MISSED_BRANCHES_COUNT -> "missed branches count"
        VerificationValueType.COVERED_BRANCHES_PERCENTAGE -> "covered branches percentage"
        VerificationValueType.COVERED_INSTRUCTIONS_COUNT -> "covered instructions count"
        VerificationValueType.MISSED_INSTRUCTIONS_COUNT -> "missed instructions count"
        VerificationValueType.COVERED_INSTRUCTIONS_PERCENTAGE -> "covered instructions percentage"
    }

    return if (minValue != null && minValue > value) {
        "$valueTypeName is $value, but expected minimum is $minValue"
    } else if (maxValue != null && maxValue < value) {
        "$valueTypeName is $value, but expected maximum is $maxValue"
    } else {
        null
    }
}
