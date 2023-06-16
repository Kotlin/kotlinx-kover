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
        assertEquals("SUCCESS", result.taskOutcome(":koverXmlReport"))
        assertEquals("SUCCESS", result.taskOutcome(":koverHtmlReport"))
        assertEquals("SUCCESS", result.taskOutcome(":koverVerify"))
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



                tasks.koverXmlReportName mustBe "koverXmlReport"
                tasks.koverXmlReportName mustBe KoverNames.DEFAULT_XML_REPORT_NAME

                tasks.koverHtmlReportName mustBe "koverHtmlReport"
                tasks.koverHtmlReportName mustBe KoverNames.DEFAULT_HTML_REPORT_NAME

                tasks.koverVerifyName mustBe "koverVerify"
                tasks.koverVerifyName mustBe KoverNames.DEFAULT_VERIFY_REPORT_NAME



                tasks.koverAndroidXmlReportName("variant") mustBe "koverXmlReportVariant"
                tasks.koverAndroidXmlReportName("variant") mustBe KoverNames.androidXmlReport("variant")

                tasks.koverAndroidHtmlReportName("variant") mustBe "koverHtmlReportVariant"
                tasks.koverAndroidHtmlReportName("variant") mustBe KoverNames.androidHtmlReport("variant")

                tasks.koverAndroidVerifyName("variant") mustBe "koverVerifyVariant"
                tasks.koverAndroidVerifyName("variant") mustBe KoverNames.androidVerify("variant")



                extensions.koverExtensionName mustBe "kover"
                extensions.koverExtensionName mustBe KoverNames.PROJECT_EXTENSION_NAME

                extensions.koverReportExtensionName mustBe "koverReport"
                extensions.koverReportExtensionName mustBe KoverNames.REPORT_EXTENSION_NAME

                infix fun String.mustBe(a: String) {
                    if (this != a) throw AssertionError("Expected " + a + ", actual " + this)
                }
            """.trimIndent()
            )
        }.generate("Test names accessors", "custom")

        val result = build.runWithParams("tasks")

        assertTrue(result.isSuccessful)
    }
}