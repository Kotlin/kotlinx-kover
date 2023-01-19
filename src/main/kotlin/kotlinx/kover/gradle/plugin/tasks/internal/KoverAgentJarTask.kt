/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tasks.internal

import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.tools.*
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.support.*
import javax.inject.*

internal open class KoverAgentJarTask @Inject constructor(private val tool: CoverageTool) : DefaultTask() {
    private val archiveOperations: ArchiveOperations = project.serviceOf()

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    val agentClasspath: ConfigurableFileCollection = project.objects.fileCollection()

    @get:OutputFile
    val agentJarPath: RegularFileProperty = project.objects.fileProperty()

    @get:Nested
    val toolVariant: CoverageToolVariant = tool.variant

    @TaskAction
    fun find() {
        // TODO can jar file be cached?
        agentJarPath.get().asFile.writeText(
            tool.findJvmAgentJar(agentClasspath, archiveOperations).canonicalPath
        )
    }
}
