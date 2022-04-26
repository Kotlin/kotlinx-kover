/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.intellij

import kotlinx.kover.api.*
import kotlinx.kover.api.VerificationValueType.COVERED_LINES_PERCENTAGE
import kotlinx.kover.engines.commons.*
import kotlinx.kover.engines.commons.Report
import kotlinx.kover.json.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.process.ExecOperations
import java.io.*
import java.math.BigDecimal
import java.math.RoundingMode

@Suppress("UNUSED_PARAMETER")
internal fun Task.intellijVerification(
    exec: ExecOperations,
    report: Report,
    rules: Iterable<VerificationRule>,
    classpath: FileCollection
) {
    val aggRequest = File(temporaryDir, "agg-request.json")
    val aggFile = File(temporaryDir, "aggregated.ic")
    aggRequest.writeAggVerifyJson(report, aggFile)
    exec.javaexec { e ->
        e.mainClass.set("com.intellij.rt.coverage.aggregate.Main")
        e.classpath = classpath
        e.args = mutableListOf(aggRequest.canonicalPath)
    }

    val verifyRequest = File(temporaryDir, "verify-request.json")
    val resultFile = File(temporaryDir, "verify-result.json")
    verifyRequest.writeVerifyJson(aggFile, resultFile, rules)
    exec.javaexec { e ->
        e.mainClass.set("com.intellij.rt.coverage.verify.Main")
        e.classpath = classpath
        e.args = mutableListOf(verifyRequest.canonicalPath)
    }

    val violations = resultFile.readJsonArray()
    if (violations.isNotEmpty()) {
        val result = processViolationsModel(violations)
        raiseViolations(result, rules)
    }
}

/*
{
  "reports": [{"ic": "path"}, ...],
  "modules": [{"output": ["path1", "path2"], "sources": ["source1",...]},... ],
  "result": [{
    "filters": {   // optional
      "include": { // optional
        "classes": [ String,... ]
      },
      "exclude": { // optional
        "classes": [ String,... ]
      }
    },
    "aggregatedReportFile": String,
  },...
  ]

  }
}
 */
private fun File.writeAggVerifyJson(
    report: Report,
    aggReport: File
) {
    writeJsonObject(mapOf(
        "reports" to report.files.map { mapOf("ic" to it) },
        "modules" to report.projects.map { mapOf("sources" to it.sources, "output" to it.outputs) },
        "result" to listOf(mapOf(
            "aggregatedReportFile" to aggReport,
            "filters" to mutableMapOf<String, Any>().also {
                if (report.includes.isNotEmpty()) {
                    it["include"] = mapOf("classes" to report.includes.map { c -> c.wildcardsToRegex() })
                }
                if (report.excludes.isNotEmpty()) {
                    it["exclude"] = mapOf("classes" to report.excludes.map { c -> c.wildcardsToRegex() })
                }
            }
        ))
    ))
}

/*
{
  "resultFile": String,
  "rules": [{
    "id": Int,
    "aggregatedReportFile": String,
    "targetType": String, // enum values: "CLASS", "PACKAGE", "ALL", (later may be added "FILE" and "FUNCTION")
    "bounds": [
      {
        "id": Int,
        "counter": String, // "LINE", "INSTRUCTION", "BRANCH"
        "valueType": String, // "MISSED", "COVERED", "MISSED_RATE", "COVERED_RATE"
        "min": BigDecimal, // optional
        "max": BigDecimal, // optional
      },...
    ]
  },...
  ]
}
 */
private fun File.writeVerifyJson(
    aggReport: File,
    result: File,
    rules: Iterable<VerificationRule>
) {
    writeJsonObject(mapOf(
        "resultFile" to result,
        "rules" to rules.map { rule ->
            mapOf(
                "id" to rule.id,
                "aggregatedReportFile" to aggReport,
                "targetType" to "ALL",
                "bounds" to rule.bounds.map { b ->
                    mutableMapOf(
                        "id" to b.id,
                        "counter" to "LINE",
                        "valueType" to b.valueTypeConverted(),
                    ).also {
                        val minValue = b.minValue
                        val maxValue = b.maxValue
                        if (minValue != null) {
                            it["min"] = b.valueAligned(minValue)
                        }
                        if (maxValue != null) {
                            it["max"] = b.valueAligned(maxValue)
                        }
                    }
                }
            )
        }
    ))
}

