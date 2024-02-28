/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers.instrumentation

import kotlinx.kover.gradle.plugin.appliers.KoverContext
import kotlinx.kover.gradle.plugin.commons.binReportPath
import kotlinx.kover.gradle.plugin.dsl.internal.KoverCurrentProjectVariantsConfigImpl
import kotlinx.kover.gradle.plugin.tools.*
import org.gradle.api.*
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.testing.Test
import org.gradle.process.*
import java.io.File

/**
 * Add online instrumentation to all JVM test tasks.
 */
internal fun TaskCollection<Test>.instrument(
    koverContext: KoverContext,
    koverDisabled: Provider<Boolean>,
    current: KoverCurrentProjectVariantsConfigImpl
) {
    configureEach {
        val binReportProvider =
            project.layout.buildDirectory.map { dir ->
                dir.file(binReportPath(name, koverContext.toolProvider.get().variant.vendor))
            }
        dependsOn(koverContext.findAgentJarTask)

        doFirst {
            // delete report so that when the data is re-measured, it is not appended to an already existing file
            // see https://github.com/Kotlin/kotlinx-kover/issues/489
            binReportProvider.get().asFile.delete()
        }

        // Always excludes android classes, see https://github.com/Kotlin/kotlinx-kover/issues/89
        val excludedClassesWithAndroid = current.instrumentation.excludedClasses.map { it +
                setOf(
                    // Always excludes android classes, see https://github.com/Kotlin/kotlinx-kover/issues/89
                    "android.*", "com.android.*",
                    // excludes JVM internal classes, in some cases, errors occur when trying to instrument these classes, for example, when using JaCoCo + Robolectric. There is also no point in instrumenting them in Kover.
                    "jdk.internal.*"
                )
        }

        val taskInstrumentationDisabled = koverDisabled.map {
            // disable task instrumentation if Kover disabled
            if (it) return@map true
            // disable task instrumentation if it disabled for all tasks
            if (current.instrumentation.disabledForAll.get()) return@map true
            // disable task instrumentation if it explicitly excluded by name
            if (name in current.instrumentation.disabledForTestTasks.get()) return@map true
            //
            return@map false
        }

        jvmArgumentProviders += JvmTestTaskArgumentProvider(
            temporaryDir,
            koverContext.toolProvider,
            koverContext.findAgentJarTask.map { it.agentJar.get().asFile },
            taskInstrumentationDisabled,
            excludedClassesWithAndroid,
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
    val instrumentationDisabled: Provider<Boolean>,

    @get:Input
    val excludedClasses: Provider<Set<String>>,

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
        return if (!instrumentationDisabled.get()) {
            toolProvider.get()
                .jvmAgentArgs(agentJar.get(), tempDir, reportProvider.get().asFile, excludedClasses.get())
                .toMutableList()
        } else {
            mutableListOf()
        }
    }
}
