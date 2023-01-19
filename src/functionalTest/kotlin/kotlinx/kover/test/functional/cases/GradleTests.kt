/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.framework.runner.*
import kotlinx.kover.test.functional.framework.starter.SimpleTest
import java.io.*
import kotlin.test.*

class GradleTests {

    /**
     * An example to show how you can design your own builds in the test function, run and check them.
     */
    @SimpleTest
    fun File.test() {
        resolve("build.gradle.kts").writeText("""""")
        resolve("settings.gradle.kts").writeText("""rootProject.name = "empty-project"""")

        val result = runGradleBuild(listOf("tasks"))
        val buildLog = result.output

        assertTrue(buildLog.contains("BUILD SUCCESSFUL"))
    }
}
