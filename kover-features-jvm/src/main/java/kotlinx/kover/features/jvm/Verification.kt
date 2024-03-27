/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.features.jvm

import java.math.BigDecimal


/**
 * Entity type for grouping code to coverage evaluation.
 */
public enum class GroupingBy {
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

/**
 * Type of the metric to evaluate code coverage.
 */
public enum class CoverageUnit {
    /**
     * Number of lines.
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
public enum class AggregationType {
    COVERED_COUNT,
    MISSED_COUNT,
    COVERED_PERCENTAGE,
    MISSED_PERCENTAGE
}

/**
 * Evaluated coverage [value] for given entity with name [entityName].
 *
 * Entity could be class, package, etc
 */
public data class CoverageValue(val entityName: String?, val value: BigDecimal)

/**
 * Describes a single bound for the verification rule to enforce
 */
public data class Bound(
    val minValue: BigDecimal?,
    val maxValue: BigDecimal?,
    val coverageUnits: CoverageUnit,
    val aggregationForGroup: AggregationType
)

/**
 * Verification rule - a named set of bounds of coverage value to check.
 */
public data class Rule(val name: String, val groupBy: GroupingBy, val bounds: List<Bound>)

/**
 * Violation of verification rule.
 */
public data class RuleViolations(val rule: Rule, val violations: List<BoundViolation>)

/**
 * Violation of verification bound.
 */
public data class BoundViolation(val bound: Bound, val isMax: Boolean, val value: BigDecimal, val entityName: String?)
