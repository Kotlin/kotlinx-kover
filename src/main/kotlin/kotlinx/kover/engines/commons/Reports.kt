/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.commons

import kotlinx.kover.api.*
import java.math.BigDecimal


internal class ReportVerificationRule(
    val id: Int,
    val name: String?,
    val target: VerificationTarget,
    val filters: KoverClassFilter?,
    val bounds: List<ReportVerificationBound>
)

internal class ReportVerificationBound(
    val id: Int,
    val minValue: BigDecimal?,
    val maxValue: BigDecimal?,
    val counter: CounterType,
    val valueType: VerificationValueType
)

private val regexMetacharactersSet = "<([{\\^-=$!|]})+.>".toSet()

/**
 * Replaces characters `*` or `.` to `.*` and `.` regexp characters.
 */
internal fun String.wildcardsToRegex(): String {
    // in most cases, the characters `*` or `.` will be present therefore, we increase the capacity in advance
    val builder = StringBuilder(length * 2)

    forEach { char ->
        when (char) {
            in regexMetacharactersSet -> builder.append('\\').append(char)
            '*' -> builder.append('.').append("*")
            '?' -> builder.append('.')
            else -> builder.append(char)
        }
    }

    return builder.toString()
}

internal val ONE_HUNDRED = 100.toBigDecimal()
