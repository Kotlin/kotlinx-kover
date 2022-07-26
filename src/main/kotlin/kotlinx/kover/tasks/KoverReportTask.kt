/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.tasks

import kotlinx.kover.api.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.configurationcache.extensions.*
import org.gradle.process.*
import java.io.*

// TODO make internal in 0.7 version - for now it public to save access to deprecated fields to print deprecation message
public abstract class KoverReportTask : DefaultTask() {
    @get:Nested
    internal val files: MapProperty<String, ProjectFiles> =
        project.objects.mapProperty(String::class.java, ProjectFiles::class.java)

    @get:Nested
    internal val classFilter: Property<KoverClassFilter> = project.objects.property(KoverClassFilter::class.java)

    @get:Nested
    internal val engine: Property<EngineDetails> = project.objects.property(EngineDetails::class.java)

    // exec operations to launch Java applications
    @get:Internal
    protected val exec: ExecOperations = project.serviceOf()

    @get:Internal
    protected val projectPath: String = project.path

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











