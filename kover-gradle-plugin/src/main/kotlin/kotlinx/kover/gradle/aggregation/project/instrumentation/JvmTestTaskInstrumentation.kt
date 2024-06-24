/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.project.instrumentation

import kotlinx.kover.gradle.aggregation.commons.names.KoverPaths
import org.gradle.api.Named
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.api.tasks.testing.Test
import org.gradle.process.CommandLineArgumentProvider
import java.io.File


internal object JvmOnFlyInstrumenter {

    /**
     * Add online instrumentation to all JVM test tasks.
     */
    fun instrument(
        tasks: TaskCollection<Test>,
        jarConfiguration: Configuration,
        filter: InstrumentationFilter
    ) {
        tasks.configureEach {
            val binReportProvider =
                project.layout.buildDirectory.map { dir ->
                    dir.file(KoverPaths.binReportPath(name))
                }

            doFirst {
                // delete report so that when the data is re-measured, it is not appended to an already existing file
                // see https://github.com/Kotlin/kotlinx-kover/issues/489
                binReportProvider.get().asFile.delete()
            }

            // Always excludes android classes, see https://github.com/Kotlin/kotlinx-kover/issues/89
            val androidClasses = setOf(
                // Always excludes android classes, see https://github.com/Kotlin/kotlinx-kover/issues/89
                "android.*", "com.android.*",
                // excludes JVM internal classes, in some cases, errors occur when trying to instrument these classes, for example, when using JaCoCo + Robolectric. There is also no point in instrumenting them in Kover.
                "jdk.internal.*"
            )

            val excludedClassesWithAndroid = filter.copy(excludes = filter.excludes + androidClasses)

            dependsOn(jarConfiguration)
            jvmArgumentProviders += JvmTestTaskArgumentProvider(
                temporaryDir,
                project.objects.fileCollection().from(jarConfiguration),
                excludedClassesWithAndroid,
                binReportProvider
            )
        }
    }
}

internal data class InstrumentationFilter(
    @get:Input
    val includes: Set<String>,
    @get:Input
    val excludes: Set<String>
)

/**
 * Provider of additional JVM string arguments for running a test task.
 */
private class JvmTestTaskArgumentProvider(
    private val tempDir: File,

    // relative sensitivity for file is a comparison by file name and its contents
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val jarFiles: ConfigurableFileCollection,

    @get:Nested
    val filter: InstrumentationFilter,

    @get:OutputFile
    val reportProvider: Provider<RegularFile>
) : CommandLineArgumentProvider, Named {

    @Internal
    override fun getName(): String {
        return "koverArgumentsProvider"
    }

    override fun asArguments(): MutableIterable<String> {
        val files = jarFiles.files
        if (files.size != 1) {
            return mutableSetOf()
        }
        val jarFile = files.single()

        return buildKoverJvmAgentArgs(jarFile, tempDir, reportProvider.get().asFile, filter.excludes)
            .toMutableList()
    }
}


private fun buildKoverJvmAgentArgs(
    jarFile: File,
    tempDir: File,
    binReportFile: File,
    excludedClasses: Set<String>
): List<String> {
    val argsFile = tempDir.resolve("kover-agent.args")
    argsFile.writeAgentArgs(binReportFile, excludedClasses)

    return mutableListOf("-javaagent:${jarFile.canonicalPath}=file:${argsFile.canonicalPath}")
}

private fun File.writeAgentArgs(binReportFile: File, excludedClasses: Set<String>) {
    binReportFile.parentFile.mkdirs()
    val binReportPath = binReportFile.canonicalPath

    printWriter().use { pw ->
        pw.append("report.file=").appendLine(binReportPath)
        excludedClasses.forEach { e ->
            pw.append("exclude=").appendLine(e)
        }
    }
}