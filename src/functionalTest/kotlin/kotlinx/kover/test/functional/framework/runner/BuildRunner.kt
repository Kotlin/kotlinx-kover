/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.runner

import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.checker.*
import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.starter.*
import java.io.*

internal fun File.runAndCheck(runs: List<TestRunConfig>) {
    logInfo(
        """
Project builds are being started, directory ${this.absolutePath}
======== START SCRIPT ========
${this.buildScript()}
======== END SCRIPT ========
    """.trimIndent()
    )

    runs.forEachIndexed { i, run ->
        val description = "run #$i, commands: '${run.args.joinToString(" ")}'\nProject dir: ${this.uri}"

        val runResult = this.runGradleBuild(run.args, i)

        createCheckerContext(runResult)
            .check(description, run.errorExpected, run.checker)
    }
}

private fun File.buildScript(): String {
    var file = this.resolve("build.gradle")
    if (file.exists() && file.isFile) return file.readText()

    file = this.resolve("build.gradle.kts")
    if (file.exists() && file.isFile) return file.readText()

    return ""
}