private fun VerificationBound.valueTypeConverted(): String {
    return when (valueType) {
        VerificationValueType.COVERED_LINES_COUNT -> "COVERED"
        VerificationValueType.MISSED_LINES_COUNT -> "MISSED"
        COVERED_LINES_PERCENTAGE -> "COVERED_RATE"
    }
}

private fun VerificationBound.valueAligned(value: Int): BigDecimal {
    return if (valueType == COVERED_LINES_PERCENTAGE) {
        value.toBigDecimal().divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP)
    } else {
        value.toBigDecimal()
    }
}

@Suppress("UNCHECKED_CAST")
private fun processViolationsModel(violations: List<Any>): ViolationsResult {
    val rules = mutableMapOf<Int, RuleViolation>()
    try {
        for (violation in violations) {
            val ruleViolation = violation as Map<String, Any>
            val ruleId = ruleViolation.getValue("id") as BigDecimal
            val boundViolations = ruleViolation.getValue("bounds") as Map<String, Any>

            val bounds = mutableMapOf<Int, BoundViolation>()
            boundViolations.forEach { (id, v) ->
                val minViolations = (v as Map<String, Map<String, Any>>)["min"]
                val maxViolations = v["max"]

                val min =
                    minViolations?.mapValues { it.value.let { v -> if (v is String) v.toBigDecimal() else v as BigDecimal } }
                        ?: emptyMap()
                val max =
                    maxViolations?.mapValues { it.value.let { v -> if (v is String) v.toBigDecimal() else v as BigDecimal } }
                        ?: emptyMap()

                bounds[id.toInt()] = BoundViolation(min, max)
            }
            rules[ruleId.toInt()] = RuleViolation(bounds)
        }
    } catch (e: Throwable) {
        throw GradleException("", e)
    }

    return ViolationsResult(rules)
}

private fun raiseViolations(result: ViolationsResult, rules: Iterable<VerificationRule>) {
    val messageBuilder = StringBuilder()
    val rulesMap = rules.associateBy { r -> r.id }

    result.ruleViolations.forEach { (ruleId, rv) ->
        val rule = rulesMap[ruleId] ?: throw Exception("")
        val ruleName = if (rule.name != null) " '${rule.name}' " else " "

        val boundsMap = rule.bounds.associateBy { b -> b.id }

        val boundMessages = rv.boundViolations.mapNotNull { (boundId, v) ->
            val bound = boundsMap[boundId] ?: throw Exception("")
            val minViolation = v.min["all"]
            val maxViolation = v.max["all"]

            if (minViolation != null) {
                "${bound.readableValueType} is ${minViolation.toRateIfNeeded(bound)}, but expected minimum is ${bound.minValue}"
            } else if (maxViolation != null) {
                "${bound.readableValueType} is ${maxViolation.toRateIfNeeded(bound)}, but expected maximum is ${bound.maxValue}"
            } else {
                null
            }
        }

        if (boundMessages.size > 1) {
            messageBuilder.append(
                "Rule${ruleName}violated:" + boundMessages.joinToString("\n  ", "\n  ")
            )
        } else {
            messageBuilder.append("Rule${ruleName}violated: ${boundMessages[0]}")
        }
    }

    throw GradleException(messageBuilder.toString())
}

private fun BigDecimal.toRateIfNeeded(bound: VerificationBound): BigDecimal {
    return if (bound.valueType == COVERED_LINES_PERCENTAGE) {
        this.multiply(ONE_HUNDRED)
    } else {
        this
    }
}

private val VerificationBound.readableValueType: String
    get() = when (valueType) {
        VerificationValueType.COVERED_LINES_COUNT -> "covered lines count"
        VerificationValueType.MISSED_LINES_COUNT -> "missed lines count"
        COVERED_LINES_PERCENTAGE -> "covered lines percentage"
    }


private data class ViolationsResult(val ruleViolations: Map<Int, RuleViolation>)

private data class RuleViolation(val boundViolations: Map<Int, BoundViolation>)

private data class BoundViolation(val min: Map<String, BigDecimal>, val max: Map<String, BigDecimal>)
