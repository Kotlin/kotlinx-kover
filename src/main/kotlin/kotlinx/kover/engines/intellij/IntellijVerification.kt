/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.intellij

import kotlinx.kover.api.*
import kotlinx.kover.api.VerificationValueType.*
import kotlinx.kover.engines.commons.*
import kotlinx.kover.json.*
import kotlinx.kover.tasks.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.process.ExecOperations
import java.io.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.TreeMap

internal fun Task.intellijVerification(
    exec: ExecOperations,
    projectFiles: Map<String, ProjectFiles>,
    classFilter: KoverClassFilter,
    rules: List<ReportVerificationRule>,
    classpath: FileCollection
): String? {
    val aggRequest = File(temporaryDir, "agg-request.json")
    val groupedRules = groupRules(classFilter, rules)
    aggRequest.writeAggJson(projectFiles, groupedRules)
    exec.javaexec {
        mainClass.set("com.intellij.rt.coverage.aggregate.Main")
        this@javaexec.classpath = classpath
        args = mutableListOf(aggRequest.canonicalPath)
    }

    val verifyRequest = File(temporaryDir, "verify-request.json")
    val verifyResponseFile = File(temporaryDir, "verify-result.json")
    verifyRequest.writeVerifyJson(groupedRules, verifyResponseFile)
    exec.javaexec {
        mainClass.set("com.intellij.rt.coverage.verify.Main")
        this@javaexec.classpath = classpath
        args = mutableListOf(verifyRequest.canonicalPath)
    }

    val violations = verifyResponseFile.readJsonObject()
    return if (violations.isNotEmpty()) {
        val result = processViolationsModel(violations)
        raiseViolations(result, rules)
    } else {
        null
    }
}

private data class RulesGroup(
    val aggFile: File,
    val filters: KoverClassFilter,
    val rules: List<ReportVerificationRule>
)

