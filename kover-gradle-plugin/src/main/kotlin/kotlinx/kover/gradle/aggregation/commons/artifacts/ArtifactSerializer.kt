/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.commons.artifacts

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import java.io.File
import java.io.Reader

internal object ArtifactSerializer {
    fun serialize(appender: Appendable, rootDir: File, info: ProjectArtifactInfo) {
        appender.appendLine("PROJECT=${info.path}")
        appender.appendLine()
        info.reports.forEach { report ->
            appender.appendLine( "REPORT=${report.toRelativeString(rootDir)}")
        }
        info.compilations.forEach { (name, info) ->
            appender.appendLine("[COMPILATION]")
            appender.appendLine("NAME=$name")
            info.sourceDirs.forEach { sourceDir ->
                appender.appendLine("SOURCE=${sourceDir.toRelativeString(rootDir)}")
            }
            info.outputDirs.forEach { outputDir ->
                appender.appendLine("OUTPUT=${outputDir.toRelativeString(rootDir)}")
            }
            appender.appendLine("[END]")
        }
    }

    fun deserialize(reader: Reader, rootDir: File): ProjectArtifactInfo {
        class Comp(
            var name: String? = null,
            val sourceDirs: MutableSet<File> = mutableSetOf(),
            val outputDirs: MutableSet<File> = mutableSetOf()
        )

        var projectPath: String? = null
        val reports: MutableSet<File> = mutableSetOf()
        val all: MutableList<Comp> = mutableListOf()

        var current: Comp? = null

        reader.forEachLine { line ->
            when {
                line.startsWith("PROJECT=") -> {
                    projectPath = line.substringAfter("PROJECT=")
                }

                line.startsWith("REPORT=") -> {
                    reports.add(rootDir.resolve(line.substringAfter("REPORT=")))
                }

                line.startsWith("[COMPILATION]") -> { current = Comp() }
                line.startsWith("NAME=") -> {
                    current?.name = line.substringAfter("NAME=")
                }

                line.startsWith("SOURCE=") -> {
                    current?.sourceDirs?.add(rootDir.resolve(line.substringAfter("SOURCE=")))
                }
                line.startsWith("OUTPUT=") -> {
                    current?.outputDirs?.add(rootDir.resolve(line.substringAfter("OUTPUT=")))
                }
                line.startsWith("[END]") -> {
                    all += current!!
                    current = null
                }
            }
        }

        val map = all.associate { it.name!! to CompilationInfo(it.sourceDirs.toList(), it.outputDirs.toSet()) }
        return ProjectArtifactInfo(projectPath!!, reports, map)
    }
}

internal class ProjectArtifactInfo(
    @get:Input
    val path: String,

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val reports: Collection<File>,

    @get:Nested
    val compilations: Map<String, CompilationInfo>
)

internal class CompilationInfo(
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val sourceDirs: Collection<File>,

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val outputDirs: Collection<File>
)