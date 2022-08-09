/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.commons

import org.gradle.api.tasks.Input


internal data class AgentFilters(
    @get:Input val includesClasses: List<String>,
    @get:Input val excludesClasses: List<String>
) {
    fun appendExcludedTo(vararg excludesClasses: String): AgentFilters {
        return AgentFilters(
            this.includesClasses,
            this.excludesClasses + excludesClasses
        )
    }
}
