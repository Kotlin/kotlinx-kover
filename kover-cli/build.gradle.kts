import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/*
 * Copyright 2000-2023 JetBrains s.r.o.
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
    id("kover-publishing-conventions")
    id("kover-docs-conventions")
    id("kover-fat-jar-conventions")
    id("kover-release-conventions")
}

koverPublication {
    description = "Command Line Interface for Kotlin Coverage Toolchain"
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

dependencies {
    compileOnly(kotlin("stdlib"))
    fatJar(kotlin("stdlib"))

    fatJar(projects.koverFeaturesJvm)
    compileOnly(projects.koverFeaturesJvm)
    testImplementation(projects.koverFeaturesJvm)

    fatJar(libs.args4j)
    compileOnly(libs.args4j)
    testImplementation(libs.args4j)

    testImplementation(kotlin("test"))
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "kotlinx.kover.cli.MainKt")
    }
}

repositories {
    mavenCentral()
}

koverDocs {
    docsDirectory = "cli"
    description = "Kover Command Line Interface"
}
