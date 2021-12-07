/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.intellij

import kotlinx.kover.api.*
import org.gradle.api.*
import org.gradle.api.artifacts.*
import java.io.*


internal fun Project.createIntellijAgent(koverExtension: KoverExtension): IntellijAgent {
    val intellijConfig = createIntellijConfig(koverExtension)
    return IntellijAgent(intellijConfig)
}

internal class IntellijAgent(val config: Configuration) {
    private val trackingPerTest = false // a flag to enable tracking per test coverage
    private val calculateForUnloadedClasses = true // a flag to calculate coverage for unloaded classes
    private val appendToDataFile = false // a flag to use data file as initial coverage
    private val samplingMode = false //a flag to run coverage in sampling mode or in tracing mode otherwise
    private val generateSmapFile = true

    fun buildCommandLineArgs(extension: KoverTaskExtension, task: Task): MutableList<String> {
        val argsFile = File(task.temporaryDir, "intellijagent.args")
        argsFile.writeArgsToFile(extension)
        val jarFile = config.fileCollection { it.name == "intellij-coverage-agent" }.singleFile
        return mutableListOf(
            "-javaagent:${jarFile.canonicalPath}=${argsFile.canonicalPath}",
            "-Didea.coverage.check.inline.signatures=true",
            "-Didea.new.sampling.coverage=true",
            "-Didea.new.tracing.coverage=true",
            "-Didea.coverage.log.level=error",
            "-Dcoverage.ignore.private.constructor.util.class=true"
        )
    }

    private fun File.writeArgsToFile(extension: KoverTaskExtension) {
        val binary = extension.binaryReportFile.get()
        binary.parentFile.mkdirs()
        val binaryPath = binary.canonicalPath

        val smap = extension.smapFile.get()
        smap.parentFile.mkdirs()
        val smapPath = smap.canonicalPath

        printWriter().use { pw ->
            pw.appendLine(binaryPath)
            pw.appendLine(trackingPerTest.toString())
            pw.appendLine(calculateForUnloadedClasses.toString())
            pw.appendLine(appendToDataFile.toString())
            pw.appendLine(samplingMode.toString())
            pw.appendLine(generateSmapFile.toString())
            pw.appendLine(smapPath)
            extension.includes.forEach { i ->
                pw.appendLine(i.replaceWildcards())
            }

            if (extension.excludes.isNotEmpty()) {
                pw.appendLine("-exclude")
            }

            extension.excludes.forEach { e ->
                pw.appendLine(e.replaceWildcards())
            }
        }
    }

    private fun String.replaceWildcards(): String {
        // in most cases, the characters `*` or `.` will be present therefore, we increase the capacity in advance
        val builder = StringBuilder(length * 2)

        forEach { char ->
            when (char) {
                in regexMetacharactersSet -> builder.append('\\').append(char)
                '*' -> builder.append('.').append("*")
                '?' -> builder.append('.')
                else -> builder.append(char)
            }
        }

        return builder.toString()
    }
}

private val regexMetacharactersSet = "<([{\\^-=$!|]})+.>".toSet()

private fun Project.createIntellijConfig(koverExtension: KoverExtension): Configuration {
    val config = project.configurations.create("IntellijKoverConfig")
    config.isVisible = false
    config.isTransitive = true
    config.description = "Kotlin Kover Plugin configuration for IntelliJ agent and reporter"

    config.defaultDependencies { dependencies ->
        val agentVersion = koverExtension.intellijEngineVersion.get()
        IntellijEngineVersion.parseOrNull(agentVersion)?.let {
            if (it < minimalIntellijVersion) throw GradleException("IntelliJ engine version $it is too low, minimal version is $minimalIntellijVersion")
        }

        dependencies.add(
            this.dependencies.create("org.jetbrains.intellij.deps:intellij-coverage-agent:$agentVersion")
        )

        dependencies.add(
            this.dependencies.create("org.jetbrains.intellij.deps:intellij-coverage-reporter:$agentVersion")
        )
    }
    return config
}
