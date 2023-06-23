/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import kotlinx.kover.gradle.plugin.commons.binReportPath
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.tools.*
import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.testing.Test
import org.gradle.process.*
import java.io.File

/**
 * Add online instrumentation to all JVM test tasks.
 */
internal fun TaskCollection<Test>.instrument(data: InstrumentationData) {
    configureEach {
        JvmTestTaskConfigurator(this, data).apply()
    }
}

/**
 * Gradle Plugin applier of the specific JVM test task.
 */
internal class JvmTestTaskConfigurator(
    private val testTask: Test,
    private val data: InstrumentationData
) {
    fun apply() {
        val binReportProvider =
            testTask.project.layout.buildDirectory.map { dir ->
                dir.file(binReportPath(testTask.name, data.toolProvider.get().variant.vendor))
            }
        testTask.dependsOn(data.findAgentJarTask)

        // Always excludes android classes, see https://github.com/Kotlin/kotlinx-kover/issues/89
        val excluded = data.excludedClasses + listOf("android.*", "com.android.*")

        testTask.jvmArgumentProviders += JvmTestTaskArgumentProvider(
            testTask.temporaryDir,
            data.toolProvider,
            data.findAgentJarTask.map { it.agentJar.get().asFile },
            excluded,
            binReportProvider
        )
    }
}

/**
 * Provider of additional JVM string arguments for running a test task.
 */
private class JvmTestTaskArgumentProvider(
    private val tempDir: File,
    private val toolProvider: Provider<CoverageTool>,

    // relative sensitivity for file is a comparison by file name and its contents
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val agentJar: Provider<File>,

    @get:Input
    val excludedClasses: Set<String>,

    @get:OutputFile
    val reportProvider: Provider<RegularFile>
) : CommandLineArgumentProvider, Named {

    @get:Nested
    val toolVariant: CoverageToolVariant
        get() = toolProvider.get().variant

    @Internal
    override fun getName(): String {
        return "koverArgumentsProvider"
    }

    override fun asArguments(): MutableIterable<String> {
        return toolProvider.get().jvmAgentArgs(agentJar.get(), tempDir, reportProvider.get().asFile, excludedClasses)
            .toMutableList()
    }
}
