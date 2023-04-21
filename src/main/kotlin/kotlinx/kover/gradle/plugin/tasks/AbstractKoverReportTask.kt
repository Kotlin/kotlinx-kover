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

