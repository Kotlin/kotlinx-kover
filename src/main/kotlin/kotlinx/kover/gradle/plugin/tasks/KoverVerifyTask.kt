/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.tools.*
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import kotlinx.kover.gradle.plugin.util.*
import kotlinx.kover.gradle.plugin.util.ONE_HUNDRED
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import javax.inject.*

@CacheableTask
internal open class KoverVerifyTask @Inject constructor(tool: CoverageTool) : AbstractKoverReportTask(tool) {
    @get:Nested
    val rules: ListProperty<VerificationRule> = project.objects.listProperty()

    @get:OutputFile
    val resultFile: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun verify() {
        val enabledRules = rules.get().filter { it.isEnabled }

        val violations = tool.verify(enabledRules, filters.get(), context())

        if (violations.isNotEmpty()) {
            val message = generateErrorMessage(violations)
            resultFile.get().asFile.writeText(message)
            throw KoverVerificationException(message)
        }
    }

    private fun generateErrorMessage(violations: List<RuleViolations>): String {
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

}
