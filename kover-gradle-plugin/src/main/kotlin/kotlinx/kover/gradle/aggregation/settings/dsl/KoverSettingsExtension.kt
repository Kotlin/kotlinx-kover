/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings.dsl

import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import kotlinx.kover.gradle.plugin.dsl.KoverGradlePluginDsl
import org.gradle.api.Action
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty

@KoverGradlePluginDsl
public interface KoverSettingsExtension {
    fun enableCoverage()

    val reports: ReportsSettings
    fun reports(action: Action<ReportsSettings>)
}

@KoverGradlePluginDsl
public interface ReportsSettings: ReportFiltersSettings {
    val verify: VerifySettings

    fun verify(action: Action<VerifySettings>) {
        action.execute(verify)
    }
}

@KoverGradlePluginDsl
public interface ReportFiltersSettings {
    fun clearFilters() {
        includedProjects.empty()
        excludedProjects.empty()
        excludedClasses.empty()
        includedClasses.empty()
        excludesAnnotatedBy.empty()
        includesAnnotatedBy.empty()
        includesInheritedFrom.empty()
        excludesInheritedFrom.empty()
    }

    val includedProjects: SetProperty<String>
    val excludedProjects: SetProperty<String>
    val excludedClasses: SetProperty<String>
    val includedClasses: SetProperty<String>
    val excludesAnnotatedBy: SetProperty<String>
    val includesAnnotatedBy: SetProperty<String>
    val includesInheritedFrom: SetProperty<String>
    val excludesInheritedFrom: SetProperty<String>
}


@KoverGradlePluginDsl
public interface VerifySettings {
    public val rules: ListProperty<VerificationRuleSettings>

    /**
     * Add new coverage verification rule to check.
     */
    public fun rule(action: Action<VerificationRuleSettings>)

    /**
     * Add new named coverage verification rule to check.
     *
     * The name will be displayed in case of a verification error.
     */
    public fun rule(name: String, action: Action<VerificationRuleSettings>)

    /**
     * In case of a verification error, print a message to the log with the warn level instead of the Gradle task execution error.
     *
     * Gradle task error if `false`, warn message if `true`.
     *
     * `false` by default.
     */
    public val warningInsteadOfFailure: Property<Boolean>
}

@KoverGradlePluginDsl
public interface VerificationRuleSettings {
    /**
     * Name of the rule. Will be displayed in case of a verification error.
     *
     * Empty by default.
     */
    public val name: Property<String>

    /**
     * Specifies that the rule is checked during verification.
     *
     * `false` by default.
     */
    public val disabled: Property<Boolean>

    /**
     * Specifies by which entity the code for separate coverage evaluation will be grouped.
     * [GroupingEntityType.APPLICATION] by default.
     */
    public val groupBy: Property<GroupingEntityType>

    /**
     * Instance for configuring report filters for this rule.
     *
     * See details in [filters].
     */
    public val filters: ReportFiltersSettings

    /**
     * Modify filters for this rule, these filters will be inherited from common report filters.
     * ```
     *  filters {
     *      includedProjects.add(":a:*")
     *      excludedProjects.add(":a:b")
     *      excludedClasses.add("*Class")
     *      includedClasses.add("*")
     *      excludesAnnotatedBy.add("*Generated")
     *      includesAnnotatedBy.add("*Covered")
     *      includesInheritedFrom.add("*.AutoClosable")
     *      excludesInheritedFrom.add("*.Any")
     *  }
     * ```
     */
    public fun filters(action: Action<ReportFiltersSettings>) {
        action.execute(filters)
    }

    /**
     * Instance for configuring bounds for this rule.
     *
     * See details in [bound].
     */
    public val bounds: ListProperty<BoundSettings>

    /**
     * Specifies the set of verification limits that control the coverage conditions.
     *
     * An example of bound configuration:
     * ```
     * // At least 75% of lines should be covered in order for build to pass
     * bound {
     *     aggregationForGroup = AggregationType.COVERED_PERCENTAGE // Default aggregation
     *     coverageUnits = CoverageUnit.LINE
     *     minValue = 75
     * }
     * ```
     *
     * @see BoundSettings
     */
    public fun bound(action: Action<BoundSettings>)
}

