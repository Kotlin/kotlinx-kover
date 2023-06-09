/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import org.gradle.api.Project

/**
 * A locator that works if no Kotlin plugins were detected.
 */
internal class EmptyLocator(
    project: Project,
    listener: CompilationsListenerWrapper
) {
    init {
        project.afterEvaluate {
            listener.defaultFinalize()
        }
    }
}
