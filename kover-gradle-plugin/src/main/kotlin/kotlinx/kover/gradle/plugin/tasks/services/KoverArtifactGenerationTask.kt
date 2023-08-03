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
import org.gradle.kotlin.dsl.*
import java.io.*
import javax.inject.*

/**
 * A task that writes a Kover artifact - named lists of sources directories, directories with class-files, binary reports.
 *
 * This artifact that will be shared between projects through dependencies for creating merged reports.
 */
@CacheableTask
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

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val additionalArtifacts: ConfigurableFileCollection

    @get:OutputFile
    abstract val artifactFile: RegularFileProperty

    private val rootDir: File = project.rootDir

    @TaskAction
    fun generate() {
        val mainContent = ArtifactContent(sources.toSet(), outputDirs.toSet(), reports.toSet())
        val additional = additionalArtifacts.files.map { it.parseArtifactFile(rootDir) }
        mainContent.joinWith(additional).write(artifactFile.get().asFile, rootDir)
    }
}
