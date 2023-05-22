/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.runner

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.*
import kotlinx.kover.gradle.plugin.test.functional.framework.common.*
import kotlinx.kover.gradle.plugin.test.functional.framework.common.logInfo
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import java.io.*

internal fun File.runAndCheck(steps: List<TestExecutionStep>) {
    logInfo(
        """
Project builds are being started, directory ${this.absolutePath}
======== START SCRIPT ========
${this.buildScript()}
======== END SCRIPT ========
    """.trimIndent()
    )

    steps.forEachIndexed { i, step ->
        val description = "step #$i, ${step.name}\nProject dir: ${this.uri}"

        when(step) {
            is TestGradleStep -> {
                val runResult = this.runGradleBuild(step.args, i)
                createCheckerContext(runResult)
                    .check(description, step.errorExpected, step.checker)
            }
            is TestFileEditStep -> {
                if (File(step.filePath).isAbsolute) {
                    throw Exception("It is not allowed to edit a file by an absolute path. For $description")
                }

                val file = this.resolve(step.filePath)
                if (!file.exists()) {
                    throw Exception("Project file not found for editing. For $description")
                }

                val content = file.readText()
                val newContent = step.editor(content)
                file.writeText(newContent)
            }
        }


    }
}

private fun File.buildScript(): String {
    var file = this.resolve("build.gradle")
    if (file.exists() && file.isFile) return file.readText()

    file = this.resolve("build.gradle.kts")
    if (file.exists() && file.isFile) return file.readText()

    return ""
}
