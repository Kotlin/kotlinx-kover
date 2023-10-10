package kotlinx.kover.gradle.plugin.tools.kover

import com.intellij.rt.coverage.report.api.Filters
import com.intellij.rt.coverage.verify.api.Counter
import com.intellij.rt.coverage.verify.api.Target
import com.intellij.rt.coverage.verify.api.ValueType
import kotlinx.kover.gradle.plugin.commons.ReportFilters
import kotlinx.kover.gradle.plugin.commons.VerificationBound
import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import kotlinx.kover.gradle.plugin.dsl.MetricType
import kotlinx.kover.gradle.plugin.util.ONE_HUNDRED
import kotlinx.kover.gradle.plugin.util.asPatterns
import java.math.BigDecimal
import java.math.RoundingMode

internal fun ReportFilters.toIntellij() = Filters(
    includesClasses.asPatterns(),
    excludesClasses.asPatterns(),
    excludesAnnotations.asPatterns()
)

internal fun VerificationRule.targetToIntellij(): Target {
    return when (entityType) {
        GroupingEntityType.APPLICATION -> Target.ALL
        GroupingEntityType.CLASS -> Target.CLASS
        GroupingEntityType.PACKAGE -> Target.PACKAGE
    }
}

internal fun VerificationBound.counterToIntellij(): Counter {
    return when (metric) {
        MetricType.LINE -> Counter.LINE
        MetricType.INSTRUCTION -> Counter.INSTRUCTION
        MetricType.BRANCH -> Counter.BRANCH
    }
}

internal fun VerificationBound.valueTypeToIntellij(): ValueType {
    return when (aggregation) {
        AggregationType.COVERED_COUNT -> ValueType.COVERED
        AggregationType.MISSED_COUNT -> ValueType.MISSED
        AggregationType.COVERED_PERCENTAGE -> ValueType.COVERED_RATE
        AggregationType.MISSED_PERCENTAGE -> ValueType.MISSED_RATE
    }
}

internal fun VerificationBound.valueToIntellij(value: BigDecimal?): BigDecimal? {
    value ?: return null
    return if (aggregation.isPercentage) {
        value.divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP)
    } else {
        value
    }
}
