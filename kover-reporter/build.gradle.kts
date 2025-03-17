/*
 * Copyright 2000-2024 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")

    `kotlin-dsl`
    `java-gradle-plugin`

    id("kover-publishing-conventions")
    id("kover-docs-conventions")
    id("kover-release-conventions")
}

koverPublication {
    description = "Kover reporter"
}

koverDocs {
    docsDirectory = "reporter"
    description = "Kover reporter"
}


dependencies {
    implementation("org.jetbrains.kotlin:kotlin-metadata-jvm:2.0.21")
    implementation("org.ow2.asm:asm:9.7.1")
    implementation("org.ow2.asm:asm-tree:9.7.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.0")
    implementation("com.charleskorn.kaml:kaml:0.72.0")

    compileOnly(libs.gradlePlugin.kotlin)
}

repositories {
    mavenCentral()
}

gradlePlugin {
    website = "https://github.com/Kotlin/kotlinx-kover"
    vcsUrl = "https://github.com/Kotlin/kotlinx-kover.git"

    plugins {
        create("Kover") {
            id = "org.jetbrains.kotlinx.kover.reporter"
            implementationClass = "kotlinx.kover.reporter.KoverReporterPlugin"
            displayName = "Kover Reporter prototype"
            description = "prototype"
            tags.addAll("kover", "kotlin", "coverage", "reporter")
        }
    }
}
