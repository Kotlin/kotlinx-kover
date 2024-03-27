package kotlinx.kover.gradle.plugin.tools.kover

import kotlinx.kover.features.jvm.*
import kotlinx.kover.gradle.plugin.commons.ReportFilters
import kotlinx.kover.gradle.plugin.commons.VerificationBound
import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType

private typealias FeatureCoverageUnit = kotlinx.kover.features.jvm.CoverageUnit
private typealias FeatureAggregationType = kotlinx.kover.features.jvm.AggregationType

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


internal fun CoverageUnit.convert(): FeatureCoverageUnit {
    return when (this) {
        CoverageUnit.LINE -> FeatureCoverageUnit.LINE
        CoverageUnit.BRANCH -> FeatureCoverageUnit.BRANCH
        CoverageUnit.INSTRUCTION -> FeatureCoverageUnit.INSTRUCTION
    }
}

internal fun AggregationType.convert(): FeatureAggregationType {
    return when (this) {
        AggregationType.COVERED_COUNT -> FeatureAggregationType.COVERED_COUNT
        AggregationType.COVERED_PERCENTAGE -> FeatureAggregationType.COVERED_PERCENTAGE
        AggregationType.MISSED_COUNT -> FeatureAggregationType.MISSED_COUNT
        AggregationType.MISSED_PERCENTAGE -> FeatureAggregationType.MISSED_PERCENTAGE
    }
}
