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
}

extensions.configure<Kover_publishing_conventions_gradle.KoverPublicationExtension> {
    description.set("Command Line Interface for Kotlin Coverage Toolchain")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    compileOnly(kotlin("stdlib"))
    fatJar(kotlin("stdlib"))

    fatJar(project(":kover-features-jvm"))
    compileOnly(project(":kover-features-jvm"))
    testImplementation(project(":kover-features-jvm"))

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

extensions.configure<Kover_docs_conventions_gradle.KoverDocsExtension> {
    docsDirectory.set("cli")
    description.set("Kover Command Line Interface")
}
