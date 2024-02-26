/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.runner.generateBuild
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runWithParams
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AccessorsTests {
    @Test
    fun testDefaultTasks() {
        val build = generateBuild { dir ->
            dir.resolve("settings.gradle.kts").createNewFile()

            dir.resolve("build.gradle.kts").writeText(
                """
                plugins {
                    kotlin("jvm") version "1.8.0"
                    id("org.jetbrains.kotlinx.kover")
                }

                tasks.register("custom") {
                    dependsOn(tasks.koverHtmlReport)
                    dependsOn(tasks.koverXmlReport)
                    dependsOn(tasks.koverBinaryReport)
                    dependsOn(tasks.koverVerify)
                    dependsOn(tasks.koverLog)
                }
            """.trimIndent()
            )
        }.generate()

        val result = build.runWithParams("custom")

        // skipped because there is no tests, but tasks are triggered
        assertEquals("SUCCESS", result.taskOutcome(":koverXmlReport"))
        assertEquals("SUCCESS", result.taskOutcome(":koverHtmlReport"))
        assertEquals("SUCCESS", result.taskOutcome(":koverBinaryReport"))
        assertEquals("SUCCESS", result.taskOutcome(":koverVerify"))
        assertEquals("SUCCESS", result.taskOutcome(":koverLog"))
    }

    @Test
    fun testNames() {
        val build = generateBuild { dir ->
            dir.resolve("settings.gradle.kts").createNewFile()

            dir.resolve("build.gradle.kts").writeText(
                """
                import kotlinx.kover.gradle.plugin.dsl.*

                plugins {
                    id("org.jetbrains.kotlinx.kover")
                }


                KoverNames.pluginId mustBe "org.jetbrains.kotlinx.kover"
                KoverNames.jvmVariantName mustBe "jvm"
                KoverNames.configurationName mustBe "kover"
                KoverNames.extensionName mustBe "kover"
               

                KoverNames.koverXmlReportName mustBe "koverXmlReport"
                KoverNames.koverHtmlReportName mustBe "koverHtmlReport"
                KoverNames.koverBinaryReportName mustBe "koverBinaryReport"
                KoverNames.koverVerifyName mustBe "koverVerify"
                KoverNames.koverLogName mustBe "koverLog"

                KoverNames.koverXmlReportName("variant") mustBe "koverXmlReportVariant"
                KoverNames.koverHtmlReportName("variant") mustBe "koverHtmlReportVariant"
                KoverNames.koverBinaryReportName("variant") mustBe "koverBinaryReportVariant"
                KoverNames.koverVerifyName("variant") mustBe "koverVerifyVariant"
                KoverNames.koverLogName("variant") mustBe "koverLogVariant"

                infix fun String.mustBe(a: String) {
                    if (this != a) throw AssertionError("Expected " + a + ", actual " + this)
                }
            """.trimIndent()
            )
        }.generate()

        val result = build.runWithParams("tasks")

        assertTrue(result.isSuccessful)
    }
}