/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.core.writer

import kotlinx.kover.test.functional.core.*
import kotlinx.kover.test.functional.core.ProjectSlice
import java.io.*

internal fun PrintWriter.printKoverMerged(merged: TestKoverMergedConfigState?, slice: ProjectSlice, indents: Int) {
    if (merged == null) return

    indented(indents, "koverMerged {")
    printEnabled(merged.enabled, indents + 1)
    printFilters(merged.filters, slice, indents + 1)
    printVerify(merged.verify, slice, indents + 1)
    indented(indents, "}")
}

private fun PrintWriter.printEnabled(isEnabled: Boolean, indents: Int) {
    if (isEnabled) {
        indented(indents, "enable()")
    }
}

private fun PrintWriter.printFilters(state: TestKoverMergedFiltersState, slice: ProjectSlice, indents: Int) {
    val classes = state.classes
    val projects = state.projects
    if (projects == null && classes == null) return

    indented(indents, "filters {")
    if (classes != null && (classes.excludes.isNotEmpty() || classes.includes.isNotEmpty())) {
        indented(indents + 1, "classes {")
        printClassFilter(classes, slice, indents + 2)
        indented(indents + 1, "}")
    }

    if (projects != null) {
        indented(indents + 1, "projects {")
        if (projects.excludes.isNotEmpty()) {
            indented(indents + 2, "excludes".addAllList(projects.excludes, slice.language))
        }
        indented(indents + 1, "}")
    }

    indented(indents, "}")
}
