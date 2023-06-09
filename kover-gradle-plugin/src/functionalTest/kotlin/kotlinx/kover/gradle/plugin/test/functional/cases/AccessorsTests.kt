/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.runner.generateBuild
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runWithParams
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class AccessorsTests {
    @Test
    fun testDefaultAccessors() {
        val build = generateBuild { dir ->
            dir.resolve("settings.gradle.kts").createNewFile()

            dir.resolve("build.gradle.kts").writeText(
                """
                plugins {
                    kotlin("jvm") version "1.8.0"
                    id("org.jetbrains.kotlinx.kover") version "DEV"
                }


                group = "org.jetbrains"
                version = "1.0-SNAPSHOT"

                repositories {
                    mavenCentral()
                }

                tasks.register("custom") {
                    dependsOn(tasks.koverHtmlReport)
                    dependsOn(tasks.koverXmlReport)
                    dependsOn(tasks.koverVerify)
                }
            """.trimIndent()
            )
        }.generate("Test accessors", "custom")

        val result = build.runWithParams("custom")

        // skipped because there is no tests, but tasks are triggered
        assertEquals("SKIPPED", result.taskOutcome(":koverXmlReport"))
        assertEquals("SKIPPED", result.taskOutcome(":koverHtmlReport"))
        assertEquals("SKIPPED", result.taskOutcome(":koverVerify"))
    }
}