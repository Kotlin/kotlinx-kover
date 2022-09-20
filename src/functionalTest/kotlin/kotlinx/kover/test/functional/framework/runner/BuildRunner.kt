/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.runner

import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.checker.*
import kotlinx.kover.test.functional.framework.configurator.*
import org.gradle.testkit.runner.*
import java.io.*

internal fun File.runAndCheck(runs: List<TestRunConfig>) {
    logInfo("""
Project builds are being started, directory ${this.absolutePath}
======== START SCRIPT ========
${this.buildScript()}
======== END SCRIPT ========
    """.trimIndent())

    runs.forEachIndexed { i, run ->
        val description = "run #$i '${run.args.joinToString(" ")}'\nProject dir: ${this.uri}"

        val runResult = try {
            this.runGradleBuild(run.args)
        } catch (e: UnexpectedBuildFailure) {
            if (run.errorExpected) {
                return@forEachIndexed
            } else {
                throw AssertionError("Build error in for $description\n\n${e.buildResult.output}")
            }
        }

        try {
            this.checkResult(runResult, description, run.errorExpected, run.checker)
        } catch (e: Throwable) {
            if (run.errorExpected) {
                return@forEachIndexed
            } else {
                throw AssertionError(e.message + "\n For $description\n\n${runResult.output}", e)
            }
        }
        if (run.errorExpected) {
            throw AssertionError("Error expected for $description")
        }
    }
}

internal fun File.runGradleBuild(vararg args: String): BuildResult = runGradleBuild(args.toList())

internal fun File.runGradleBuild(args: List<String>): BuildResult {
    return GradleRunner.create()
        .withProjectDir(this)
        .withPluginClasspath()
        .addPluginTestRuntimeClasspath()
        .withArguments(args)
        .build()
}

private fun GradleRunner.addPluginTestRuntimeClasspath() = apply {
    val pluginClasspath = pluginClasspath + additionalPluginClasspath
    withPluginClasspath(pluginClasspath)
}

private fun File.buildScript(): String {
    var file = File(this, "build.gradle")
    if (file.exists() && file.isFile) return file.readText()

    file = File(this, "build.gradle.kts")
    if (file.exists() && file.isFile) return file.readText()

    return ""
}
