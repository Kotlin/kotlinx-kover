/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.tools.*
import kotlinx.kover.gradle.plugin.util.*
import kotlinx.kover.gradle.plugin.util.json.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.process.*
import org.jetbrains.kotlin.gradle.utils.*
import java.io.*
import java.math.*
import java.util.*

internal fun ReportContext.koverVerify(rules: List<VerificationRule>, commonFilters: ReportFilters): List<RuleViolations> {
    val rulesByFilter = groupRules(rules, commonFilters)
    val usedFilters = rulesByFilter.map { it.first }
    val groupedRules = rulesByFilter.map { it.second }
    val groups = aggregateRawReports(usedFilters)

    val verifyRequest = tempDir.resolve("verify-request.json")
    val verifyResponseFile = tempDir.resolve("verify-result.json")
    verifyRequest.writeVerifyJson(groups, groupedRules, verifyResponseFile)
    services.exec.javaexec {
        mainClass.set("com.intellij.rt.coverage.verify.Main")
        this@javaexec.classpath = this@koverVerify.classpath
        args = mutableListOf(verifyRequest.canonicalPath)
    }

    val violations = verifyResponseFile.readJsonObject()
    return processViolations(rules, violations)
}

private fun groupRules(
    allRules: List<VerificationRule>,
    commonFilters: ReportFilters
): List<Pair<ReportFilters, List<VerificationRule>>> {
    val groupedMap = mutableMapOf<ReportFilters, MutableList<VerificationRule>>()

    allRules.forEach {
        val filters = it.filters ?: commonFilters

        val excludesClasses = filters.excludesClasses
        val includesClasses = filters.includesClasses
        val excludesAnnotations = filters.excludesAnnotations

        val reportFilters = ReportFilters(includesClasses, emptySet(), excludesClasses, excludesAnnotations)
        groupedMap.computeIfAbsent(reportFilters) { mutableListOf() } += it
    }

    return groupedMap.entries.map { it.key to it.value }
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
    groups: List<AggregationGroup>,
    groupedRules: List<List<VerificationRule>>,
    result: File,
) {
    val rulesArray = mutableListOf<Map<String, Any>>()

    groups.forEachIndexed { index, group ->
        val rules = groupedRules[index]
        rules.forEachIndexed { ruleIndex, rule ->
            rulesArray += mapOf(
                "id" to ruleIndex,
                "aggregatedReportFile" to group.ic,
                "smapFile" to group.smap,
                "targetType" to rule.targetToReporter(),
                "bounds" to rule.bounds.mapIndexed { boundIndex, b ->
                    mutableMapOf(
                        "id" to boundIndex,
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

private fun VerificationRule.targetToReporter(): String {
    return when (entityType) {
        GroupingEntityType.APPLICATION -> "ALL"
        GroupingEntityType.CLASS -> "CLASS"
        GroupingEntityType.PACKAGE -> "PACKAGE"
    }
}

private fun VerificationBound.counterToReporter(): String {
    return when (metric) {
        MetricType.LINE -> "LINE"
        MetricType.INSTRUCTION -> "INSTRUCTION"
        MetricType.BRANCH -> "BRANCH"
    }
}

private fun VerificationBound.valueTypeToReporter(): String {
    return when (aggregation) {
        AggregationType.COVERED_COUNT -> "COVERED"
        AggregationType.MISSED_COUNT -> "MISSED"
        AggregationType.COVERED_PERCENTAGE -> "COVERED_RATE"
        AggregationType.MISSED_PERCENTAGE -> "MISSED_RATE"
    }
}

private fun VerificationBound.valueToReporter(value: BigDecimal): BigDecimal {
    return if (aggregation.isPercentage) {
        value.divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP)
    } else {
        value
    }
}

@Suppress("UNCHECKED_CAST")
private fun processViolations(
    rules: List<VerificationRule>,
    violations: Map<String, Any>
): List<RuleViolations> {

    val rulesMap = rules.mapIndexed { index, rule -> index to rule }.associate { it }
    // the order of the rules is guaranteed for Kover (as in config)
    val result = TreeMap<Int, RuleViolations>()

    try {
        violations.forEach { (ruleIdString, boundViolations) ->
            val ruleIndex = ruleIdString.toInt()
            val rule = rulesMap[ruleIndex]
                ?: throw KoverCriticalException("Error occurred while parsing verification result: unmapped rule with index $ruleIndex")

            val boundsMap = rule.bounds.mapIndexed { index, bound -> index to bound }.associate { it }

            // the order of the bound is guaranteed for Kover (as in config + suborder by entity name)
            val boundsResult = TreeMap<ViolationId, BoundViolations>()

            (boundViolations as Map<String, Map<String, Map<String, Any>>>).forEach { (boundIdString, v) ->
                val boundIndex = boundIdString.toInt()

                val bound = boundsMap[boundIndex]
                    ?: throw KoverCriticalException("Error occurred while parsing verification error: unmapped bound with index $boundIndex and rule index $ruleIndex")

                v["min"]?.map {
                    bound.minValue
                        ?: throw KoverCriticalException("Error occurred while parsing verification error: no minimal bound with ID $boundIndex and rule index $ruleIndex")

                    val entityName = it.key.ifEmpty { null }
                    val rawValue = it.value
                    val value = if (rawValue is String) rawValue.toBigDecimal() else rawValue as BigDecimal
                    val actual = if (bound.aggregation.isPercentage) value * ONE_HUNDRED else value
                    boundsResult += ViolationId(boundIndex, entityName) to BoundViolations(
                        false,
                        bound.minValue,
                        actual,
                        bound.metric,
                        bound.aggregation,
                        entityName
                    )
                }

                v["max"]?.map {
                    bound.maxValue
                        ?: throw KoverCriticalException("Error occurred while parsing verification error: no maximal bound with index $boundIndex and rule index $ruleIndex")

                    val entityName = it.key.ifEmpty { null }
                    val rawValue = it.value
                    val value = if (rawValue is String) rawValue.toBigDecimal() else rawValue as BigDecimal
                    val actual = if (bound.aggregation.isPercentage) value * ONE_HUNDRED else value
                    boundsResult += ViolationId(boundIndex, entityName) to BoundViolations(
                        true,
                        bound.maxValue,
                        actual,
                        bound.metric,
                        bound.aggregation,
                        entityName
                    )
                }
            }

            result += ruleIndex to RuleViolations(rule.entityType, boundsResult.values.toList(), rule.name)
        }
    } catch (e: Throwable) {
        throw KoverCriticalException("Error occurred while parsing verifier result", e)
    }

    return result.values.toList()
}

private data class ViolationId(val index: Int, val entityName: String?): Comparable<ViolationId> {
    override fun compareTo(other: ViolationId): Int {
        // first compared by index
        index.compareTo(other.index).takeIf { it != 0 }?.let { return it }

        // if indexes are equals then compare by entity name

        if (entityName == null) {
            // bounds with empty entity names goes first
            return if (other.entityName == null) 0 else -1
        }
        if (other.entityName == null) return 1

        entityName.compareTo(other.entityName).takeIf { it != 0 }?.let { return it }

        // indexes and names are equals
        return 0
    }
}
