/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.internal

import kotlinx.kover.gradle.plugin.commons.*
import org.gradle.api.*
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import java.io.*
import javax.inject.*

@CacheableTask
internal open class KoverArtifactGenerationTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val sources: ConfigurableFileCollection = project.objects.fileCollection()

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val outputs: ConfigurableFileCollection = project.objects.fileCollection()

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val reports: ConfigurableFileCollection = project.objects.fileCollection()


    @get:OutputFile
    val artifactFile: RegularFileProperty = project.objects.fileProperty()


    @TaskAction
    fun generate() {
        val sources = sources.joinToString("\n") { it.canonicalPath }
        val outputs = outputs.joinToString("\n") { it.canonicalPath }
        val reports = reports.joinToString("\n") { it.canonicalPath }

        artifactFile.get().asFile.writeText("$sources\n\n$outputs\n\n$reports")
    }
}
