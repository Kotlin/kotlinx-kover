/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

/**
 * A locator that works if no Kotlin plugins were detected.
 */
internal fun LocatorContext.initNoKotlinPluginLocator() {
    project.afterEvaluate {
        listener.finalizeIfNoKotlinPlugin()
    }
}
