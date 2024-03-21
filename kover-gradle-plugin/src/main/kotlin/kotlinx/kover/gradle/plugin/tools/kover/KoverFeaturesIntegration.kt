package kotlinx.kover.gradle.plugin.tools.kover

import kotlinx.kover.features.jvm.KoverLegacyFeatures
import kotlinx.kover.features.jvm.KoverLegacyFeatures.Bound
import kotlinx.kover.gradle.plugin.commons.ReportFilters
import kotlinx.kover.gradle.plugin.commons.VerificationBound
import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType

internal fun ReportFilters.toKoverFeatures() = KoverLegacyFeatures.ClassFilters(
    includesClasses,
    excludesClasses,
    excludesAnnotations
)

internal fun VerificationRule.convert(): KoverLegacyFeatures.Rule {
    return KoverLegacyFeatures.Rule(
        name,
        entityType.convert(),
        bounds.map { it.convert() }
    )
}


internal fun VerificationBound.convert(): Bound {
    return Bound(minValue, maxValue, metric.convert(), aggregation.convert())
}


internal fun GroupingEntityType.convert(): KoverLegacyFeatures.GroupingBy {
    return when (this) {
        GroupingEntityType.APPLICATION -> KoverLegacyFeatures.GroupingBy.APPLICATION
        GroupingEntityType.CLASS -> KoverLegacyFeatures.GroupingBy.CLASS
        GroupingEntityType.PACKAGE -> KoverLegacyFeatures.GroupingBy.PACKAGE
    }
}


internal fun CoverageUnit.convert(): KoverLegacyFeatures.CoverageUnit {
    return when (this) {
        CoverageUnit.LINE -> KoverLegacyFeatures.CoverageUnit.LINE
        CoverageUnit.BRANCH -> KoverLegacyFeatures.CoverageUnit.BRANCH
        CoverageUnit.INSTRUCTION -> KoverLegacyFeatures.CoverageUnit.INSTRUCTION
    }
}

internal fun AggregationType.convert(): KoverLegacyFeatures.AggregationType {
    return when (this) {
        AggregationType.COVERED_COUNT -> KoverLegacyFeatures.AggregationType.COVERED_COUNT
        AggregationType.COVERED_PERCENTAGE -> KoverLegacyFeatures.AggregationType.COVERED_PERCENTAGE
        AggregationType.MISSED_COUNT -> KoverLegacyFeatures.AggregationType.MISSED_COUNT
        AggregationType.MISSED_PERCENTAGE -> KoverLegacyFeatures.AggregationType.MISSED_PERCENTAGE
    }
}
