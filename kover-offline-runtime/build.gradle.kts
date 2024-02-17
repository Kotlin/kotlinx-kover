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
    java
    id("kover-publishing-conventions")
}

extensions.configure<Kover_publishing_conventions_gradle.KoverPublicationExtension> {
    description.set("Compiled dependency to ensure the operation of the code that has been instrumented offline")
    fatJar.set(true)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

repositories {
    mavenCentral()
}

val fatJarDependency = "fatJar"
val fatJarConfiguration = configurations.create(fatJarDependency)

dependencies {
    compileOnly(libs.intellij.offline)
    fatJarConfiguration(libs.intellij.offline)
}

tasks.jar {
    from(
        fatJarConfiguration.map { if (it.isDirectory) it else zipTree(it) }
    ) {
        exclude("OSGI-OPT/**")
        exclude("META-INF/**")
        exclude("LICENSE")
        exclude("classpath.index")
    }
}

tasks.register("releaseDocs") {
    val dirName = "offline-instrumentation"
    val description = "Kover offline instrumentation"
    val sourceDir = projectDir.resolve("docs")
    val resultDir = rootDir.resolve("docs/$dirName")
    val mainIndexFile = rootDir.resolve("docs/index.md")

    doLast {
        resultDir.mkdirs()
        sourceDir.copyRecursively(resultDir)
        mainIndexFile.appendText("- [$description]($dirName)\n")
    }
}
