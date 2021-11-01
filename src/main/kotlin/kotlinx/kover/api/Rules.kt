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
interface VerificationRule {
    /**
     * Custom name of the rule.
     */
    public var name: String?
        @Input @Nullable @Optional get

    public val bounds: List<VerificationBound>
        @Input get

    public fun bound(configureBound: Action<VerificationBound>)
}

interface VerificationBound {
    /**
     * Minimal value to compare with counter value.
     */
    public var minValue: Int?
        @Input @Nullable @Optional get

    /**
     * Maximal value to compare with counter value.
     */
    public var maxValue: Int?
        @Input @Nullable @Optional get

    /**
     * Type of lines counter value to compare with minimal and maximal values if them defined.
     * Default is [VerificationValueType.COVERED_LINES_PERCENTAGE]
     */
    public var valueType: VerificationValueType
        @Input get
}

/**
 * Type of lines counter value to compare with minimal and maximal values if them defined.
 */
public enum class VerificationValueType {
    COVERED_LINES_COUNT,
    MISSED_LINES_COUNT,
    COVERED_LINES_PERCENTAGE
}
