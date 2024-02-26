/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.services

import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.tools.*
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.*
import org.gradle.api.file.*
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.support.*
import javax.inject.*

/**
 * Task to get online instrumentation agent jar file by specified coverage tool.
 *
 * The task is cached, so in general there should not be a performance issue on large projects.
 */
@CacheableTask
internal abstract class KoverAgentJarTask : DefaultTask() {
    // relative sensitivity for file collections which are not FileTree is a comparison by file name and its contents
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val agentClasspath: ConfigurableFileCollection

    @get:OutputFile
    abstract val agentJar: RegularFileProperty

    @get:Internal
    abstract val tool: Property<CoverageTool>

    @get:Input
    abstract val koverDisabled: Property<Boolean>

    @get:Nested
    val toolVariant: CoverageToolVariant
        get() = tool.get().variant

    @get:Inject
    protected abstract val archiveOperations: ArchiveOperations

    @TaskAction
    fun find() {
        if (!koverDisabled.get()) {
            val srcJar = tool.get().findJvmAgentJar(agentClasspath, archiveOperations)
            srcJar.copyTo(agentJar.get().asFile, true)
        } else {
            // if Kover is disabled - create mock jar file to avoid copying files
            agentJar.get().asFile.createNewFile()
        }

    }
}
