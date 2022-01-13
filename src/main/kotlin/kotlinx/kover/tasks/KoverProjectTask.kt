/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.tasks

import kotlinx.kover.api.*
import kotlinx.kover.engines.commons.ProjectInfo
import kotlinx.kover.engines.commons.Report
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
    val srcDirs: Property<FileCollection> = project.objects.property(FileCollection::class.java)

    @get:Classpath
    val outputDirs: Property<FileCollection> = project.objects.property(FileCollection::class.java)

    @get:Input
    internal val coverageEngine: Property<CoverageEngine> = project.objects.property(CoverageEngine::class.java)

    @get:Classpath
    internal val classpath: Property<FileCollection> = project.objects.property(FileCollection::class.java)

    /**
     * Specifies class inclusion rules into report.
     * Only the specified classes may be present in the report.
     * Exclusion rules have priority over inclusion ones.
     *
     * Inclusion rules are represented as a set of fully-qualified names of the classes being instrumented.
     * It's possible to use `*` and `?` wildcards.
     *
     * **Works only with IntelliJ Coverage Engine.**
     */
    @get:Input
    public var includes: List<String> = emptyList()

    /**
     * Specifies class exclusion rules into report.
     * The specified classes will definitely be missing from report.
     * Exclusion rules have priority over inclusion ones.
     *
     * Exclusion rules are represented as a set of fully-qualified names of the classes being instrumented.
     * It's possible to use `*` and `?` wildcards.
     *
     * **Works only with IntelliJ Coverage Engine.**
     */
    @get:Input
    public var excludes: List<String> = emptyList()

    internal fun report(): Report {
        return Report(binaryReportFiles.get().toList(), listOf(ProjectInfo(srcDirs.get(), outputDirs.get())))
    }
}
