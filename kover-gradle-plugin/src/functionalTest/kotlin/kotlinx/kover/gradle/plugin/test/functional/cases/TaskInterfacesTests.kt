/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.generateBuild
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runWithParams
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.TemplateTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TaskInterfacesTests {

    @Test
    fun testDefaultHtmlDir() {
        val build = generateBuild { dir ->
            dir.resolve("settings.gradle.kts").createNewFile()

            dir.resolve("build.gradle.kts").writeText(
                """
                import kotlinx.kover.gradle.plugin.dsl.tasks.KoverHtmlReport
                
                plugins {
                    kotlin("jvm") version ("2.2.0")
                    id("org.jetbrains.kotlinx.kover")
                }
                
                tasks.register("checkDir") {
                    doFirst {
                        val task = tasks.withType<KoverHtmlReport>().matching { it.name == "koverHtmlReport" }.first()
                        if (task.reportDir.get().asFile != layout.buildDirectory.dir("reports/kover/html").get().asFile) {
                            throw Exception("Default directory differ, expect ${'$'}{layout.buildDirectory.dir("reports/kover/html").get().asFile} actual ${'$'}{task.reportDir.get().asFile}")
                        }
                    }
                }
            """.trimIndent()
            )
        }.generate()

        val result = build.runWithParams("checkDir")
        assertTrue(result.isSuccessful)
    }

    @Test
    fun testCustomHtmlDir() {
        val build = generateBuild { dir ->
            dir.resolve("settings.gradle.kts").createNewFile()

            dir.resolve("build.gradle.kts").writeText(
                """
                import kotlinx.kover.gradle.plugin.dsl.tasks.KoverHtmlReport
                
                plugins {
                    kotlin("jvm") version ("2.2.0")
                    id("org.jetbrains.kotlinx.kover")
                }
                
                kover {
                    reports {
                        total {
                            html {
                                htmlDir.set(layout.buildDirectory.dir("customHtmlReport"))
                            }
                        }
                    }
                }
                
                tasks.register("checkDir") {
                    doFirst {
                        val task = tasks.withType<KoverHtmlReport>().matching { it.name == "koverHtmlReport" }.first()
                        if (task.reportDir.get().asFile != layout.buildDirectory.dir("customHtmlReport").get().asFile) {
                            throw Exception("Custom directory differ, expect ${'$'}{layout.buildDirectory.dir("customHtmlReport").get().asFile} actual ${'$'}{task.reportDir.get().asFile}")
                        }
                    }
                }
            """.trimIndent()
            )
        }.generate()

        val result = build.runWithParams("checkDir")
        assertTrue(result.isSuccessful)
    }

    @TemplateTest("android-test-tasks-filtering", [":app:findAllTasks"])
    fun CheckerContext.testTasksSearch() {
        taskOutput(":app:findTotalTasks") {
            assertEquals(
                """
                    XML=koverXmlReport
                    HTML=koverHtmlReport
                    Verify=koverVerify
                    Log=koverLog
                    Binary=koverBinaryReport
                    
                    """.trimIndent()
                , this
            )
        }

        taskOutput(":app:findDebugTasks") {
            assertEquals(
                """
                    XML=koverXmlReportDebug
                    HTML=koverHtmlReportDebug
                    Verify=koverVerifyDebug
                    Log=koverLogDebug
                    Binary=koverBinaryReportDebug
                    
                    """.trimIndent(),
                this
            )
        }
    }
}
