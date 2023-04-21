/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import org.gradle.api.*

/**
 * Locate information about Kotlin project's compilations.
 * This information is necessary for carrying out instrumentation and generating Kover reports.
 *
 * The locator is engaged in reading the settings of the applied plugins.
 */
internal interface CompilationKitLocator {
    fun locate(koverExtension: KoverProjectExtensionImpl): ProjectCompilation
}

/**
 * A factory that creates a locator suitable for a specific project.
 */
internal object CompilationsLocatorFactory {

    fun get(project: Project): CompilationKitLocator = when {
        KotlinJvmLocator.isApplied(project) -> KotlinJvmLocator(project)
        KotlinMultiPlatformLocator.isApplied(project) -> KotlinMultiPlatformLocator(project)
        KotlinAndroidLocator.isApplied(project) -> KotlinAndroidLocator(project)
        else -> EmptyLocator()
    }

}
