/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.reports

import kotlinx.kover.features.jvm.KoverFeatures
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.tools.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import java.io.File
import javax.inject.Inject


internal abstract class AbstractKoverReportTask : DefaultTask() {
    @get:Internal
    abstract val variantName: String

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val artifacts: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val reportClasspath: ConfigurableFileCollection

    /**
     * This will cause the task to be considered out-of-date when source files of dependencies have changed.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal val sources: Provider<Set<File>> = artifacts.elements.map {
        val content = ArtifactContent.Empty
        content.joinWith(it.map { file -> file.asFile.parseArtifactFile(rootDir).filterProjectSources() }).sources
    }

    /**
     * This will cause the task to be considered out-of-date when coverage measurements of dependencies have changed.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal val reports: Provider<Set<File>> = artifacts.elements.map {
        val content = ArtifactContent.Empty
        content.joinWith(it.map { file -> file.asFile.parseArtifactFile(rootDir).filterProjectSources() }).reports
    }

    @get:Nested
    val toolVariant: CoverageToolVariant
        get() = tool.get().variant

    @get:Nested
    val filters: Property<ReportFilters> = project.objects.property()

    @get:Internal
    abstract val tool: Property<CoverageTool>

    @get:Internal
    protected val projectPath: String = project.path

    @get:Inject
    protected abstract val obj: ObjectFactory

    private val rootDir: File = project.rootDir

    protected fun context(): ReportContext {
        val services = GradleReportServices(ant, obj)
        return ReportContext(collectAllFiles(), filters.get(), reportClasspath, temporaryDir, projectPath, services)
    }

    private fun collectAllFiles(): ArtifactContent {
        val local = ArtifactContent(projectPath, emptySet(), emptySet(), emptySet())
        return local.joinWith(artifacts.files.map { it.parseArtifactFile(rootDir).filterProjectSources() }).existing()
    }

    private fun ArtifactContent.filterProjectSources(): ArtifactContent {
        val reportFilters = filters.get()
        if (reportFilters.includeProjects.isNotEmpty()) {
            val notIncluded = reportFilters.includeProjects.none { filter ->
                KoverFeatures.koverWildcardToRegex(filter).toRegex().matches(path)
            }
            if (notIncluded) {
                return ArtifactContent(path, emptySet(), emptySet(), reports)
            }
        }
        if (reportFilters.excludeProjects.isNotEmpty()) {
            val excluded = reportFilters.excludeProjects.any { filter ->
                KoverFeatures.koverWildcardToRegex(filter).toRegex().matches(path)
            }
            if (excluded) {
                return ArtifactContent(path, emptySet(), emptySet(), reports)
            }
        }
        return this
    }
}

