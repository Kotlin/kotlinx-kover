/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import org.gradle.api.*


internal interface SetupLocator {
    val kotlinPlugin: AppliedKotlinPlugin

    fun locateSingle(koverExtension: KoverProjectExtensionImpl): KoverSetup<*>

    fun locateMultiple(koverExtension: KoverProjectExtensionImpl): List<KoverSetup<*>> {
        return listOf(locateSingle(koverExtension))
    }
}


internal object SetupLocatorFactory {

    fun get(project: Project): SetupLocator = when {
        KotlinJvmLocator.isApplied(project) -> KotlinJvmLocator(project)
        KotlinMultiPlatformLocator.isApplied(project) -> KotlinMultiPlatformLocator(project)
        KotlinAndroidLocator.isApplied(project) -> KotlinAndroidLocator(project)
        else -> EmptyLocator(project)
    }

}
