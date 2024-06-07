/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

        if (project.path == Project.PATH_SEPARATOR) {
            projectDir.file("gradle.properties").asFile.patchProperties(releaseVersion)
            projectDir.file("CHANGELOG.md").asFile.patchChangeLog(releaseVersion)

            projectDir.file("README.md").asFile.replaceInFile(prevReleaseVersion, releaseVersion)
        } else {
            // replace versions in examples
            projectDir.dir("examples").patchExamples(releaseVersion, prevReleaseVersion)

            // replace versions in docs
            projectDir.dir("docs").patchDocs(releaseVersion, prevReleaseVersion)
        }


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
                line.startsWith("version=") -> writer.append("version=")
                    .appendLine(increaseSnapshotVersion(releaseVersion))

                line.startsWith("kover.release.version=") -> writer.append("kover.release.version=")
                    .appendLine(releaseVersion)

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
    if (name.endsWith(".png")) return

    val newContent = readText().replace(old, new)
    writeText(newContent)
}

