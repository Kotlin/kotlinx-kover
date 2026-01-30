/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings.tasks

import kotlinx.kover.gradle.aggregation.commons.artifacts.ProjectArtifactInfoDeserialized
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested

internal abstract class AbstractKoverReportTask : AbstractKoverTask() {
    @get:Nested
    abstract val filters: Property<FiltersInput>

    @get:Nested
    val data: Provider<Map<String, ProjectArtifactInfoDeserialized>> = artifacts.elements.map { elements ->
        elements.data().mapValues { entry ->
            entry.value.filterProjectSources(filters.get())
        }
    }

    @get:Internal
    protected val reports get() = data.get().values.flatMap { artifact -> artifact.reports }

    @get:Internal
    protected val sources get() =
        data.get().values.flatMap { artifact -> artifact.compilations.flatMap { compilation -> compilation.value.sourceDirs } }

    @get:Internal
    protected val outputs get() =
        data.get().values.flatMap { artifact -> artifact.compilations.flatMap { compilation -> compilation.value.outputDirs } }

}