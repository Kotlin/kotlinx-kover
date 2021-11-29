/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.tasks

import kotlinx.kover.api.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*

abstract class KoverCommonTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    val binaryReportFiles: Property<FileCollection> = project.objects.property(FileCollection::class.java)

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    val smapFiles: Property<FileCollection> = project.objects.property(FileCollection::class.java)

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    val srcDirs: Property<FileCollection> = project.objects.property(FileCollection::class.java)

    @get:Classpath
    val outputDirs: Property<FileCollection> = project.objects.property(FileCollection::class.java)

    @get:Input
    internal val coverageEngine: Property<CoverageEngine> = project.objects.property(CoverageEngine::class.java)

    @get:Classpath
    internal val classpath: Property<FileCollection> = project.objects.property(FileCollection::class.java)
}
