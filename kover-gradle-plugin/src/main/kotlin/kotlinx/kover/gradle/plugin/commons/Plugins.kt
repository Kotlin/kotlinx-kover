/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.commons

import kotlinx.kover.gradle.plugin.util.bean
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer

/**
 * The ID of the Kotlin JVM Gradle plugin.
 */
internal const val KOTLIN_JVM_PLUGIN_ID = "kotlin"

/**
 * The ID of the Kotlin Multiplatform Gradle plugin.
 */
internal const val KOTLIN_MULTIPLATFORM_PLUGIN_ID = "kotlin-multiplatform"

/**
 * The ID of the Kotlin Android Gradle plugin.
 */
internal const val KOTLIN_ANDROID_PLUGIN_ID = "kotlin-android"

/**
 * The ID of the Kotlin Android Gradle plugin.
 */
internal const val ANDROID_BASE_PLUGIN_ID = "com.android.base"

/**
 * The plugin ID for the Android application Gradle plugin.
 */
internal const val ANDROID_APP_PLUGIN_ID = "com.android.application"

/**
 * The plugin ID for the Android library Gradle plugin.
 */
internal const val ANDROID_LIB_PLUGIN_ID = "com.android.library"

/**
 * The plugin ID for the Android dynamic feature Gradle plugin.
 */
internal const val ANDROID_DYNAMIC_PLUGIN_ID = "com.android.dynamic-feature"

internal fun Project.hasAndroid9WithKotlin() = pluginManager.hasPlugin(ANDROID_BASE_PLUGIN_ID) && !hasAnyKotlinPlugin() && hasKotlinExtension()

internal fun Project.hasAnyKotlinPlugin() = pluginManager.hasPlugin(KOTLIN_JVM_PLUGIN_ID) || pluginManager.hasPlugin(KOTLIN_ANDROID_PLUGIN_ID) || pluginManager.hasPlugin(KOTLIN_MULTIPLATFORM_PLUGIN_ID)

internal fun Project.hasKotlinExtension() = extensions.findByName("kotlin") != null

/**
 * Returns the major version of the Android Gradle plugin.
 *
 * @return the major version of the Android Gradle plugin or 0 if the version can't be read.
 */
internal fun ExtensionContainer.androidMajorVersion(): Int {
    val androidComponents = findByName("androidComponents")?.bean()?: return 0
    return androidComponents.beanOrNull("pluginVersion")?.valueOrNull<Int>("major") ?: 0
}