private fun Task.groupRules(
    commonClassFilter: KoverClassFilter,
    allRules: List<ReportVerificationRule>
): List<RulesGroup> {
    val result = mutableListOf<RulesGroup>()
    val commonAggFile = File(temporaryDir, "aggregated-common.ic")
    val commonRules = mutableListOf<ReportVerificationRule>()
    result += RulesGroup(commonAggFile, commonClassFilter, commonRules)

    allRules.forEach {
        if (it.filters == null) {
            commonRules += it
        } else {
            result += RulesGroup(File(temporaryDir, "aggregated-${result.size}.ic"), it.filters, listOf(it))
        }
    }
    return result
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
private fun File.writeAggJson(
    projectFiles: Map<String, ProjectFiles>,
    groups: List<RulesGroup>
) {
    writeJsonObject(mapOf(
        "reports" to projectFiles.flatMap { it.value.binaryReportFiles }.map { mapOf("ic" to it) },
        "modules" to projectFiles.map { mapOf("sources" to it.value.sources, "output" to it.value.outputs) },
        "result" to groups.map { group ->
            mapOf(
                "aggregatedReportFile" to group.aggFile,
                "filters" to mutableMapOf<String, Any>().also {
                    if (group.filters.includes.isNotEmpty()) {
                        it["include"] =
                            mapOf("classes" to group.filters.includes.map { c -> c.wildcardsToRegex() })
                    }
                    if (group.filters.excludes.isNotEmpty()) {
                        it["exclude"] =
                            mapOf("classes" to group.filters.excludes.map { c -> c.wildcardsToRegex() })
                    }
                }
            )
        }
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
    groups: List<RulesGroup>,
    result: File,
) {
    val rulesArray = mutableListOf<Map<String, Any>>()

    groups.forEach { group ->
        group.rules.forEach { rule ->
            rulesArray += mapOf(
                "id" to rule.id,
                "aggregatedReportFile" to group.aggFile,
                "targetType" to rule.targetToReporter(),
                "bounds" to rule.bounds.map { b ->
                    mutableMapOf(
                        "id" to b.id,
                        "counter" to b.counterToReporter(),
                        "valueType" to b.valueTypeToReporter(),
                    ).also {
                        val minValue = b.minValue
                        val maxValue = b.maxValue
                        if (minValue != null) {
                            it["min"] = b.valueToReporter(minValue)
                        }
                        if (maxValue != null) {
                            it["max"] = b.valueToReporter(maxValue)
                        }
                    }
                }
            )
        }
    }

    writeJsonObject(mapOf("resultFile" to result, "rules" to rulesArray))
}

private fun ReportVerificationRule.targetToReporter(): String {
    return when (target) {
        VerificationTarget.ALL -> "ALL"
        VerificationTarget.CLASS -> "CLASS"
        VerificationTarget.PACKAGE -> "PACKAGE"
    }
}

private fun ReportVerificationBound.counterToReporter(): String {
    return when (metric) {
        CounterType.LINE -> "LINE"
        CounterType.INSTRUCTION -> "INSTRUCTION"
        CounterType.BRANCH -> "BRANCH"
    }
}

private fun ReportVerificationBound.valueTypeToReporter(): String {
    return when (valueType) {
        COVERED_COUNT -> "COVERED"
        MISSED_COUNT -> "MISSED"
        COVERED_PERCENTAGE -> "COVERED_RATE"
        MISSED_PERCENTAGE -> "MISSED_RATE"
    }
}

private fun ReportVerificationBound.valueToReporter(value: BigDecimal): BigDecimal {
    return if (valueType == COVERED_PERCENTAGE || valueType == MISSED_PERCENTAGE) {
        value.divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP)
    } else {
        value
    }
}

@Suppress("UNCHECKED_CAST")
private fun processViolationsModel(violations: Map<String, Any>): ViolationsResult {
    val rules = TreeMap<Int, RuleViolation>()
    try {
        violations.forEach { (ruleId, boundViolations) ->
            val bounds = TreeMap<Int, BoundViolation>()
            (boundViolations as Map<String, Any>).forEach { (id, v) ->
                val minViolations = (v as Map<String, Map<String, Any>>)["min"]
                val maxViolations = v["max"]

                val min = minViolations
                    ?.mapValues { it.value.let { v -> if (v is String) v.toBigDecimal() else v as BigDecimal } }
                    //make stable order for entity names
                    ?.let { TreeMap(it) }
                    ?: emptyMap()


                val max = maxViolations
                    ?.mapValues { it.value.let { v -> if (v is String) v.toBigDecimal() else v as BigDecimal } }
                    //make stable order for entity names
                    ?.let { TreeMap(it) }
                    ?: emptyMap()

                bounds[id.toInt()] = BoundViolation(min, max)
            }
            rules[ruleId.toInt()] = RuleViolation(bounds)
        }
    } catch (e: Throwable) {
        throw GradleException("Error occurred while parsing verifier result", e)
    }

    return ViolationsResult(rules)
}

private fun raiseViolations(result: ViolationsResult, rules: Iterable<ReportVerificationRule>): String {
    val messageBuilder = StringBuilder()
    val rulesMap = rules.associateBy { r -> r.id }

    result.ruleViolations.forEach { (ruleId, rv) ->
        val rule = rulesMap[ruleId]
            ?: throw Exception("Error occurred while parsing verification error: unmapped rule with ID $ruleId")
        val ruleName = if (rule.name != null) " '${rule.name}'" else ""

        messageBuilder.appendLine("Rule${ruleName} violated:")

        val boundsMap = rule.bounds.associateBy { b -> b.id }
        rv.boundViolations.forEach { (boundId, v) ->
            val bound = boundsMap[boundId]
                ?: throw Exception("Error occurred while parsing verification error: unmapped bound with ID $boundId")

            v.min.forEach { (name, value) ->
                messageBuilder.appendLine(bound.formatViolation(value, rule.target, name, false))
            }
            v.max.forEach { (name, value) ->
                messageBuilder.appendLine(bound.formatViolation(value, rule.target, name, true))
            }
        }
    }

    return messageBuilder.toString()
}

private fun ReportVerificationBound.formatViolation(
    value: BigDecimal,
    entityType: VerificationTarget,
    entityName: String,
    isMax: Boolean
): String {
    val directionText = if (isMax) "maximum" else "minimum"

    val metricText = when (metric) {
        CounterType.LINE -> "lines"
        CounterType.INSTRUCTION -> "instructions"
        CounterType.BRANCH -> "branches"
    }

    val valueTypeText = when (valueType) {
        COVERED_COUNT -> "covered count"
        MISSED_COUNT -> "missed count"
        COVERED_PERCENTAGE -> "covered percentage"
        MISSED_PERCENTAGE -> "missed percentage"
    }

    val entityText = when (entityType) {
        VerificationTarget.ALL -> ""
        VerificationTarget.CLASS -> " for class '$entityName'"
        VerificationTarget.PACKAGE -> " for package '$entityName'"
    }

    val actual = if (valueType == COVERED_PERCENTAGE || valueType == MISSED_PERCENTAGE) {
        value.multiply(ONE_HUNDRED)
    } else {
        value
    }

    val expected = if (isMax) maxValue else minValue

    return "  $metricText $valueTypeText$entityText is $actual, but expected $directionText is $expected"
}

private data class ViolationsResult(val ruleViolations: Map<Int, RuleViolation>)

private data class RuleViolation(val boundViolations: Map<Int, BoundViolation>)

private data class BoundViolation(val min: Map<String, BigDecimal>, val max: Map<String, BigDecimal>)
