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
    id("kover-docs-conventions")
    id("kover-fat-jar-conventions")
    id("kover-release-conventions")
}

koverPublication {
    description.set("Compiled dependency to ensure the operation of the code that has been instrumented offline")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.intellij.offline)
    fatJar(libs.intellij.offline)
}

koverDocs {
    docsDirectory.set("offline-instrumentation")
    description.set("Kover offline instrumentation")
    callDokkaHtml.set(true)
}
