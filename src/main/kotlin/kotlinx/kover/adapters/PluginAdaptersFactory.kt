/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.adapters

import kotlinx.kover.adapters.api.*
import org.gradle.api.*
import org.gradle.api.file.*
import java.io.*

internal fun createAdapters(): List<CompilationPluginAdapter> {
    return listOf(
        OldJavaPluginAdapter(),
        KotlinMultiplatformPluginAdapter(),
        AndroidPluginAdapter(),
        KotlinAndroidPluginAdapter()
    )
}

val Project.androidPluginIsApplied: Boolean
    get() {
        return plugins.findPlugin("android") != null || plugins.findPlugin("kotlin-android") != null
    }
