/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.services

import kotlinx.kover.gradle.plugin.commons.*
import org.gradle.api.*
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault
import java.io.*

internal enum class TestTaskOutcome {
    FAILED,
    NOT_EXECUTED,
    NO_SOURCE,
    SKIPPED,
    EXECUTED,
    UP_TO_DATE
}

/**
 * A task that writes a Kover artifact - named lists of sources directories, directories with class-files, binary reports.
 *
 * This artifact that will be shared between projects through dependencies for creating merged reports.
 */
@DisableCachingByDefault(because = "The task action is so quick that cache does not provide a benefit")
internal abstract class KoverArtifactGenerationTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val outputDirs: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val reports: ConfigurableFileCollection

    @get:Input
    abstract val testTaskOutcomes: MapProperty<String, TestTaskOutcome>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val additionalArtifacts: ConfigurableFileCollection

    @get:OutputFile
    abstract val artifactFile: RegularFileProperty

    private val rootDir: File = project.rootDir

    @get:Input
    internal val projectPath: String = project.path

    @TaskAction
    fun generate() {
        val validReports = testTaskOutcomes.get()
            .filterValues { it == TestTaskOutcome.EXECUTED || it == TestTaskOutcome.UP_TO_DATE }
            .keys
        val actualReports = reports.filter { it.name in validReports }.toSet()

        val mainContent = ArtifactContent(projectPath, sources.toSet(), outputDirs.toSet(), actualReports)
        val additional = additionalArtifacts.files.map { it.parseArtifactFile(rootDir) }
        mainContent.joinWith(additional).write(artifactFile.get().asFile, rootDir)
    }

}
