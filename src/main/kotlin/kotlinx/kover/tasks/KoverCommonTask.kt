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
    val binaryReportFiles: Property<FileCollection> = project.objects.property(FileCollection::class.java)
        @InputFiles @PathSensitive(PathSensitivity.ABSOLUTE) get

    val srcDirs: Property<FileCollection> = project.objects.property(FileCollection::class.java)
        @InputFiles @PathSensitive(PathSensitivity.ABSOLUTE) get

    val outputDirs: Property<FileCollection> = project.objects.property(FileCollection::class.java)
        @Classpath get

    internal val coverageEngine: Property<CoverageEngine> = project.objects.property(CoverageEngine::class.java)
        @Input get

    internal val classpath: Property<FileCollection> =  project.objects.property(FileCollection::class.java)
        @Classpath get
}
