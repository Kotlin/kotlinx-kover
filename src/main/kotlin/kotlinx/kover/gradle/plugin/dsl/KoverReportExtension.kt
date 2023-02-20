/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

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
}

public interface KoverReportFilter : KoverClassDefinitions {
    public override fun className(vararg className: String)

    public override fun className(classNames: Iterable<String>)

    public override fun packageName(vararg className: String)

    public override fun packageName(classNames: Iterable<String>)

    public fun annotatedBy(vararg annotationName: String)
}

public interface KoverHtmlReportConfig : KoverGeneralHtmlReportConfig {
    public var onCheck: Boolean?

    public fun setReportDir(dir: File)
    public fun setReportDir(dir: Provider<Directory>)
}

public interface KoverGeneralHtmlReportConfig {
    public var title: String?

    public fun filters(config: Action<KoverReportFilters>)
}

public interface KoverXmlReportConfig : KoverGeneralXmlReportConfig {
    public var onCheck: Boolean?

    public fun setReportFile(xmlFile: File)
    public fun setReportFile(xmlFile: Provider<RegularFile>)
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
     * Specifies optional user-specified name of the rule.
     */
    public var name: String?

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
    PACKAGE
}
