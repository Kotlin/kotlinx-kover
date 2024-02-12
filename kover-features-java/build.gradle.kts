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
    java
    id("kover-publishing-conventions")
}

extensions.configure<Kover_publishing_conventions_gradle.KoverPublicationExtension> {
    description.set("Implementation of calling the main features of Kover via Java invokes")
    fatJar.set(true)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

repositories {
    mavenCentral()
}

tasks.processResources {
    filesMatching("**/kover.version") {
        filter {
            it.replace("\$version", project.version.toString())
        }
    }
}

dependencies {
    implementation(libs.intellij.reporter)
}