/**
 * Describes a single bound for the verification rule to enforce;
 * Bound specifies what type of coverage is enforced (branches, lines, instructions),
 * how coverage is aggregated (raw count or percents) and what numerical values of coverage
 * are acceptable.
 */
@KoverGradlePluginDsl
public interface BoundSettings {
    /**
     * Specifies minimal value to compare with aggregated coverage value.
     * The comparison occurs only if the value is present.
     *
     * Absent by default.
     */
    public val minValue: Property<Int>

    /**
     * Specifies maximal value to compare with counter value.
     * The comparison occurs only if the value is present.
     *
     * Absent by default.
     */
    public val maxValue: Property<Int>

    /**
     * The type of application code division (unit type) whose unit coverage will be considered independently.
     * It affects which blocks the value of the covered and missed units will be calculated for.
     *
     * [CoverageUnit.LINE] by default.
     */
    public val coverageUnits: Property<CoverageUnit>

    /**
     * Specifies aggregation function that will be calculated over all the units of the same group.
     *
     * This function used to calculate the aggregated coverage value, it uses the values of the covered and uncovered units of type [coverageUnits] as arguments.
     *
     * Result value will be compared with the bounds.
     *
     * [AggregationType.COVERED_PERCENTAGE] by default.
     */
    public val aggregationForGroup: Property<AggregationType>
}



/**
 * A shortcut for
 * ```
 * bound {
 *     minValue = limit
 * }
 * ```
 *
 * @see VerificationRuleSettings.bound
 */
public fun VerificationRuleSettings.minBound(limit: Int) {
    bound {
        minValue.set(limit)
    }
}

/**
 * A shortcut for
 * ```
 * bound {
 *     minValue = limit
 * }
 * ```
 *
 * @see VerificationRuleSettings.bound
 */
public fun VerificationRuleSettings.minBound(limit: Provider<Int>) {
    bound {
        minValue.set(limit)
    }
}

/**
 * A shortcut for
 * ```
 * bound {
 *     maxValue = limit
 * }
 * ```
 *
 * @see VerificationRuleSettings.bound
 */
public fun VerificationRuleSettings.maxBound(limit: Int) {
    bound {
        maxValue.set(limit)
    }
}

/**
 * A shortcut for
 * ```
 * bound {
 *     maxValue = limit
 * }
 * ```
 *
 * @see VerificationRuleSettings.bound
 */
public fun VerificationRuleSettings.maxBound(limit: Provider<Int>) {
    bound {
        maxValue.set(limit)
    }
}

// Default parameters values supported only in Kotlin.

/**
 * A shortcut for
 * ```
 * bound {
 *     minValue = limit
 *     coverageUnits = coverageUnits
 *     aggregationForGroup = aggregationForGroup
 * }
 * ```
 *
 * @see VerificationRuleSettings.bound
 */
public fun VerificationRuleSettings.minBound(
    limit: Int,
    coverageUnits: CoverageUnit = CoverageUnit.LINE,
    aggregationForGroup: AggregationType = AggregationType.COVERED_PERCENTAGE
) {
    bound {
        minValue.set(limit)
        this.coverageUnits.set(coverageUnits)
        this.aggregationForGroup.set(aggregationForGroup)
    }
}

/**
 * A shortcut for
 * ```
 * bound {
 *     maxValue = limit
 *     coverageUnits = coverageUnits
 *     aggregationForGroup = aggregation
 * }
 * ```
 *
 * @see VerificationRuleSettings.bound
 */
public fun VerificationRuleSettings.maxBound(
    limit: Int,
    coverageUnits: CoverageUnit = CoverageUnit.LINE,
    aggregationForGroup: AggregationType = AggregationType.COVERED_PERCENTAGE
) {
    bound {
        maxValue.set(limit)
        this.coverageUnits.set(coverageUnits)
        this.aggregationForGroup.set(aggregationForGroup)
    }
}
