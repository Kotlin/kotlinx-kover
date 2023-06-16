/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import com.intellij.rt.coverage.verify.Verifier
import com.intellij.rt.coverage.verify.api.*
import com.intellij.rt.coverage.verify.api.Target
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import kotlinx.kover.gradle.plugin.dsl.MetricType
import kotlinx.kover.gradle.plugin.tools.BoundViolations
import kotlinx.kover.gradle.plugin.tools.RuleViolations
import kotlinx.kover.gradle.plugin.tools.generateErrorMessage
import kotlinx.kover.gradle.plugin.util.ONE_HUNDRED
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkQueue
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

internal fun ReportContext.koverVerify(specifiedRules: List<VerificationRule>, outputReportFile: File) {
    val workQueue: WorkQueue = services.workerExecutor.classLoaderIsolation {
        this.classpath.from(this@koverVerify.classpath)
    }

    workQueue.submit(VerifyReportAction::class.java) {
        outputFile.set(outputReportFile)
        rules.convention(specifiedRules)
        filters.convention(this@koverVerify.filters)

        files.convention(this@koverVerify.files)
        tempDir.set(this@koverVerify.tempDir)
        projectPath.convention(this@koverVerify.projectPath)
    }
}



internal abstract class VerifyReportAction : WorkAction<VerifyReportParameters> {
    override fun execute() {
        val violations = koverVerify(
            parameters.rules.get(),
            parameters.filters.get(),
            parameters.tempDir.get().asFile,
            parameters.files.get()
        )

        val errorMessage = generateErrorMessage(violations)
        parameters.outputFile.get().asFile.writeText(errorMessage)

        if (violations.isNotEmpty()) {
            throw KoverVerificationException(errorMessage)
        }
    }
}

internal fun koverVerify(
    rules: List<VerificationRule>,
    commonFilters: ReportFilters,
    tempDir: File,
    files: ArtifactContent
): List<RuleViolations> {
    val rulesByFilter = groupRules(rules, commonFilters)
    val usedFilters = rulesByFilter.map { it.first }
    val groupedRules = rulesByFilter.map { it.second }

    val groups = aggregateBinReports(files, usedFilters, tempDir)

    val rulesArray = mutableListOf<Rule>()
    groups.forEachIndexed { index, group ->
        val rulesForGroup = groupedRules[index]
        rulesForGroup.forEachIndexed { ruleIndex, rule ->
            val bounds = rule.bounds.mapIndexed { boundIndex, b ->
                Bound(
                    boundIndex, b.counterToReporter(), b.valueTypeToReporter(), b.valueToReporter(b.minValue),
                    b.valueToReporter(b.maxValue)
                )

            }
            rulesArray += Rule(ruleIndex, group.ic, rule.targetToReporter(), bounds)
        }
    }


    val verifier = Verifier(rulesArray)
    verifier.processRules()

    val violations = VerificationApi.verify(rulesArray)

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


private fun VerificationRule.targetToReporter(): Target {
    return when (entityType) {
        GroupingEntityType.APPLICATION -> Target.ALL
        GroupingEntityType.CLASS -> Target.CLASS
        GroupingEntityType.PACKAGE -> Target.PACKAGE
    }
}

private fun VerificationBound.counterToReporter(): Counter {
    return when (metric) {
        MetricType.LINE -> Counter.LINE
        MetricType.INSTRUCTION -> Counter.INSTRUCTION
        MetricType.BRANCH -> Counter.BRANCH
    }
}

private fun VerificationBound.valueTypeToReporter(): ValueType {
    return when (aggregation) {
        AggregationType.COVERED_COUNT -> ValueType.COVERED
        AggregationType.MISSED_COUNT -> ValueType.MISSED
        AggregationType.COVERED_PERCENTAGE -> ValueType.COVERED_RATE
        AggregationType.MISSED_PERCENTAGE -> ValueType.MISSED_RATE
    }
}

private fun VerificationBound.valueToReporter(value: BigDecimal?): BigDecimal? {
    value ?: return null
    return if (aggregation.isPercentage) {
        value.divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP)
    } else {
        value
    }
}

private fun processViolations(
    rules: List<VerificationRule>,
    violations: List<RuleViolation>
): List<RuleViolations> {

    val rulesMap = rules.mapIndexed { index, rule -> index to rule }.associate { it }
    // the order of the rules is guaranteed for Kover (as in config)
    val result = TreeMap<Int, RuleViolations>()

    try {
        violations.forEach { violation ->
            val ruleIndex = violation.id
            val rule = rulesMap[ruleIndex]
                ?: throw KoverCriticalException("Error occurred while parsing verification result: unmapped rule with index $ruleIndex")

            val boundsMap = rule.bounds.mapIndexed { index, bound -> index to bound }.associate { it }

            // the order of the bound is guaranteed for Kover (as in config + suborder by entity name)
            val boundsResult = TreeMap<ViolationId, BoundViolations>()

            violation.violations.forEach { boundViolation ->
                val boundIndex = boundViolation.id

                val bound = boundsMap[boundIndex]
                    ?: throw KoverCriticalException("Error occurred while parsing verification error: unmapped bound with index $boundIndex and rule index $ruleIndex")

                boundViolation.minViolations.forEach {
                    bound.minValue
                        ?: throw KoverCriticalException("Error occurred while parsing verification error: no minimal bound with ID $boundIndex and rule index $ruleIndex")

                    val entityName = it.targetName.ifEmpty { null }
                    val value = it.targetValue
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

                boundViolation.maxViolations.forEach {
                    bound.maxValue
                        ?: throw KoverCriticalException("Error occurred while parsing verification error: no maximal bound with index $boundIndex and rule index $ruleIndex")

                    val entityName = it.targetName.ifEmpty { null }
                    val value = it.targetValue
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

private data class ViolationId(val index: Int, val entityName: String?) : Comparable<ViolationId> {
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
