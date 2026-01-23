/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.appliers.origin.AllVariantOrigins
import kotlinx.kover.gradle.plugin.commons.KoverCriticalException
import kotlinx.kover.gradle.plugin.util.bean
import org.gradle.api.Project

/*
Since the Kover and Android plug-ins can be in different class loaders (declared in different projects), the plug-ins are stored in a single instance in the loader of the project where the plug-in was used for the first time.
Because of this, Kover may not have direct access to the Android plugin classes, and variables and literals of this types cannot be declared .

To work around this limitation, working with objects is done through reflection, using a dynamic Gradle wrapper.
 */
internal fun Project.locateKotlinAndroidVariants(variants: List<AndroidVariantInfo>): AllVariantOrigins {
    val kotlinExtension = project.getKotlinExtension()

    val androidExtension = project.extensions.findByName("android")?.bean()
        ?: throw KoverCriticalException("Kover requires extension with name 'android' for project '${project.path}' since it is recognized as Kotlin/Android project")

    val kotlinTarget = kotlinExtension["target"]

    val androidComponents = project.extensions.findByName("androidComponents")?.bean()
        ?: throw KoverCriticalException("Kover requires extension with name 'androidComponents' for project '${project.path}'. The minimum supported AGP version is 7.0.0")

    val majorVersion = androidComponents.beanOrNull("pluginVersion")?.valueOrNull<Int>("major") ?: 0
    val origins = if (majorVersion < 9) {
        project.androidCompilationKitsBefore9(androidExtension, kotlinTarget)
    } else {
        project.androidCompilationKits(androidExtension, variants, kotlinTarget)
    }
    return AllVariantOrigins(emptyList(), origins)
}
