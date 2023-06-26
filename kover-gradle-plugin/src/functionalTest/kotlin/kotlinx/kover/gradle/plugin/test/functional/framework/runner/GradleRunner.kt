/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.runner

import java.io.*


internal class BuildResult(exitCode: Int, private val logFile: File) {
    val isSuccessful: Boolean = exitCode == 0

    val output: String by lazy { logFile.readText() }

    fun taskOutcome(path: String): String? {
        val successLine = "> Task $path"
        val prefix = "$successLine "

        output.lineSequence().forEach {
            if (it == successLine) return "SUCCESS"
            if (it.startsWith(prefix)) return it.substringAfter(prefix).ifEmpty { "SUCCESS" }
        }

        return null
    }

    fun taskLog(path: String): String? {
        val prefix = "> Task $path"

        val result = mutableListOf<String>()
        val lines = output.lines()

        var index = 0
        while (index < lines.size) {
            val line = lines[index]
            index++
            if (line.startsWith(prefix)) {
                break
            }
        }
        while (index < lines.size) {
            val line = lines[index]
            index++
            if (line.startsWith("> Task") || line.startsWith("BUILD SUCCESSFUL in")) {
                break
            }
            result += line
        }

        return if (result.isEmpty()) null else result.joinToString("\n")
    }
}


internal fun File.buildGradleByShell(
    runIndex: Int,
    gradleWrapperDir: File,
    gradleCommands: List<String>,
    env: Map<String, String>
): BuildResult {
    val logFile = this.resolve("build-$runIndex.log")

    val commands = buildSystemCommand(this, gradleCommands)

    val builder = ProcessBuilder(commands)
    if (env.isNotEmpty()) {
        builder.environment().putAll(env)
    }
    builder.directory(gradleWrapperDir)
    // redirectErrorStream merges stdout and stderr, so it can be get from process.inputStream
    builder.redirectErrorStream(true)
    builder.redirectOutput(logFile)
    val process = builder.start()
    val exitCode = process.waitFor()
    return BuildResult(exitCode, logFile)
}

private fun buildSystemCommand(projectDir: File, commands: List<String>): List<String> {
    return if (isWindows)
        listOf("cmd", "/C", "gradlew.bat", "-p", projectDir.canonicalPath) + commands
    else
        listOf("/bin/bash", "gradlew", "-p", projectDir.canonicalPath) + commands
}

private val isWindows: Boolean = System.getProperty("os.name")!!.contains("Windows")
