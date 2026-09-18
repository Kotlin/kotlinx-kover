/*
 * Copyright 2017-2026 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.runner.buildFromTemplate
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.BuildOptions
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runWithParams
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

internal class DuplicateDependencyTests {

    @Test
    fun testActual() {
        val build = buildFromTemplate("dependency-duplicate").generate()
        val result = build.runWithParams(":koverXmlReport")

        assertFalse(result.isSuccessful)
        assertContains(
            result.output,
            "Kover cannot resolve project dependencies with duplicate component identities: " +
                    "'org.jetbrains:common' is used by ':a:common', ':b:common'"
        )
    }

    @Test
    fun testGradleBefore8dot11() {
        val source = buildFromTemplate("dependency-duplicate")
        source.overriddenKotlinVersion = "1.7.22"
        val build = source.generate()
        val result = build.runWithParams(":koverXmlReport", options = BuildOptions(gradleVersion = "6.8"))

        assertFalse(result.isSuccessful)
        assertContains(
            result.output,
            "Kover cannot resolve project dependencies with duplicate component identities: " +
                    "'org.jetbrains:common' is used by ':a:common', ':b:common'"
        )
    }
}
