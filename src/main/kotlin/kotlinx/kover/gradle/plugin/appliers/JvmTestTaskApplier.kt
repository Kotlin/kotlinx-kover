/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import kotlinx.kover.gradle.plugin.commons.rawReportPath
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.tasks.internal.KoverAgentJarTask
import kotlinx.kover.gradle.plugin.tools.*
import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.testing.Test
import org.gradle.process.*
import java.io.File


internal class JvmTestTaskApplier(
    private val testTask: Test,
    private val agentFindTask: Provider<KoverAgentJarTask>,
    private val agentJar: Provider<File>,
    private val tool: CoverageTool,
    private val excludedClasses: Set<String>
) {
    fun apply() {
        val rawReportProvider =
            testTask.project.layout.buildDirectory.file(rawReportPath(testTask.name, tool.variant.vendor))
        testTask.dependsOn(agentFindTask)
        testTask.jvmArgumentProviders += JvmTestTaskArgumentProvider(
            testTask.temporaryDir,
            tool,
            agentJar,
            excludedClasses,
            rawReportProvider
        )
    }
}


private class JvmTestTaskArgumentProvider(
    private val tempDir: File,
    private val tool: CoverageTool,

    @get:InputFile
    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    val agentJar: Provider<File>,

    @get:Input
    val excludedClasses: Set<String>,

    @get:OutputFile
    val reportProvider: Provider<RegularFile>
) : CommandLineArgumentProvider, Named {

    @get:Nested
    val toolVariant: CoverageToolVariant = tool.variant

    @Internal
    override fun getName(): String {
        return "koverArgumentsProvider"
    }

    override fun asArguments(): MutableIterable<String> {
        return tool.jvmAgentArgs(agentJar.get(), tempDir, reportProvider.get().asFile, excludedClasses).toMutableList()
    }
}
