/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings.tasks

import kotlinx.kover.features.jvm.KoverFeatures
import kotlinx.kover.gradle.aggregation.commons.artifacts.ArtifactSerializer
import kotlinx.kover.gradle.aggregation.commons.artifacts.ProjectArtifactInfoDeserialized
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import java.io.File

internal abstract class AbstractKoverTask : DefaultTask() {
    // relative sensitivity for file collections which are not FileTree is a comparison by file name and its contents
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val artifacts: ConfigurableFileCollection

    @get:Internal
    protected val rootDir: File = project.rootDir

    @get:Internal
    protected val projectPath: String = project.path

    protected fun Set<FileSystemLocation>.data(): Map<String, ProjectArtifactInfoDeserialized> {
        return map { location -> location.asFile }
            .map { file -> ArtifactSerializer.deserialize(file.bufferedReader(), rootDir) }
            .associateBy { it.path }
    }

    protected fun ProjectArtifactInfoDeserialized.filterProjectSources(filters: FiltersInput): ProjectArtifactInfoDeserialized {
        val included = filters.includedProjects
        val excluded = filters.excludedProjects

        if (included.isNotEmpty()) {
            val notIncluded = included.none { filter ->
                KoverFeatures.koverWildcardToRegex(filter).toRegex().matches(path)
            }
            if (notIncluded) {
                return ProjectArtifactInfoDeserialized(path, reports, emptyMap())
            }
        }

        if (excluded.isNotEmpty()) {
            val excl = excluded.any { filter ->
                KoverFeatures.koverWildcardToRegex(filter).toRegex().matches(path)
            }
            if (excl) {
                return ProjectArtifactInfoDeserialized(path, reports, emptyMap())
            }
        }
        return this
    }
}