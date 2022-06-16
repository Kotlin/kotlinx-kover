/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.core.writer

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.core.*
import kotlinx.kover.test.functional.core.ProjectSlice
import kotlinx.kover.test.functional.core.TestKoverProjectConfigState
import java.io.*

internal fun PrintWriter.printKover(kover: TestKoverProjectConfigState?, slice: ProjectSlice, indents: Int) {
    if (kover == null) return

    indented(indents, "kover {")
    printDisabled(kover.isDisabled, slice, indents + 1)
    printEngine(kover.engine, slice, indents + 1)
    printFilters(kover.filters, slice, indents + 1)
    printVerify(kover.verify, slice, indents + 1)
    indented(indents, "}")
}


private fun PrintWriter.printEngine(scriptEngine: CoverageEngineVariant?, slice: ProjectSlice, indents: Int) {
    if (scriptEngine == null && slice.engine == null) return

    val value = if (slice.engine != null) {
        val clazz =
            if (slice.engine == CoverageEngineVendor.INTELLIJ) DefaultIntellijEngine::class else DefaultJacocoEngine::class
        clazz.obj(slice.language)
    } else {
        val clazz =
            if (scriptEngine!!.vendor == CoverageEngineVendor.INTELLIJ) IntellijEngine::class else JacocoEngine::class
        val new = if (slice.language == GradleScriptLanguage.KOTLIN) {
            clazz.qualifiedName
        } else {
            clazz.qualifiedName
        }
        "$new(\"${scriptEngine.version}\")"
    }

    indented(indents, "engine".setProperty(value, slice.language))
}

private fun PrintWriter.printDisabled(isDisabled: Boolean?, slice: ProjectSlice, indents: Int) {
    if (isDisabled == null) return

    if (slice.language == GradleScriptLanguage.KOTLIN) {
        indented(indents, "isDisabled.set($isDisabled)")
    } else {
        indented(indents, "disabled = $isDisabled")
    }
}

private fun PrintWriter.printFilters(state: TestKoverProjectFiltersState, slice: ProjectSlice, indents: Int) {
    val classes = state.classes
    val sourcesets = state.sourcesets
    if (sourcesets == null && classes == null) return

    indented(indents, "filters {")
    if (classes != null && (classes.excludes.isNotEmpty() || classes.includes.isNotEmpty())) {
        indented(indents + 1, "classes {")
        printClassFilters(classes, slice, indents + 2)
        indented(indents + 1, "}")
    }

    if (sourcesets != null) {
        indented(indents + 1, "sourcesets {")
        if (sourcesets.excludes.isNotEmpty()) {
            indented(indents + 2, "excludes".addAllList(sourcesets.excludes, slice.language))
        }
        indented(indents + 2, "excludeTests = " + sourcesets.excludeTests)
        indented(indents + 1, "}")
    }

    indented(indents, "}")
}

