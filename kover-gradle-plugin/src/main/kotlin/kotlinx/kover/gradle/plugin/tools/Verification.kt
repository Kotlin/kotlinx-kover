/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools

import kotlinx.kover.gradle.plugin.dsl.*
import java.io.File
import java.io.Serializable
import java.math.*
import java.nio.charset.Charset

internal fun CoverageMeasures.writeToFile(file: File, header: String?, lineFormat: String) {
    file.bufferedWriter(Charset.forName("UTF-8")).use { writer ->
        header?.let { h -> writer.appendLine(h) }

        values.forEach { coverage ->
            val entityName = coverage.entityName ?: "application"
            writer.appendLine(
                lineFormat.replace("<value>", coverage.value.stripTrailingZeros().toPlainString())
                    .replace("<entity>", entityName)
            )
        }
    }
}

internal fun File.writeNoSources(header: String?) {
    this.bufferedWriter(Charset.forName("UTF-8")).use { writer ->
        header?.let { h -> writer.appendLine(h) }
        writer.appendLine("No sources")
    }
}


internal data class CoverageRequest(
    val entity: GroupingEntityType,
    val metric: MetricType,
    val aggregation: AggregationType,
    val header: String?,
    val lineFormat: String,
): Serializable

internal data class CoverageMeasures(
    val values: List<CoverageValue>
)

internal data class CoverageValue(
    val value: BigDecimal,
    val entityName: String? = null,
)

internal data class RuleViolations(
    val entityType: GroupingEntityType,
    val bounds: List<BoundViolations>,
    val name: String
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
        val namedRule = if (rule.name.isNotEmpty()) "Rule '${rule.name}'" else "Rule"

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
