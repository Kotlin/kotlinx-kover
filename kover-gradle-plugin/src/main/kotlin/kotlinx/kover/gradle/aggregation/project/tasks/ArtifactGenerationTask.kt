/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.project.tasks

import kotlinx.kover.gradle.aggregation.commons.android.AndroidVariantInfo
import kotlinx.kover.gradle.aggregation.commons.artifacts.ArtifactSerializer
import kotlinx.kover.gradle.aggregation.commons.artifacts.CompilationInfo
import kotlinx.kover.gradle.aggregation.commons.artifacts.ProjectArtifactInfo
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.SetProperty
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

    @get:Nested
    abstract val android9Variants: SetProperty<AndroidVariantInfo>

    @get:Input
    internal val projectPath = project.path

    private val rootDir = project.rootDir

    @TaskAction
    internal fun generate() {
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()

        val variants = android9Variants.get()
        val actualCompilations =
            if (variants.isEmpty()) {
                compilations.get()
            } else {
                // make patch for AGP > 9.0.0 - take sources from the corresponding variant
                compilations.get().mapValues { (compilationName, info) ->
                    val variant = variants.firstOrNull { it.name == compilationName } ?: return@mapValues info
                    CompilationInfo(variant.sourceDirs, info.outputDirs)
                }
            }

        val projectInfo = ProjectArtifactInfo(projectPath, reportFiles.files, actualCompilations)

        file.bufferedWriter().use { writer ->
            ArtifactSerializer.serialize(writer, rootDir, projectInfo)
        }
    }
}
