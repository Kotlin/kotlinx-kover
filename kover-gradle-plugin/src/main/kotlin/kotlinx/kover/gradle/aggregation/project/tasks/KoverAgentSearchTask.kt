/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.project.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault

/**
 * Task to get online instrumentation agent jar file by specified coverage tool.
 *
 * The task is cached, so in general there should not be a performance issue on large projects.
 */
@DisableCachingByDefault(because = "This task only copies one file")
internal abstract class KoverAgentSearchTask : DefaultTask() {
    // relative sensitivity for file collections which are not FileTree is a comparison by file name and its contents
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val agentClasspath: ConfigurableFileCollection

    @get:OutputFile
    abstract val agentJar: RegularFileProperty

    @TaskAction
    fun find() {
        val srcJar = agentClasspath.filter { it.name.startsWith("kover-jvm-agent") }.files.firstOrNull()
            ?: throw GradleException("JVM instrumentation agent not found for Kover Coverage Tool")

        srcJar.copyTo(agentJar.get().asFile, true)
    }
}
