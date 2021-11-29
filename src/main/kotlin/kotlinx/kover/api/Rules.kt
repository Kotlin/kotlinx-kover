/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.api

import org.gradle.api.*
import org.gradle.api.tasks.*
import javax.annotation.*

/**
 * Simple verification rule for code coverage.
 * Works only with lines counter.
 */
public interface VerificationRule {
    /**
     * Custom name of the rule.
     */
    @get:Input
    @get:Nullable
    @get:Optional
    public var name: String?

    /**
     * Added constraints on the values of code coverage metrics.
     */
    @get:Input
    public val bounds: List<VerificationBound>

    /**
     * Add a constraint on the value of the code coverage metric.
     */
    public fun bound(configureBound: Action<VerificationBound>)
}

public interface VerificationBound {
    /**
     * Minimal value to compare with counter value.
     */
    @get:Input
    @get:Nullable
    @get:Optional
    public var minValue: Int?

    /**
     * Maximal value to compare with counter value.
     */
    @get:Input
    @get:Nullable
    @get:Optional
    public var maxValue: Int?

    /**
     * Type of lines counter value to compare with minimal and maximal values if them defined.
     * Default is [VerificationValueType.COVERED_LINES_PERCENTAGE]
     */
    @get:Input
    public var valueType: VerificationValueType
}

/**
 * Type of lines counter value to compare with minimal and maximal values if them defined.
 */
public enum class VerificationValueType {
    COVERED_LINES_COUNT,
    MISSED_LINES_COUNT,
    COVERED_LINES_PERCENTAGE
}
