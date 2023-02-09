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

public interface KoverCommonReportExtension {
    public fun filters(config: Action<KoverReportFilters>)

    public fun html(config: Action<KoverCommonHtmlReportConfig>)

    public fun xml(config: Action<KoverCommonXmlReportConfig>)

    public fun verify(config: Action<KoverCommonVerifyReportConfig>)
}

public interface KoverReportFilters {
    public fun excludes(config: Action<KoverReportFilter>)

    public fun includes(config: Action<KoverReportFilter>)
}

public interface KoverReportFilter: KoverClassDefinitions {
    public override fun className(vararg className: String)

    public override fun className(classNames: Iterable<String>)

    public override fun packageName(vararg className: String)

    public override fun packageName(classNames: Iterable<String>)

    public fun annotatedBy(vararg annotationName: String)
}

public interface KoverHtmlReportConfig: KoverCommonHtmlReportConfig {
    public var onCheck: Boolean?

    public fun setReportDir(dir: File)
    public fun setReportDir(dir: Provider<Directory>)
}

public interface KoverCommonHtmlReportConfig {
    public var title: String?

    public fun filters(config: Action<KoverReportFilters>)
}

public interface KoverXmlReportConfig: KoverCommonXmlReportConfig {
    public var onCheck: Boolean?

    public fun setReportFile(xmlFile: File)
    public fun setReportFile(xmlFile: Provider<RegularFile>)
}

public interface KoverCommonXmlReportConfig {
    public fun filters(config: Action<KoverReportFilters>)
}

public interface KoverVerifyReportConfig: KoverCommonVerifyReportConfig {
    public var onCheck: Boolean
}

public interface KoverCommonVerifyReportConfig {
    public fun rule(config: Action<KoverVerifyRule>)
    public fun rule(name: String, config: Action<KoverVerifyRule>)
}

public interface KoverVerifyRule {
    /**
     * Specifies that the rule will be checked during verification.
     */
    public var isEnabled: Boolean

    /**
     * Specifies custom name of the rule.
     */
    public var name: String?

    /**
     * Specifies by which entity the code for separate coverage evaluation will be grouped.
     */
    public var entity: GroupingEntityType

    public fun filters(config: Action<KoverReportFilters>)

    public fun bound(config: Action<KoverVerifyBound>)

    // Added since default parameter values are not supported in the Groovy.
    public fun minBound(minValue: Int)

    public fun maxBound(maxValue: Int)

    // Default parameters values supported only in Kotlin.

    public fun minBound(minValue: Int, metric: MetricType = MetricType.LINE, aggregation: AggregationType = AggregationType.COVERED_PERCENTAGE)

    public fun maxBound(maxValue: Int, metric: MetricType = MetricType.LINE, aggregation: AggregationType = AggregationType.COVERED_PERCENTAGE)

    public fun bound(minValue: Int, maxValue: Int, metric: MetricType = MetricType.LINE, aggregation: AggregationType = AggregationType.COVERED_PERCENTAGE)
}

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
     * Specifies which metric will be evaluation code coverage.
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
     * Evaluates coverage for lines.
     */
    LINE,

    /**
     * Evaluates coverage for JVM bytecode instructions.
     */
    INSTRUCTION,

    /**
     * Evaluates coverage for code branches excluded dead-branches.
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
