/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import kotlinx.kover.gradle.plugin.util.*
import org.gradle.api.*

/*
Since the Kover and Android plug-ins can be in different class loaders (declared in different projects), the plug-ins are stored in a single instance in the loader of the project where the plug-in was used for the first time.
Because of this, Kover may not have direct access to the Android plugin classes, and variables and literals of this types cannot be declared .

To work around this limitation, working with objects is done through reflection, using a dynamic Gradle wrapper.
 */
internal fun LocatorContext.initKotlinAndroidPluginLocator() {
    listener.onApplyPlugin(KotlinPluginType.ANDROID)

    project.pluginManager.withPlugin(ANDROID_APP_PLUGIN_ID) {
        project.afterAndroidPluginApplied { processAndroidTarget() }
    }
    project.pluginManager.withPlugin(ANDROID_LIB_PLUGIN_ID) {
        project.afterAndroidPluginApplied { processAndroidTarget() }
    }
}

private fun LocatorContext.processAndroidTarget() {
    val kotlinExtension = project.getKotlinExtension()
    locateAndroidCompilations(kotlinExtension)

    listener.finalize()
}

private fun LocatorContext.locateAndroidCompilations(kotlinExtension: DynamicBean) {
    val androidExtension = project.extensions.findByName("android")?.bean()
        ?: throw KoverCriticalException("Kover requires extension with name 'android' for project '${project.path}' since it is recognized as Kotlin/Android project")

    val kotlinTarget = kotlinExtension["target"]

    val androidCompilations = project.androidCompilationKits(androidExtension, koverExtension, kotlinTarget)
    listener.android(androidCompilations)
}
