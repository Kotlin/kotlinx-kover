/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.tasks

import kotlinx.kover.api.*
import kotlinx.kover.engines.commons.ProjectInfo
import kotlinx.kover.engines.commons.Report
import kotlinx.kover.engines.commons.ReportFiles
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*

abstract class KoverProjectTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val binaryReportFiles: Property<FileCollection> = project.objects.property(FileCollection::class.java)

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val smapFiles: Property<FileCollection> = project.objects.property(FileCollection::class.java)

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val srcDirs: Property<FileCollection> = project.objects.property(FileCollection::class.java)

    @get:Classpath
    val outputDirs: Property<FileCollection> = project.objects.property(FileCollection::class.java)

    @get:Input
    internal val coverageEngine: Property<CoverageEngine> = project.objects.property(CoverageEngine::class.java)

    @get:Classpath
    internal val classpath: Property<FileCollection> = project.objects.property(FileCollection::class.java)

    internal fun report(): Report {
        val binaries = binaryReportFiles.get()
        val smapFiles = smapFiles.get().iterator()

        val reportFiles = if (coverageEngine.get() == CoverageEngine.INTELLIJ) {
            binaries.map { binary ->
                ReportFiles(binary, smapFiles.next())
            }
        } else {
            binaries.map { binary ->
                ReportFiles(binary)
            }
        }

        return Report(reportFiles, listOf(ProjectInfo(srcDirs.get(), outputDirs.get())))
    }
}
