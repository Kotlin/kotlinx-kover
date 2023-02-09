/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import org.gradle.api.*

/**
 * For instrumentation and report generation, a set of information is needed  (called setup),
 * which can be obtained by reading the settings of the Kotlin plugins  (and others).
 *
 * The locator is engaged in reading the settings of the applied plugins and, based on their settings,
 * collects this information in a universal form [KoverSetup].
 */
internal interface SetupLocator {
    val kotlinPlugin: AppliedKotlinPlugin

    /**
     * Collect information for projects that assume the presence of only one regular setup.
     *
     * Works for Kotlin JVM or Kotlin multi-platform plugins.
     */
    fun locateRegular(koverExtension: KoverProjectExtensionImpl): KoverSetup<*>

    /**
     * Collect information for projects that assume the presence of several named setups.
     *
     * Works only for Kotlin Android plugin.
     */
    fun locateAll(koverExtension: KoverProjectExtensionImpl): List<KoverSetup<*>> {
        throw KoverCriticalException("Not supported 'locateAll' setups")
    }
}

/**
 * A factory that creates a locator suitable for a specific project.
 */
internal object SetupLocatorFactory {

    fun get(project: Project): SetupLocator = when {
        KotlinJvmLocator.isApplied(project) -> KotlinJvmLocator(project)
        KotlinMultiPlatformLocator.isApplied(project) -> KotlinMultiPlatformLocator(project)
        KotlinAndroidLocator.isApplied(project) -> KotlinAndroidLocator(project)
        else -> EmptyLocator(project)
    }

}
