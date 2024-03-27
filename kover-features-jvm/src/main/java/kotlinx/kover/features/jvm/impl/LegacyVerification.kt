/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.features.jvm.impl

import com.intellij.rt.coverage.aggregate.api.AggregatorApi
import com.intellij.rt.coverage.aggregate.api.Request
import com.intellij.rt.coverage.report.api.Filters
import com.intellij.rt.coverage.verify.Verifier
import com.intellij.rt.coverage.verify.api.Counter
import com.intellij.rt.coverage.verify.api.Target
import com.intellij.rt.coverage.verify.api.ValueType
import com.intellij.rt.coverage.verify.api.VerificationApi
import kotlinx.kover.features.jvm.*
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

private typealias IntellijRule = com.intellij.rt.coverage.verify.api.Rule

private typealias IntellijBound = com.intellij.rt.coverage.verify.api.Bound

internal object LegacyVerification {
    internal val ONE_HUNDRED: BigDecimal = BigDecimal(100)

    fun verify(
        rules: List<Rule>,
        tempDir: File,
        filters: ClassFilters,
        reports: List<File>,
        outputs: List<File>
    ): List<RuleViolations> {
        val intellijFilters: Filters = filters.convert()

        val rulesArray = ArrayList<IntellijRule>()

        val ic = File(tempDir, "agg-ic.ic")
        val smap = File(tempDir, "agg-smap.smap")

        val requests = Request(intellijFilters, ic, smap)
        AggregatorApi.aggregate(listOf(requests), reports, outputs)

        for (ruleIndex in rules.indices) {
            val rule = rules[ruleIndex]

            val bounds: MutableList<IntellijBound> = ArrayList()
            for (boundIndex in rule.bounds.indices) {
                val b = rule.bounds[boundIndex]

                bounds.add(
                    IntellijBound(
                        boundIndex,
                        counterToIntellij(b),
                        valueTypeToIntellij(b),
                        valueToIntellij(b, b.minValue),
                        valueToIntellij(b, b.maxValue)
                    )
                )
            }

            rulesArray.add(IntellijRule(ruleIndex, ic, targetToIntellij(rule), bounds))
        }


        val verifier = Verifier(rulesArray)
        verifier.processRules()
        val violations = VerificationApi.verify(rulesArray)

        val ruleViolations = ArrayList<RuleViolations>()
        for (ruleViolation in violations) {
            // TreeMap is using for getting stable order in result List - in this case, it is easier to write tests and Gradle build cache will not miss
            val resultBounds = TreeMap<ViolationId, BoundViolation>()

            val rule = rules[ruleViolation.id]
            for (boundIndex in ruleViolation.violations.indices) {
                val boundViolation = ruleViolation.violations[boundIndex]
                val bound = rule.bounds[boundViolation.id]

                for (maxViolation in boundViolation.maxViolations) {
                    val entityName = if (rule.groupBy != GroupingBy.APPLICATION) maxViolation.targetName else null
                    resultBounds[ViolationId(boundViolation.id, entityName)] =
                        BoundViolation(bound, true, intellijToValue(maxViolation.targetValue, bound), entityName)
                }
                for (minViolation in boundViolation.minViolations) {
                    val entityName = if (rule.groupBy != GroupingBy.APPLICATION) minViolation.targetName else null
                    resultBounds[ViolationId(boundViolation.id, entityName)] =
                        BoundViolation(bound, false, intellijToValue(minViolation.targetValue, bound), entityName)
                }
            }

            ruleViolations.add(RuleViolations(rule, ArrayList(resultBounds.values)))
        }

        return ruleViolations
    }

    private fun intellijToValue(intellijValue: BigDecimal, bound: Bound): BigDecimal {
        return if (isPercentage(bound.aggregationForGroup)) {
            intellijValue.multiply(ONE_HUNDRED)
        } else {
            intellijValue
        }
    }

    private fun targetToIntellij(rule: Rule): Target? {
        return when (rule.groupBy) {
            GroupingBy.APPLICATION -> Target.ALL
            GroupingBy.CLASS -> Target.CLASS
            GroupingBy.PACKAGE -> Target.PACKAGE
        }
    }

    private fun counterToIntellij(bound: Bound): Counter? {
        return when (bound.coverageUnits) {
            CoverageUnit.LINE -> Counter.LINE
            CoverageUnit.INSTRUCTION -> Counter.INSTRUCTION
            CoverageUnit.BRANCH -> Counter.BRANCH
        }
    }

    private fun valueTypeToIntellij(bound: Bound): ValueType? {
        return when (bound.aggregationForGroup) {
            AggregationType.COVERED_COUNT -> ValueType.COVERED
            AggregationType.MISSED_COUNT -> ValueType.MISSED
            AggregationType.COVERED_PERCENTAGE -> ValueType.COVERED_RATE
            AggregationType.MISSED_PERCENTAGE -> ValueType.MISSED_RATE
        }
    }

    private fun valueToIntellij(bound: Bound, value: BigDecimal?): BigDecimal? {
        if (value == null) return null

        return if (isPercentage(bound.aggregationForGroup)) {
            value.divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP)
        } else {
            value
        }
    }

    private fun isPercentage(aggregationType: AggregationType): Boolean {
        return aggregationType == AggregationType.COVERED_PERCENTAGE || aggregationType == AggregationType.MISSED_PERCENTAGE
    }

    private data class ViolationId(private val index: Int, private val entityName: String?) : Comparable<ViolationId> {
        override fun compareTo(other: ViolationId): Int {
            // first compared by index
            index.compareTo(other.index).takeIf { it != 0 }?.let { return it }

            // if indexes are equals then compare by entity name
            if (entityName == null) {
                // bounds with empty entity names goes first
                return if (other.entityName == null) 0 else -1
            }

            if (other.entityName == null) return 1

            return entityName.compareTo(other.entityName)
        }
    }
}
