/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

import kotlinx.kover.gradle.plugin.commons.*
import org.gradle.api.*
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import java.io.*

public interface KoverReportExtension {
    public fun filters(config: Action<KoverReportFilters>)

    public fun html(config: Action<KoverHtmlReportConfig>)

    public fun xml(config: Action<KoverXmlReportConfig>)

    public fun verify(config: Action<KoverVerifyReportConfig>)
}

public interface KoverGeneralReportExtension {
    public fun filters(config: Action<KoverReportFilters>)

    public fun html(config: Action<KoverGeneralHtmlReportConfig>)

    public fun xml(config: Action<KoverGeneralXmlReportConfig>)

    public fun verify(config: Action<KoverGeneralVerifyReportConfig>)
}

public interface KoverReportFilters {
    public fun excludes(config: Action<KoverReportFilter>)

    public fun includes(config: Action<KoverReportFilter>)

    @Deprecated(
        message = "Class filters was moved into 'excludes { classes(\"fq.name\") }' or 'includes { classes(\"fq.name\") }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public fun classes(block: () -> Unit) { }

    @Deprecated(
        message = "Class inclusion filters was moved into 'includes { classes(\"fq.name\") }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public val includes: MutableList<String>
        get() = mutableListOf()

    @Deprecated(
        message = "Class exclusion filters was moved into 'excludes { classes(\"fq.name\") }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public val excludes: MutableList<String>
        get() = mutableListOf()
}

public interface KoverReportFilter: KoverClassDefinitions {
    public override fun classes(vararg names: String)

    public override fun classes(names: Iterable<String>)

    public override fun packages(vararg names: String)

    public override fun packages(names: Iterable<String>)

    public fun annotatedBy(vararg annotationName: String)
}

public interface KoverHtmlReportConfig : KoverGeneralHtmlReportConfig {
    public var onCheck: Boolean?

    public fun setReportDir(dir: File)
    public fun setReportDir(dir: Provider<Directory>)

    @Deprecated(
        message = "Property was removed. Use function 'setReportDir(file)'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("setReportDir"),
        level = DeprecationLevel.ERROR
    )
    public val reportDir: Nothing?
        get() = null

    @Deprecated(
        message = "Use function 'filters' instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("filters"),
        level = DeprecationLevel.ERROR
    )
    public fun overrideFilters(block: () -> Unit) { }
}

public interface KoverGeneralHtmlReportConfig {
    public var title: String?

    public fun filters(config: Action<KoverReportFilters>)
}

public interface KoverXmlReportConfig : KoverGeneralXmlReportConfig {
    public var onCheck: Boolean?

    public fun setReportFile(xmlFile: File)
    public fun setReportFile(xmlFile: Provider<RegularFile>)

    @Deprecated(
        message = "Property was removed. Use function 'setReportFile(file)'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("setReportFile"),
        level = DeprecationLevel.ERROR
    )
    public val reportFile: Nothing?
        get() = null

    @Deprecated(
        message = "Use function 'filters' instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("filters"),
        level = DeprecationLevel.ERROR
    )
    public fun overrideFilters(block: () -> Unit) { }
}

public interface KoverGeneralXmlReportConfig {
    public fun filters(config: Action<KoverReportFilters>)
}

public interface KoverVerifyReportConfig : KoverGeneralVerifyReportConfig {
    public var onCheck: Boolean
}

public interface KoverGeneralVerifyReportConfig {
    public fun rule(config: Action<KoverVerifyRule>)
    public fun rule(name: String, config: Action<KoverVerifyRule>)
}

/**
 * Describes a single Kover verification task rule (that is part of Gradle's verify),
 * with the following configurable parameters:
 *
 * - Which classes and packages are included or excluded into the current rule
 * - What coverage bounds are enforced by current rules
 * - What kind of bounds (branches, lines, bytecode instructions) are checked by bound rules.
 */
public interface KoverVerifyRule {
    /**
     * Specifies that the rule is checked during verification.
     */
    public var isEnabled: Boolean

    /**
     * Specifies by which entity the code for separate coverage evaluation will be grouped.
     */
    public var entity: GroupingEntityType

    /**
     * Specifies the set of Kover report filters that control
     * included or excluded classes and packages for verification.
     *
     * An example of filter configuration:
     * ```
     * filters {
     *    excludes {
     *        // Do not include deprecated package into verification coverage data
     *        className("org.jetbrains.deprecated.*")
     *     }
     * }
     * ```
     *
     * @see KoverReportFilter
     */
    public fun filters(config: Action<KoverReportFilters>)

    /**
     * Specifies the set of verification rules that control the
     * coverage conditions required for the verification task to pass.
     *
     * An example of bound configuration:
     * ```
     * // At least 75% of lines should be covered in order for build to pass
     * bound {
     *     aggregation = AggregationType.COVERED_PERCENTAGE // Default aggregation
     *     metric = MetricType.LINE
     *     minValue = 75
     * }
     * ```
     *
     * @see KoverVerifyBound
     */
    public fun bound(config: Action<KoverVerifyBound>)

    // Added since default parameter values are not supported in the Groovy.
    public fun minBound(minValue: Int)

    public fun maxBound(maxValue: Int)

    // Default parameters values supported only in Kotlin.

    public fun minBound(
        minValue: Int,
        metric: MetricType = MetricType.LINE,
        aggregation: AggregationType = AggregationType.COVERED_PERCENTAGE
    )

    public fun maxBound(
        maxValue: Int,
        metric: MetricType = MetricType.LINE,
        aggregation: AggregationType = AggregationType.COVERED_PERCENTAGE
    )

    public fun bound(
        minValue: Int,
        maxValue: Int,
        metric: MetricType = MetricType.LINE,
        aggregation: AggregationType = AggregationType.COVERED_PERCENTAGE
    )

    @Deprecated(
        message = "Property was renamed to 'entity'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("entity"),
        level = DeprecationLevel.ERROR
    )
    public var target: GroupingEntityType
        get() = GroupingEntityType.APPLICATION
        set(@Suppress("UNUSED_PARAMETER") value) {}

    @Deprecated(
        message = "Property 'name' was removed, specify rule name in `rule(myName) { ... }` function. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public var name: String?

    @Deprecated(
        message = "Use function 'filters' instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("filters"),
        level = DeprecationLevel.ERROR
    )
    public fun overrideClassFilter(block: () -> Unit) {}
}

/**
 * Describes a single bound for the verification rule to enforce;
 * Bound specifies what type of coverage is enforced (branches, lines, instructions),
 * how coverage is aggerageted (raw number or percents) and what numerical values of coverage
 * are acceptable.
 */
public interface KoverVerifyBound {
    /**
     * Specifies minimal value to compare with counter value.
     */
    public var minValue: Int?

    /**
     * Specifies maximal value to compare with counter value.
     */
    public var maxValue: Int?

    /**
     * Specifies which metric is used for code coverage verification.
     */
    public var metric: MetricType

    /**
     * Specifies type of lines counter value to compare with minimal and maximal values if them defined.
     * Default is [AggregationType.COVERED_PERCENTAGE]
     */
    public var aggregation: AggregationType

    @Deprecated(
        message = "Property was renamed to 'metric'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("metric"),
        level = DeprecationLevel.ERROR
    )
    public var counter: MetricType
        get() = MetricType.LINE
        set(@Suppress("UNUSED_PARAMETER") value) {}

    @Deprecated(
        message = "Property was renamed to 'aggregation'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("aggregation"),
        level = DeprecationLevel.ERROR
    )
    public var valueType: AggregationType
        get() = AggregationType.COVERED_PERCENTAGE
        set(@Suppress("UNUSED_PARAMETER") value) {}


}

/**
 * Type of the metric to evaluate code coverage.
 */
public enum class MetricType {
    /**
     * Numer of lines.
     */
    LINE,

    /**
     * Number of JVM bytecode instructions.
     */
    INSTRUCTION,

    /**
     * Number of branches covered.
     */
    BRANCH
}

/**
 * Type of counter value to compare with minimal and maximal values if them defined.
 */
public enum class AggregationType(val isPercentage: Boolean) {
    COVERED_COUNT(false),
    MISSED_COUNT(false),
    COVERED_PERCENTAGE(true),
    MISSED_PERCENTAGE(true)
}

/**
 *  Entity type for grouping code to coverage evaluation.
 */
public enum class GroupingEntityType {
    /**
     * Counts the coverage values for all code.
     */
    APPLICATION,

    /**
     * Counts the coverage values for each class separately.
     */
    CLASS,

    /**
     * Counts the coverage values for each package that has classes separately.
     */
    PACKAGE;

    @Deprecated(
        message = "Entry was renamed to 'APPLICATION'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("APPLICATION"),
        level = DeprecationLevel.ERROR
    )
    object ALL
}
