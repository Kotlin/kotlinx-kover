/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings.tasks

import kotlinx.kover.features.jvm.Bound
import kotlinx.kover.features.jvm.ClassFilters
import kotlinx.kover.features.jvm.GroupingBy
import kotlinx.kover.features.jvm.Rule
import kotlinx.kover.gradle.aggregation.settings.dsl.BoundSettings
import kotlinx.kover.gradle.aggregation.settings.dsl.ReportFiltersSettings
import kotlinx.kover.gradle.aggregation.settings.dsl.VerificationRuleSettings
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import java.io.Serializable

internal data class FiltersInput(
    @get:Input
    val includedProjects: Set<String> = emptySet(),
    @get:Input
    val excludedProjects: Set<String> = emptySet(),
    @get:Input
    val includedClasses: Set<String> = emptySet(),
    @get:Input
    val excludedClasses: Set<String> = emptySet(),
    @get:Input
    val includesAnnotatedBy: Set<String> = emptySet(),
    @get:Input
    val excludesAnnotatedBy: Set<String> = emptySet(),
    @get:Input
    val includesInheritedFrom: Set<String> = emptySet(),
    @get:Input
    val excludesInheritedFrom: Set<String> = emptySet()
): Serializable


internal fun ReportFiltersSettings.asInput(): FiltersInput {
    return FiltersInput(includedProjects.get(),
    excludedProjects.get(),
    includedClasses.get(),
    excludedClasses.get(),
    includesAnnotatedBy.get(),
    excludesAnnotatedBy.get(),
    includesInheritedFrom.get(),
    excludesInheritedFrom.get())
}

internal fun FiltersInput.toExternalFilters(): ClassFilters {
    return ClassFilters(
        includedClasses,
        excludedClasses,
        includesAnnotatedBy,
        excludesAnnotatedBy,
        includesInheritedFrom,
        excludesInheritedFrom
    )
}

internal class VerificationRuleInput(
    @get:Input
    val disabled: Boolean,
    @get:Input
    @get:Optional
    val name: String?,
    @get:Input
    val groupBy: GroupingEntityType,
    @get:Input
    val filters: FiltersInput,
    @get:Nested
    val bounds: List<BoundInput>
): Serializable

internal class BoundInput(
    @get:Optional
    @get:Input
    val minValue: Int?,
    @get:Optional
    @get:Input
    val maxValue: Int?,
    @get:Input
    val coverageUnits: CoverageUnit,
    @get:Input
    val aggregationForGroup: AggregationType
): Serializable

internal fun VerificationRuleInput.toExternal(): Rule {
    return Rule(name ?: "", groupBy.toExternal(), bounds.map { bound -> bound.toExternal() })
}

internal fun BoundInput.toExternal(): Bound {
    return Bound(
        minValue?.toBigDecimal(),
        maxValue?.toBigDecimal(),
        coverageUnits.toExternal(),
        aggregationForGroup.toExternal()
    )
}

internal fun GroupingEntityType.toExternal(): GroupingBy {
    return when (this) {
        GroupingEntityType.APPLICATION -> GroupingBy.APPLICATION
        GroupingEntityType.CLASS -> GroupingBy.CLASS
        GroupingEntityType.PACKAGE -> GroupingBy.PACKAGE
    }
}

private typealias FeatureCoverageUnit = kotlinx.kover.features.jvm.CoverageUnit
private typealias FeatureAggregationType = kotlinx.kover.features.jvm.AggregationType

internal fun AggregationType.toExternal(): FeatureAggregationType {
    return when (this) {
        AggregationType.COVERED_COUNT -> FeatureAggregationType.COVERED_COUNT
        AggregationType.COVERED_PERCENTAGE -> FeatureAggregationType.COVERED_PERCENTAGE
        AggregationType.MISSED_COUNT -> FeatureAggregationType.MISSED_COUNT
        AggregationType.MISSED_PERCENTAGE -> FeatureAggregationType.MISSED_PERCENTAGE
    }
}


internal fun CoverageUnit.toExternal(): FeatureCoverageUnit {
    return when (this) {
        CoverageUnit.LINE -> FeatureCoverageUnit.LINE
        CoverageUnit.BRANCH -> FeatureCoverageUnit.BRANCH
        CoverageUnit.INSTRUCTION -> FeatureCoverageUnit.INSTRUCTION
    }
}

internal fun VerificationRuleSettings.asInput(): VerificationRuleInput {
    val inputBounds = bounds.map { it.map { bound -> bound.asInput() } }
    return VerificationRuleInput(disabled.get(), name.orNull, groupBy.get(), filters.asInput(), inputBounds.get())
}

internal fun BoundSettings.asInput(): BoundInput {
    return BoundInput(minValue.orNull, maxValue.orNull, coverageUnits.get(), aggregationForGroup.get())
}