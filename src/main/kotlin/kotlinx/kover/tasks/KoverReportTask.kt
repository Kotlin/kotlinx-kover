/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.tasks

import kotlinx.kover.api.CoverageEngineVariant
import kotlinx.kover.api.KoverClassFilter
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

// TODO make internal in 0.7 version - for now it public to save access to deprecated fields to print deprecation message
public abstract class KoverReportTask @Inject constructor(
    private val objects: ObjectFactory,
) : DefaultTask() {

    @get:Nested
//    internal val files: MapProperty<String, ProjectFiles> = objects.mapProperty()
    internal val files: ProjectFiles = objects.newInstance()

    @get:Nested
    internal val classFilter: Property<KoverClassFilter> = objects.property()

    @get:Nested
    internal val engine: Property<EngineDetails> = objects.property()

//    @get:Internal
//    protected val projectPath: String = path
}

abstract class ProjectFiles {
    // TODO re-enable incremental paths options
    @get:InputFiles
//    @get:PathSensitive(PathSensitivity.RELATIVE)
//    @get:SkipWhenEmpty
    abstract val binaryReportFiles: ConfigurableFileCollection

    @get:InputFiles
//    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:Classpath
    abstract val outputs: ConfigurableFileCollection
}


internal class EngineDetails(
    @get:Nested val variant: CoverageEngineVariant,
    @get:Internal val jarFile: File,
    @get:Internal val classpath: FileCollection
)
