/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.core.writer

import kotlinx.kover.test.functional.core.*
import java.io.*

internal fun generateSettingsFile(
    sliceDir: File,
    slice: ProjectSlice,
    projects: Map<String, ProjectBuilderState>,
    localCache: Boolean
) {
    File(sliceDir, "settings.${slice.scriptExtension}").printWriter().use {
        it.println("""rootProject.name = "kover-functional-test"""")
        it.println()
        projects.keys.forEach { path ->
            if (path != ":") {
                it.println("""include("$path")""")
            }
        }

        if (localCache) {
            it.println(LOCAL_CACHE)
        }
    }
}

private const val LOCAL_CACHE = """
buildCache {
    local {
        directory = "${"$"}settingsDir/build-cache"
    }
}
"""
