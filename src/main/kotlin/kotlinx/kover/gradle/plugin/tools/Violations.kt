/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools

import kotlinx.kover.gradle.plugin.dsl.*
import java.math.*

internal data class RuleViolations(
    val entityType: GroupingEntityType,
    val bounds: List<BoundViolations>,
    val name: String? = null
)

internal data class BoundViolations(
    val isMax: Boolean,
    val expectedValue: BigDecimal,
    val actualValue: BigDecimal,
    val metric: MetricType,
    val aggregation: AggregationType,
    val entityName: String? = null
)

internal fun generateErrorMessage(violations: List<RuleViolations>): String {
    val messageBuilder = StringBuilder()

    violations.forEach { rule ->
        val namedRule = if (rule.name != null) "Rule '${rule.name}'" else "Rule"

        if (rule.bounds.size == 1) {
            messageBuilder.appendLine("$namedRule violated: ${rule.bounds[0].format(rule)}")
        } else {
            messageBuilder.appendLine("$namedRule violated:")

            rule.bounds.forEach { bound ->
                messageBuilder.append("  ")
                messageBuilder.appendLine(bound.format(rule))
            }
        }
    }

    return messageBuilder.toString()
}

private fun BoundViolations.format(rule: RuleViolations): String {
    val directionText = if (isMax) "maximum" else "minimum"

    val metricText = when (metric) {
        MetricType.LINE -> "lines"
        MetricType.INSTRUCTION -> "instructions"
        MetricType.BRANCH -> "branches"
    }

    val valueTypeText = when (aggregation) {
        AggregationType.COVERED_COUNT -> "covered count"
        AggregationType.MISSED_COUNT -> "missed count"
        AggregationType.COVERED_PERCENTAGE -> "covered percentage"
        AggregationType.MISSED_PERCENTAGE -> "missed percentage"
    }

    val entityText = when (rule.entityType) {
        GroupingEntityType.APPLICATION -> ""
        GroupingEntityType.CLASS -> " for class '$entityName'"
        GroupingEntityType.PACKAGE -> " for package '$entityName'"
    }

    return "$metricText $valueTypeText$entityText is $actualValue, but expected $directionText is $expectedValue"
}
