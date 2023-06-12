/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.reports

import kotlinx.kover.api.*
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.tools.*
import kotlinx.kover.gradle.plugin.tools.kover.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.configurationcache.extensions.*
import org.gradle.kotlin.dsl.*
import org.gradle.process.*
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject


internal abstract class AbstractKoverReportTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val localArtifact: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val externalArtifacts: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val reportClasspath: ConfigurableFileCollection

    /**
     * This will cause the task to be considered out-of-date when source files of dependencies have changed.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal val externalSources: Provider<Set<File>> = externalArtifacts.elements.map {
        val content = ArtifactContent(emptySet(), emptySet(), emptySet())
        content.joinWith(it.map { file -> file.asFile.parseArtifactFile() }).sources
    }

    /**
     * This will cause the task to be considered out-of-date when coverage measurements of dependencies have changed.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal val externalReports: Provider<Set<File>> = externalArtifacts.elements.map {
        val content = ArtifactContent(emptySet(), emptySet(), emptySet())
        content.joinWith(it.map { file -> file.asFile.parseArtifactFile() }).reports
    }

    /**
     * This will cause the task to be considered out-of-date when source files of this project have changed.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal val localSources: Provider<Set<File>> = localArtifact.map {
        it.asFile.parseArtifactFile().sources
    }

    /**
     * This will cause the task to be considered out-of-date when coverage measurements of this project have changed.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal val localReports: Provider<Set<File>> = localArtifact.map {
        it.asFile.parseArtifactFile().reports
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

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    protected fun context(): ReportContext {
        val services = GradleReportServices(workerExecutor, ant, obj)
        return ReportContext(collectAllFiles(), filters.get(), reportClasspath, temporaryDir, projectPath, services)
    }

    private fun collectAllFiles(): ArtifactContent {
        val local = localArtifact.get().asFile.parseArtifactFile()
        return local.joinWith(externalArtifacts.files.map { it.parseArtifactFile() })
    }
}

