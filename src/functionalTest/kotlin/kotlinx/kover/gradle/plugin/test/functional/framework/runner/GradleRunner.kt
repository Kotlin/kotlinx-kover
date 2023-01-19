/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.runner

import kotlinx.kover.gradle.plugin.test.functional.framework.common.*
import kotlinx.kover.gradle.plugin.test.functional.framework.common.isDebugEnabled
import java.io.*

private val wrappersRoot = File("gradle-wrappers")
private val defaultWrapperDir = File(".")


/**
 * Options:
 *  - build cache - disabled by default, to enable it, you need to pass the "--build-cache" argument.
 *  - Gradle daemon - is not used by default, a new process is started for each test, which stops when the run is finished.
 */
internal fun File.runGradleBuild(args: List<String>, runIndex: Int = 0): BuildResult {
    val gradleArgs: MutableList<String> = mutableListOf()
    gradleArgs += args
    if (args.none { it == "--build-cache" }) gradleArgs += "--no-build-cache"

    if (isDebugEnabled) {
        gradleArgs += "-Dorg.gradle.debug=true"
        gradleArgs += "--no-daemon"
    }

    val wrapperDir = if (gradleWrapperVersion == null) defaultWrapperDir else getWrapper(gradleWrapperVersion)

    logInfo("Run Gradle commands $gradleArgs for project '${this.canonicalPath}' with wrapper '${wrapperDir.canonicalPath}'")

    val env: MutableMap<String, String> = mutableMapOf()
    androidSdkDir?.also { env[ANDROID_HOME_ENV] = it }

    return buildGradleByShell(runIndex, wrapperDir, gradleArgs, env)
}


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
}

private fun getWrapper(version: String): File {
    val wrapperDir = wrappersRoot.resolve(version)
    if (!wrapperDir.exists()) throw Exception("Wrapper for Gradle version '$version' is not supported by functional tests")
    return wrapperDir
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
