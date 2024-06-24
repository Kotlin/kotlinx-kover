/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.project.tasks

import kotlinx.kover.gradle.aggregation.commons.artifacts.ArtifactSerializer
import kotlinx.kover.gradle.aggregation.commons.artifacts.CompilationInfo
import kotlinx.kover.gradle.aggregation.commons.artifacts.ProjectArtifactInfo
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "There are no heavy computations")
internal abstract class ArtifactGenerationTask: DefaultTask() {

    @get:OutputFile
    internal abstract val outputFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val reportFiles: ConfigurableFileCollection

    @get:Nested
    abstract val compilations: MapProperty<String, CompilationInfo>

    @get:Input
    internal val projectPath = project.path

    private val rootDir = project.rootDir

    @TaskAction
    internal fun generate() {
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()

        val projectInfo = ProjectArtifactInfo(projectPath, reportFiles.files, compilations.get())

        file.bufferedWriter().use { writer ->
            ArtifactSerializer.serialize(writer, rootDir, projectInfo)
        }
    }
}
