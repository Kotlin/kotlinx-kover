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

    private fun collectAllFiles(): BuildFiles {
        val local = localArtifact.get().asFile.parseArtifact()
        return local.joinWith(externalArtifacts.files.map { it.parseArtifact() })
    }
}


private fun BuildFiles.joinWith(others: List<BuildFiles>): BuildFiles {
    val sources = this.sources.toMutableSet()
    val outputs = this.outputs.toMutableSet()
    val reports = this.reports.toMutableSet()

    others.forEach {
        sources += it.sources
        outputs += it.outputs
        reports += it.reports
    }

    return BuildFiles(sources, outputs, reports)
}

private fun File.parseArtifact(): BuildFiles {
    if (!exists()) return BuildFiles(emptySet(), emptySet(), emptySet())

    val iterator = readLines().iterator()

    val sources = iterator.groupUntil { it.isEmpty() }.map { File(it) }.filter { it.exists() }.toSet()
    val outputs = iterator.groupUntil { it.isEmpty() }.map { File(it) }.filter { it.exists() }.toSet()
    val reports = iterator.groupUntil { it.isEmpty() }.map { File(it) }.filter { it.exists() }.toSet()

    return BuildFiles(sources, outputs, reports)
}

private fun <T> Iterator<T>.groupUntil(block: (T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    while (hasNext()) {
        val next = next()
        if (block(next)) break
        result += next
    }
    return result
}
