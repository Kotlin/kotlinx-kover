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

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.intellij.deps:intellij-coverage-offline:1.0.721")
}

tasks.jar {
    from(
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    ) {
        exclude("OSGI-OPT/**")
        exclude("META-INF/**")
        exclude("LICENSE")
    }

}
