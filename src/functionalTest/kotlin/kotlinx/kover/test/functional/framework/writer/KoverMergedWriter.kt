/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.writer

import kotlinx.kover.test.functional.framework.configurator.*

internal fun FormattedScriptAppender.writeKoverMerged(merged: TestKoverMergedConfig?) {
    block("koverMerged", merged != null) {
        writeEnabled(merged!!.enabled)
        writeFilters(merged.filters)
        writeXmlReport(merged.xml)
        writeVerify(merged.verify)
    }
}

private fun FormattedScriptAppender.writeEnabled(isEnabled: Boolean) {
    lineIf(isEnabled, "enable()")
}

private fun FormattedScriptAppender.writeFilters(state: TestKoverMergedFiltersConfig) {
    val classes = state.classes
    val annotations = state.annotations
    val projects = state.projects

    block("filters", projects != null || classes != null || annotations != null) {
        block("classes", classes != null && (classes.excludes.isNotEmpty() || classes.includes.isNotEmpty())) {
            writeClassFilterContent(classes!!)
        }
        block("projects", projects != null) {
            lineIf(projects!!.excludes.isNotEmpty(), "excludes".addAllList(projects.excludes, language))
        }
        block("annotations", annotations != null && annotations.excludes.isNotEmpty()) {
            line("excludes".addAllList(annotations!!.excludes, language))
        }
    }
}

private fun FormattedScriptAppender.writeXmlReport(state: TestMergedXmlConfig) {
    val overrideClassFilter = state.overrideClassFilter
    val overrideAnnotationFilter = state.overrideAnnotationFilter

    block(
        "xmlReport",
        state.onCheck != null || state.reportFile != null || overrideClassFilter != null || overrideAnnotationFilter != null
    ) {
        block("overrideClassFilter", overrideClassFilter != null) {
            writeClassFilterContent(overrideClassFilter!!)
        }
        block("overrideAnnotationFilter", overrideAnnotationFilter != null) {
            line("excludes".addAllList(overrideAnnotationFilter!!.excludes, language))
        }
    }
}
