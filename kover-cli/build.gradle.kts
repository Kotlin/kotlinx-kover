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
    implementation(project(":kover-features-jvm"))

    implementation(libs.args4j)

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

    from(
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    ) {
        exclude("OSGI-OPT/**")
        exclude("META-INF/**")
        exclude("LICENSE")
    }
}

repositories {
    mavenCentral()
}

tasks.register("releaseDocs") {
    val dirName = "cli"
    val description = "Kover Command Line Interface"
    val sourceDir = projectDir.resolve("docs")
    val resultDir = rootDir.resolve("docs/$dirName")
    val mainIndexFile = rootDir.resolve("docs/index.md")

    doLast {
        resultDir.mkdirs()
        sourceDir.copyRecursively(resultDir)
        mainIndexFile.appendText("- [$description]($dirName)\n")
    }
}
