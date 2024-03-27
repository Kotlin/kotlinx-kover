package kotlinx.kover.gradle.plugin.tools.kover

import kotlinx.kover.features.jvm.*
import kotlinx.kover.gradle.plugin.commons.ReportFilters
import kotlinx.kover.gradle.plugin.commons.VerificationBound
import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType

internal fun ReportFilters.toKoverFeatures() = ClassFilters(
    includesClasses,
    excludesClasses,
    excludesAnnotations
)

internal fun VerificationRule.convert(): Rule {
    return Rule(
        name,
        entityType.convert(),
        bounds.map { it.convert() }
    )
}


internal fun VerificationBound.convert(): Bound {
    return Bound(minValue, maxValue, metric.convert(), aggregation.convert())
}


internal fun GroupingEntityType.convert(): GroupingBy {
    return when (this) {
        GroupingEntityType.APPLICATION -> GroupingBy.APPLICATION
        GroupingEntityType.CLASS -> GroupingBy.CLASS
        GroupingEntityType.PACKAGE -> GroupingBy.PACKAGE
    }
}


internal fun CoverageUnit.convert(): kotlinx.kover.features.jvm.CoverageUnit {
    return when (this) {
        CoverageUnit.LINE -> kotlinx.kover.features.jvm.CoverageUnit.LINE
        CoverageUnit.BRANCH -> kotlinx.kover.features.jvm.CoverageUnit.BRANCH
        CoverageUnit.INSTRUCTION -> kotlinx.kover.features.jvm.CoverageUnit.INSTRUCTION
    }
}

internal fun AggregationType.convert(): kotlinx.kover.features.jvm.AggregationType {
    return when (this) {
        AggregationType.COVERED_COUNT -> kotlinx.kover.features.jvm.AggregationType.COVERED_COUNT
        AggregationType.COVERED_PERCENTAGE -> kotlinx.kover.features.jvm.AggregationType.COVERED_PERCENTAGE
        AggregationType.MISSED_COUNT -> kotlinx.kover.features.jvm.AggregationType.MISSED_COUNT
        AggregationType.MISSED_PERCENTAGE -> kotlinx.kover.features.jvm.AggregationType.MISSED_PERCENTAGE
    }
}
