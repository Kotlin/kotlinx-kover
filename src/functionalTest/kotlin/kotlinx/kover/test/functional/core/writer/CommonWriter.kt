/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.core.writer

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.core.*
import java.io.PrintWriter

internal fun PrintWriter.printClassFilters(classFilters: KoverClassFilters, slice: ProjectSlice, indents: Int) {
    if (classFilters.excludes.isNotEmpty()) {
        indented(indents, "excludes".addAllList(classFilters.excludes, slice.language))
    }
    if (classFilters.includes.isNotEmpty()) {
        indented(indents, "includes".addAllList(classFilters.includes, slice.language))
    }
}

internal fun PrintWriter.printVerify(state: TestKoverVerifyConfigState, slice: ProjectSlice, indents: Int) {
    val onCheck = state.onCheck
    val rules = state.rules
    if (onCheck == null && rules.isEmpty()) return

    indented(indents, "verify {")
    if (onCheck != null) {
        indented(indents + 1, "onCheck".setProperty(onCheck.toString(), slice.language))
    }
    rules.forEach { rule ->
        indented(indents + 1, "rule {")
        if (rule.isEnabled != null) {
            indented(indents + 2, "isEnabled = ${rule.isEnabled}")
        }
        if (rule.name != null) {
            indented(indents + 2, "name = ${rule.name?.text(slice.language)}")
        }
        if (rule.target != null) {
            indented(indents + 2, "target = ${rule.target?.enum(slice.language)}")
        }
        if (rule.overrideClassFilters != null) {
            indented(indents + 2, "overrideClassFilters {")
            printClassFilters(rule.overrideClassFilters!!, slice, indents + 3)
            indented(indents + 2, "}")
        }
        rule.bounds.forEach { bound ->
            indented(indents + 2, "bound {")
            if (bound.minValue != null) {
                indented(indents + 3, "minValue = ${bound.minValue}")
            }
            if (bound.maxValue != null) {
                indented(indents + 3, "maxValue = ${bound.maxValue}")
            }
            if (bound.counter != null) {
                indented(indents + 3, "counter = ${bound.counter?.enum(slice.language)}")
            }
            if (bound.valueType != null) {
                indented(indents + 3, "valueType = ${bound.valueType?.enum(slice.language)}")
            }
            indented(indents + 2, "}")
        }
        indented(indents + 1, "}")
    }
    indented(indents, "}")
}
