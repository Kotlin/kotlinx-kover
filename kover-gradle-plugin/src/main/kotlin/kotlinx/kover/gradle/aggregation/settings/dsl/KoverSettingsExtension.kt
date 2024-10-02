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

    /**
     * Set of projects, classes and tests from which will not be included in the reports.
     *
     * See details in [skipProjects].
     */
    val skipProjects: SetProperty<String>

    /**
     * Specify projects, classes and tests from which will not be included in the reports.
     *
     * This means that all classes declared in these projects will be excluded from the report,
     * as well as all test tasks will not be instrumented - accordingly, coverage of them will not be taken into account.
     *
     * Several project writing syntaxes are supported:
     *  - project name
     *  - full path (starts with the symbol `:`)
     *  - abbreviated path (does not start with `:`, but contains the path separator `:`)
     *
     * ```
     * skipProjects(":project1", "project2", "nested:subproject")
     * ```
     */
    fun skipProjects(vararg projects: String) {
        skipProjects.addAll(projects.toList())
    }

    /**
     * Instance for configuring instrumentation in all non-skipped Gradle projects.
     *
     * See details in [instrumentation].
     */
    val instrumentation: InstrumentationSettings

    /**
     * Instrumentation settings for the all non-skipped Gradle project.
     *
     * Instrumentation is the modification of classes when they are loaded into the JVM, which helps to determine which code was called and which was not.
     * Instrumentation changes the bytecode of the class, so it may disable some JVM optimizations, slow down performance and concurrency tests, and may also be incompatible with other instrumentation libraries.
     *
     * For this reason, it may be necessary to fine-tune the instrumentation, for example, disabling instrumentation for problematic classes. Note that such classes would be marked as uncovered because of that.
     *
     * Example:
     * ```
     *  instrumentation {
     *      // disable instrumentation of specified classes in test tasks
     *      excludedClasses.addAll("foo.bar.*Biz", "*\$Generated")
     *
     *      // enable instrumentation only for specified classes. Classes in excludedClasses have priority over classes from includedClasses.
     *      includedClasses.addAll("foo.bar.*")
     *  }
     * ```
     */
    fun instrumentation(action: Action<InstrumentationSettings>)

    /**
     * Instance for configuring merged reports.
     *
     * See details in [reports].
     */
    val reports: ReportsSettings

    /**
     * Configure Kover merged reports
     * ```
     * reports {
     *     verify.warningInsteadOfFailure = false
     *     verify.rule("First rule") { minBound(50) }
     *
     *     verify {
     *          warningInsteadOfFailure = false
     *          rule("Second rule") {
     *              minBound(75)
     *          }
     *     }
     * }
     * ```
     */
    fun reports(action: Action<ReportsSettings>)
}

@KoverGradlePluginDsl
public interface InstrumentationSettings {
    /**
     * Disable instrumentation in test tasks of specified classes in all Gradle projects.
     *
     * Classes in [excludedClasses] have priority over classes from [includedClasses].
     */
    public val excludedClasses: SetProperty<String>

    /**
     * Enable instrumentation in test tasks only of specified classes in all Gradle projects.
     * All other classes will not be instrumented.
     *
     * Classes in [excludedClasses] have priority over classes from [includedClasses].
     */
    public val includedClasses: SetProperty<String>
}

@KoverGradlePluginDsl
public interface ProjectInstrumentationSettings {
    /**
     * Specifies not to use test task with passed names to measure coverage.
     * These tasks will also not be called when generating Kover reports and these tasks will not be instrumented even if you explicitly run them.
     */
    public val disabledForTestTasks: SetProperty<String>

    /**
     * Disable instrumentation in test tasks of specified classes
     *
     * Classes in [excludedClasses] have priority over classes from [includedClasses].
     */
    public val excludedClasses: SetProperty<String>

    /**
     * Enable instrumentation in test tasks only of specified classes.
     * All other classes will not be instrumented.
     *
     * Classes in [excludedClasses] have priority over classes from [includedClasses].
     */
    public val includedClasses: SetProperty<String>
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
     * Add new coverage verification rule to check in each non-skipped project.
     *
     * When checking the rule, only the classes declared in the corresponding project will be analyzed.
     *
     * The `projectName` and `projectPath` properties are used to identify the project.
     *
     * The specified action will be called for each non-skipped Gradle project,
     * that is, it must be taken into account that it is performed several times.
     *
     * ```
     * eachProjectRule {
     *     if (projectPath != ":badly-covered-project") {
     *         // all other projects should be covered with 80%
     *         minBound(80)
     *     } else {
     *         // :badly-covered-project should be covered with 50%
     *         minBound(50)
     *     }
     * }
     * ```
     */
    public fun eachProjectRule(action: Action<ProjectVerificationRuleSettings>)

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
public interface ProjectVerificationRuleSettings: VerificationRuleSettings {
    /**
     * Get the name of the project for which classes coverage is being checked.
     */
    val projectName: String

    /**
     * Get the path of the project for which classes coverage is being checked
     */
    val projectPath: String
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


@KoverGradlePluginDsl
public interface KoverProjectExtension {
    /**
     * Instance for configuring instrumentation.
     *
     * See details in [instrumentation].
     */
    val instrumentation: ProjectInstrumentationSettings

    /**
     * Configure instrumentation for current project.
     *
     * ```
     * instrumentation {
     *     disabledForTestTasks.add("test")
     *     excludedClasses.add("*.excluded.*")
     *     includedClasses.add("my.project.*")
     * }
     * ```
     */
    fun instrumentation(action: Action<ProjectInstrumentationSettings>)
}