/*
 * Copyright 2017-2025 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    kotlin("multiplatform") version ("2.2.20")
    id("com.android.kotlin.multiplatform.library") version "8.12.0"
    id ("org.jetbrains.kotlinx.kover") version "0.9.1"
}

kotlin {
    androidLibrary {
        namespace = "org.jetbrains.kover.kml.lib"
        compileSdk = 33
        minSdk = 24

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        withHostTest { }
    }

    jvm()
}

dependencies {
    commonTestImplementation("junit:junit:4.13.2")
}
