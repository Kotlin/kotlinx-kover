/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.writer

import kotlinx.kover.test.functional.framework.configurator.*

internal fun FormattedScriptAppender.writeKoverMerged(merged: TestKoverMergedConfig?) {
    block("koverMerged", merged != null) {
        writeEnabled(merged!!.enabled)
        writeFilters(merged.filters)
        writeVerify(merged.verify)
    }
}

private fun FormattedScriptAppender.writeEnabled(isEnabled: Boolean) {
    lineIf(isEnabled, "enable()")
}

private fun FormattedScriptAppender.writeFilters(state: TestKoverMergedFiltersConfig) {
    val classes = state.classes
    val projects = state.projects

    block("filters", projects != null || classes != null) {
        block("classes", classes != null && (classes.excludes.isNotEmpty() || classes.includes.isNotEmpty())) {
            writeClassFilterContent(classes!!)
        }
        block("projects", projects != null) {
            lineIf(projects!!.excludes.isNotEmpty(), "excludes".addAllList(projects.excludes, language))
        }
    }
}
