/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":subproject2"))
    testImplementation(kotlin("test"))
}