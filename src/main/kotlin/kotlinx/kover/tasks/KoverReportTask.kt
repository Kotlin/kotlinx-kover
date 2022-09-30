/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.tasks

import kotlinx.kover.api.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.configurationcache.extensions.*
import org.gradle.process.*
import java.io.*

// TODO make internal in 0.7 version - for now it public to save access to deprecated fields to print deprecation message
public abstract class KoverReportTask : DefaultTask() {
    @get:Nested
    internal val files: MapProperty<String, ProjectFiles> = project.objects.mapProperty()

    @get:Nested
    internal val classFilter: Property<KoverClassFilter> = project.objects.property()

    @get:Nested
    internal val annotationFilter: Property<KoverAnnotationFilter> = project.objects.property()

    @get:Nested
    internal val engine: Property<EngineDetails> = project.objects.property()

    // exec operations to launch Java applications
    @get:Internal
    protected val exec: ExecOperations = project.serviceOf()

    @get:Internal
    protected val projectPath: String = project.path

    @Internal
    internal fun getReportFilters(): ReportFilters {
        val classFilterValue = classFilter.get()
        return ReportFilters(
            classFilterValue.includes.toSet(),
            classFilterValue.excludes.toSet(),
            annotationFilter.get().excludes.toSet()
        )
    }

}

open class ProjectFiles(
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val binaryReportFiles: FileCollection,

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val sources: FileCollection,

    @get:Classpath
    val outputs: FileCollection
)

internal class EngineDetails(
    @get:Nested val variant: CoverageEngineVariant,
    @get:Internal val jarFile: File,
    @get:Internal val classpath: FileCollection
)

internal data class ReportFilters(
    @get:Input
    val includesClasses: Set<String>,
    @get:Input
    val excludesClasses: Set<String>,
    @get:Input
    val excludesAnnotations: Set<String>
)








