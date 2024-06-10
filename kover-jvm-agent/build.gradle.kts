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
    id("kover-fat-jar-conventions")
    id("kover-docs-conventions")
    id("kover-release-conventions")
}

extensions.configure<Kover_publishing_conventions_gradle.KoverPublicationExtension> {
    description.set("Kover JVM instrumentation agent")
}

extensions.configure<Kover_docs_conventions_gradle.KoverDocsExtension> {
    docsDirectory.set("jvm-agent")
    description.set("Kover JVM instrumentation agent")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_6
    targetCompatibility = JavaVersion.VERSION_1_6
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.intellij.agent)
    fatJar(libs.intellij.agent)
}

val targetVersion = if (project.hasProperty("releaseVersion")) {
    project.property("releaseVersion").toString()
} else {
    project.version.toString()
}

tasks.jar {
    manifest {
        attributes(
            "Premain-Class" to "kotlinx.kover.jvmagent.KoverJvmAgentPremain",
            "Can-Retransform-Classes" to "true",
            // We need to pass this parameter, because IntelliJ agent collects data in the bootstrap class loader
            // it is not possible to use other loaders because some of them (for example, FilteredClassLoader) restrict access to agent classes
            "Boot-Class-Path" to "${project.name}-$targetVersion.jar"
        )
    }
}
