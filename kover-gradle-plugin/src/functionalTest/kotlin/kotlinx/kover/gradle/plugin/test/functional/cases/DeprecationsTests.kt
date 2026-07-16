/*
 * Copyright 2017-2026 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.runner.BuildOptions
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.buildFromTemplate
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runWithParams
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DeprecationsTests {

    @Test
    fun testProjectInstanceAsDependency() {
        val buildSource = buildFromTemplate("counters")

        val theBuild = buildSource.generate()
        val buildOptions = BuildOptions(
            gradleVersion = "9.6.1",
            )

        val result = theBuild.runWithParams(listOf(":koverXmlReport", "--warning-mode",  "all"), buildOptions)
        assertTrue(result.isSuccessful, "Build should be successful")
        assertFalse(result.output.contains("Using a Project object as a dependency notation has been deprecated"), "Project instance should not be used as a dependency")
    }

    @Test
    fun testProjectInstanceAsDependencyAggregation() {
        val buildSource = buildFromTemplate("settings-plugin")

        val theBuild = buildSource.generate()
        val buildOptions = BuildOptions(
            gradleVersion = "9.6.1",
            )

        val result = theBuild.runWithParams(listOf("koverXmlReport", "-Pkover", "--warning-mode",  "all"), buildOptions)
        assertTrue(result.isSuccessful, "Build should be successful")
        assertFalse(result.output.contains("Using a Project object as a dependency notation has been deprecated"), "Project instance should not be used as a dependency")
    }

}