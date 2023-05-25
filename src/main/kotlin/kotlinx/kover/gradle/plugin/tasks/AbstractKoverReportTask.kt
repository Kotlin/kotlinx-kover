/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks

import kotlinx.kover.api.*
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.tools.*
import kotlinx.kover.gradle.plugin.tools.kover.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.configurationcache.extensions.*
import org.gradle.kotlin.dsl.*
import org.gradle.process.*
import java.io.File


internal abstract class AbstractKoverReportTask(@Internal protected val tool: CoverageTool) : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val localArtifact: RegularFileProperty = project.objects.fileProperty()

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val externalArtifacts: ConfigurableFileCollection = project.objects.fileCollection()

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val reportClasspath: ConfigurableFileCollection = project.objects.fileCollection()

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
    val toolVariant: CoverageToolVariant = tool.variant

    @get:Nested
    val filters: Property<ReportFilters> = project.objects.property()

    @get:Internal
    protected val projectPath: String = project.path

    private val exec: ExecOperations = project.serviceOf()

    private val obj = project.objects

    fun hasRawReportsAndLog(): Boolean {
        val hasReports = collectAllFiles().reports.isNotEmpty()
        if (!hasReports) {
            logger.lifecycle("Task '$name' will be skipped because no tests were executed")
        }
        return hasReports
    }

    protected fun context(): ReportContext {
        val services = GradleReportServices(exec, ant, obj)
        return ReportContext(collectAllFiles(), reportClasspath, temporaryDir, projectPath, services)
    }

    private fun collectAllFiles(): ArtifactContent {
        val local = localArtifact.get().asFile.parseArtifactFile()
        return local.joinWith(externalArtifacts.files.map { it.parseArtifactFile() })
    }
}

