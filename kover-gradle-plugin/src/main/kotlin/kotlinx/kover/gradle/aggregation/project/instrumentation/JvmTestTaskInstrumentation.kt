/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.project.instrumentation

import kotlinx.kover.gradle.aggregation.commons.names.KoverPaths
import kotlinx.kover.gradle.aggregation.settings.dsl.ProjectInstrumentationSettings
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
        instrumentation: ProjectInstrumentationSettings
    ) {
        tasks.configureEach {
            val taskName = name
            val enabledProvider = instrumentation.disabledForTestTasks.map { taskName !in it }
            val included = instrumentation.includedClasses
            val excluded = instrumentation.excludedClasses

            val binReportProvider =
                project.layout.buildDirectory.map { dir ->
                    dir.file(KoverPaths.binReportPath(name))
                }

            doFirst {
                // delete report so that when the data is re-measured, it is not appended to an already existing file
                // see https://github.com/Kotlin/kotlinx-kover/issues/489
                binReportProvider.get().asFile.delete()
            }

            // Excludes Android SDK stub classes by default to prevent instrumentation errors,
            // see https://github.com/Kotlin/kotlinx-kover/issues/89.
            // Set the Gradle property 'kover.android.excludes.disable' to opt out of the
            // android.* / com.android.* exclusions (e.g. for AOSP system apps).
            val androidExcludesDisabled = project.providers
                .gradleProperty("kover.android.excludes.disable")
                .map { true }
                .orElse(false)

            val excludedWithAndroid = excluded.zip(androidExcludesDisabled) { excludedSet, disableAndroid ->
                val defaultExclusions = if (disableAndroid) {
                    setOf("jdk.internal.*")
                } else {
                    setOf(
                        "android.*", "com.android.*",
                        // Excluded to prevent errors when instrumenting JVM internals (e.g. JaCoCo + Robolectric).
                        "jdk.internal.*"
                    )
                }
                excludedSet + defaultExclusions
            }

            dependsOn(jarConfiguration)
            jvmArgumentProviders += JvmTestTaskArgumentProvider(
                temporaryDir,
                project.objects.fileCollection().from(jarConfiguration),
                enabledProvider,
                included,
                excludedWithAndroid,
                binReportProvider
            )
        }
    }
}

/**
 * Provider of additional JVM string arguments for running a test task.
 */
private class JvmTestTaskArgumentProvider(
    private val tempDir: File,

    // relative sensitivity for file is a comparison by file name and its contents
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val jarFiles: ConfigurableFileCollection,

    @get:Input
    val enabled: Provider<Boolean>,
    @get:Input
    val includedClasses: Provider<Set<String>>,
    @get:Input
    val excludedClasses: Provider<Set<String>>,

    @get:OutputFile
    val reportProvider: Provider<RegularFile>
) : CommandLineArgumentProvider, Named {

    @Internal
    override fun getName(): String {
        return "koverArgumentsProvider"
    }

    override fun asArguments(): MutableIterable<String> {
        val files = jarFiles.files
        if (!enabled.get() || files.size != 1) {
            return mutableSetOf()
        }
        val jarFile = files.single()

        return buildKoverJvmAgentArgs(
            jarFile,
            tempDir,
            reportProvider.get().asFile,
            includedClasses.orNull ?: emptySet(),
            excludedClasses.orNull ?: emptySet()
        ).toMutableList()
    }
}


private fun buildKoverJvmAgentArgs(
    jarFile: File,
    tempDir: File,
    binReportFile: File,
    includedClasses: Set<String>,
    excludedClasses: Set<String>
): List<String> {
    val argsFile = tempDir.resolve("kover-agent.args")
    argsFile.writeAgentArgs(binReportFile, includedClasses, excludedClasses)

    return mutableListOf("-javaagent:${jarFile.canonicalPath}=file:${argsFile.canonicalPath}")
}

private fun File.writeAgentArgs(binReportFile: File, includedClasses: Set<String>, excludedClasses: Set<String>) {
    parentFile.mkdirs()
    val binReportPath = binReportFile.canonicalPath

    printWriter().use { pw ->
        pw.append("report.file=").appendLine(binReportPath)
        excludedClasses.forEach { e ->
            pw.append("exclude=").appendLine(e)
        }
        includedClasses.forEach { i ->
            pw.append("include=").appendLine(i)
        }
    }
}