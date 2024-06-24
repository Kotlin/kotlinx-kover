/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings.tasks

import kotlinx.kover.features.jvm.KoverFeatures
import kotlinx.kover.gradle.aggregation.commons.artifacts.ArtifactSerializer
import kotlinx.kover.gradle.aggregation.commons.artifacts.ProjectArtifactInfo
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*

internal abstract class AbstractKoverTask : DefaultTask() {
    // relative sensitivity for file collections which are not FileTree is a comparison by file name and its contents
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val artifacts: ConfigurableFileCollection

    @get:Input
    abstract val includedProjects: SetProperty<String>
    @get:Input
    abstract val excludedProjects: SetProperty<String>
    @get:Input
    abstract val includedClasses: SetProperty<String>
    @get:Input
    abstract val excludedClasses: SetProperty<String>

    @get:Nested
    val data: Provider<Map<String, ProjectArtifactInfo>> = artifacts.elements.map { elements ->
        elements.map { location -> location.asFile }
            .map { file -> ArtifactSerializer.deserialize(file.bufferedReader(), rootDir) }
            .map(::filterProjectSources)
            .associateBy { it.path }
    }

    @get:Internal
    protected val rootDir = project.rootDir

    @get:Internal
    protected val reports get() = data.get().values.flatMap { artifact -> artifact.reports }

    @get:Internal
    protected val sources get() =
        data.get().values.flatMap { artifact -> artifact.compilations.flatMap { compilation -> compilation.value.sourceDirs } }

    @get:Internal
    protected val outputs get() =
        data.get().values.flatMap { artifact -> artifact.compilations.flatMap { compilation -> compilation.value.outputDirs } }

    private fun filterProjectSources(info: ProjectArtifactInfo): ProjectArtifactInfo {
        val included = includedProjects.get()
        val excluded = excludedProjects.get()

        if (included.isNotEmpty()) {
            val notIncluded = included.none { filter ->
                KoverFeatures.koverWildcardToRegex(filter).toRegex().matches(info.path)
            }
            if (notIncluded) {
                return ProjectArtifactInfo(info.path, info.reports, emptyMap())
            }
        }

        if (excluded.isNotEmpty()) {
            val excl = excluded.any { filter ->
                KoverFeatures.koverWildcardToRegex(filter).toRegex().matches(info.path)
            }
            if (excl) {
                return ProjectArtifactInfo(info.path, info.reports, emptyMap())
            }
        }
        return info
    }
}