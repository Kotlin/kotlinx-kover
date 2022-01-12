/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.tasks

import kotlinx.kover.api.*
import kotlinx.kover.engines.commons.*
import kotlinx.kover.engines.commons.Report
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.model.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import java.io.*

@CacheableTask
open class KoverAggregateTask : DefaultTask() {
    @get:Nested
    val binaryReportFiles: MapProperty<String, NestedFiles> =
        project.objects.mapProperty(String::class.java, NestedFiles::class.java)

    @get:Nested
    val srcDirs: MapProperty<String, NestedFiles> =
        project.objects.mapProperty(String::class.java, NestedFiles::class.java)

    @get:Nested
    val outputDirs: MapProperty<String, NestedFiles> =
        project.objects.mapProperty(String::class.java, NestedFiles::class.java)

    @get:Input
    internal val coverageEngine: Property<CoverageEngine> = project.objects.property(CoverageEngine::class.java)

    @get:Classpath
    internal val classpath: Property<FileCollection> = project.objects.property(FileCollection::class.java)


    internal fun report(): Report {
        val binariesMap = binaryReportFiles.get()
        val sourcesMap = srcDirs.get()
        val outputsMap = outputDirs.get()

        val projectsNames = sourcesMap.keys

        val reportFiles: MutableList<File> = mutableListOf()
        val projects: MutableList<ProjectInfo> = mutableListOf()

        projectsNames.map { projectName ->
            reportFiles += binariesMap.getValue(projectName).files.get()
            projects += ProjectInfo(
                sources = sourcesMap.getValue(projectName).files.get(),
                outputs = outputsMap.getValue(projectName).files.get()
            )
        }

        return Report(reportFiles, projects)
    }
}


class NestedFiles(objects: ObjectFactory, files: Provider<FileCollection>) {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val files: Property<FileCollection> = objects.property(FileCollection::class.java).also { it.set(files) }
}
