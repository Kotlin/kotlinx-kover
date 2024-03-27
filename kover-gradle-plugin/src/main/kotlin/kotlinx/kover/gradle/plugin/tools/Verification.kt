/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools

import kotlinx.kover.features.jvm.*
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import java.io.File
import java.io.Serializable
import java.nio.charset.Charset

internal fun Iterable<CoverageValue>.writeToFile(file: File, header: String?, lineFormat: String) {
    file.bufferedWriter(Charset.forName("UTF-8")).use { writer ->
        header?.let { h -> writer.appendLine(h) }

        forEach { coverage ->
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
    val metric: CoverageUnit,
    val aggregation: AggregationType,
    val header: String?,
    val lineFormat: String,
): Serializable

internal fun generateErrorMessage(violations: List<RuleViolations>): String {
    val messageBuilder = StringBuilder()

    violations.forEach { rule ->
        val namedRule = if (rule.rule.name.isNotEmpty()) "Rule '${rule.rule.name}'" else "Rule"

        if (rule.violations.size == 1) {
            messageBuilder.appendLine("$namedRule violated: ${rule.violations[0].format(rule)}")
        } else {
            messageBuilder.appendLine("$namedRule violated:")

            rule.violations.forEach { bound ->
                messageBuilder.append("  ")
                messageBuilder.appendLine(bound.format(rule))
            }
        }
    }

    return messageBuilder.toString()
}

private fun BoundViolation.format(rule: RuleViolations): String {
    val directionText = if (isMax) "maximum" else "minimum"

    val metricText = when (bound.coverageUnits) {
        FeatureCoverageUnit.LINE -> "lines"
        FeatureCoverageUnit.INSTRUCTION -> "instructions"
        FeatureCoverageUnit.BRANCH -> "branches"
    }

    val valueTypeText = when (bound.aggregationForGroup) {
        FeatureAggregationType.COVERED_COUNT -> "covered count"
        FeatureAggregationType.MISSED_COUNT -> "missed count"
        FeatureAggregationType.COVERED_PERCENTAGE -> "covered percentage"
        FeatureAggregationType.MISSED_PERCENTAGE -> "missed percentage"
    }

    val entityText = when (rule.rule.groupBy) {
        GroupingBy.APPLICATION -> ""
        GroupingBy.CLASS -> " for class '$entityName'"
        GroupingBy.PACKAGE -> " for package '$entityName'"
    }

    val expectedValue = if (isMax) bound.maxValue else bound.minValue

    return "$metricText $valueTypeText$entityText is $value, but expected $directionText is $expectedValue"
}

private typealias FeatureCoverageUnit = kotlinx.kover.features.jvm.CoverageUnit
private typealias FeatureAggregationType = kotlinx.kover.features.jvm.AggregationType