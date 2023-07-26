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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
    kotlin("jvm") apply false
    alias(libs.plugins.kotlinx.dokka) apply false
    alias(libs.plugins.kotlinx.binaryCompatibilityValidator) apply false
}



// ====================
// Release preparation
// ====================
tasks.register("prepareRelease") {

    doLast {
        if (!project.hasProperty("releaseVersion")) {
            throw GradleException("Property 'releaseVersion' is required to run this task")
        }
        val releaseVersion = project.property("releaseVersion") as String
        val prevReleaseVersion = project.property("kover.release.version") as String

        val projectDir = layout.projectDirectory

        projectDir.file("gradle.properties").asFile.patchProperties(releaseVersion)
        projectDir.file("CHANGELOG.md").asFile.patchChangeLog(releaseVersion)

        projectDir.file("README.md").asFile.replaceInFile(prevReleaseVersion, releaseVersion)

        // replace versions in examples
        projectDir.dir("kover-gradle-plugin").dir("examples").patchExamples(releaseVersion, prevReleaseVersion)
        projectDir.dir("kover-offline-runtime").dir("examples").patchExamples(releaseVersion, prevReleaseVersion)

        // replace versions in docs
        projectDir.dir("docs").patchDocs(releaseVersion, prevReleaseVersion)
    }
}

fun Directory.patchExamples(releaseVersion: String, prevReleaseVersion: String) {
    asFileTree.matching {
        include("**/*gradle")
        include("**/*gradle.kts")
    }.files.forEach {
        it.replaceInFile(prevReleaseVersion, releaseVersion)
    }
}

fun Directory.patchDocs(releaseVersion: String, prevReleaseVersion: String) {
    asFileTree.files.forEach {
        it.replaceInFile(prevReleaseVersion, releaseVersion)
    }
}

fun File.patchChangeLog(releaseVersion: String) {
    val oldContent = readText()
    writer().use {
        it.appendLine("$releaseVersion / ${LocalDate.now().format(DateTimeFormatter.ISO_DATE)}")
        it.appendLine("===================")
        it.appendLine("TODO add changelog!")
        it.appendLine()
        it.append(oldContent)
    }
}

fun File.patchProperties(releaseVersion: String) {
    val oldLines = readLines()
    writer().use { writer ->
        oldLines.forEach { line ->
            when {
                line.startsWith("version=") -> writer.append("version=").appendLine(increaseSnapshotVersion(releaseVersion))
                line.startsWith("kover.release.version=") -> writer.append("kover.release.version=").appendLine(releaseVersion)
                else -> writer.appendLine(line)
            }
        }
    }
}

// modify version '1.2.3' to '1.2.4' and '1.2.3-Beta' to '1.2.3-SNAPSHOT'
fun increaseSnapshotVersion(releaseVersion: String): String {
    // remove postfix like '-Alpha'
    val correctedVersion = releaseVersion.substringBefore('-')
    if (correctedVersion != releaseVersion) {
        return "$correctedVersion-SNAPSHOT"
    }

    // split version 0.0.0 to int parts
    val parts = correctedVersion.split('.')
    val newVersion = parts.mapIndexed { index, value ->
        if (index == parts.size - 1) {
            (value.toInt() + 1).toString()
        } else {
            value
        }
    }.joinToString(".")

    return "$newVersion-SNAPSHOT"
}

fun File.replaceInFile(old: String, new: String) {
    val newContent = readText().replace(old, new)
    writeText(newContent)
}

