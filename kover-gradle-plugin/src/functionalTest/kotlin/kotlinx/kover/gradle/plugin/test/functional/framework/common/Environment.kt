/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.common

import java.io.File

/**
 * Name of environment variable with Android SDK path.
 */
internal const val ANDROID_HOME_ENV = "ANDROID_HOME"

/**
 * Version of current Kover build.
 */
internal val koverVersion = System.getProperty("koverVersion")
    ?: throw Exception("System property 'koverVersion' not defined for functional tests")

/**
 * Version of most recent Kover release.
 */
internal val releaseVersion = System.getProperty("kover.release.version")
    ?: throw Exception("System property 'kover.release.version' not defined for functional tests")

/**
 * Kotlin version for sliced generated tests.
 */
internal val kotlinVersion = System.getProperty("kotlinVersion")
    ?: throw Exception("System property 'kotlinVersion' not defined for functional tests")

/**
 * Overridden Kotlin version for all tests.
 */
internal val overriddenTestKotlinVersion = System.getProperty("kover.test.kotlin.version")

/**
 * Custom version of Gradle runner for functional tests.
 */
internal val gradleWrapperVersion: String? = System.getProperty("kover.test.gradle.version")

/**
 * Flag to run functional tests within debug agent.
 */
internal val isDebugEnabled: Boolean = System.getProperty("isDebugEnabled") != null

/**
 * Flag to ignore all Android functional tests.
 */
internal val isAndroidTestDisabled: Boolean = System.getProperty("kover.test.android.disable") != null

/**
 * Result path to the Android SDK. `null` if not defined.
 */
internal val androidSdkDir: String? = System.getProperty("kover.test.android.sdk")?: System.getenv(ANDROID_HOME_ENV)

/**
 * Path to the local maven repository with the current Kover build.
 */
internal val localRepositoryPath: String = System.getProperty("localRepositoryPath")
    ?: throw Exception("System property 'localRepositoryPath' not defined for functional tests")


internal fun logInfo(message: String) {
    if (testLogsEnabled) {
        println(message)
    }
}

internal val gradleWrappersRoot = File("gradle-wrappers")

internal val defaultGradleWrapperDir = File("..")

internal val testLogsEnabled = System.getProperty("testLogsEnabled") == "true"
