/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

import kotlinx.kover.gradle.aggregation.settings.dsl.*

plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
}

extensions.configure<KoverProjectExtension> {
    instrumentation.disabledForTestTasks.add("test")
}