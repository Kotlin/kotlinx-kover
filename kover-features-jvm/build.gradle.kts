import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    alias(libs.plugins.kotlinx.binaryCompatibilityValidator)
    id("kover-publishing-conventions")
    id("kover-release-conventions")
}

koverPublication {
    description = "Implementation of calling the main features of Kover programmatically"
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

// Workaround:
// `kotlin-dsl` itself specifies the language version to ensure compatibility of the Kotlin DSL API
// Since we ourselves guarantee and test compatibility with previous Gradle versions, we can override language version
// The easiest way to do this now is to specify the version in the `afterEvaluate` block
afterEvaluate {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            allWarningsAsErrors = true
            jvmTarget = JvmTarget.JVM_1_8
            languageVersion = KotlinVersion.KOTLIN_1_5
            apiVersion = KotlinVersion.KOTLIN_1_5
            freeCompilerArgs.addAll("-Xsuppress-version-warnings")
        }
    }
}

repositories {
    mavenCentral()
}

tasks.processResources {
    val file = destinationDir.resolve("kover.version")

    doLast {
        file.writeText(project.version.toString())
    }
}

dependencies {
    implementation(libs.intellij.reporter)
}
