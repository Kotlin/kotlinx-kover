package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.*
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class MultiProjectTests : BaseGradleScriptTest() {
    private val subprojectName = "common"
    private val rootName = "kover-functional-test"


    @Test
    fun testMergedReports() {
        val build = diverseBuild(engines = ALL_ENGINES, types = ALL_TYPES)
        val subPath = build.addKoverSubproject(subprojectName) {
            sourcesFrom("multiproject-common")
        }

        build.addKoverRootProject {
            sourcesFrom("multiproject-user")
            subproject(subPath)

            koverMerged {
                enable()
            }
        }


        val runner = build.prepare()
        runner.run(":koverMergedXmlReport") {
            xml(defaultMergedXmlReport()) {
                classCounter("org.jetbrains.CommonClass").assertFullyCovered()
                classCounter("org.jetbrains.CommonInternalClass").assertFullyCovered()
                classCounter("org.jetbrains.UserClass").assertFullyCovered()
            }
        }
    }

    @Test
    fun testIsolatedProjectsReports() {
        val build = diverseBuild(engines = ALL_ENGINES, types = ALL_TYPES)
        val subPath = build.addKoverSubproject(subprojectName) {
            sourcesFrom("multiproject-common")
        }

        build.addKoverRootProject {
            sourcesFrom("multiproject-user")
            subproject(subPath)
        }


        val runner = build.prepare()
        runner.run("koverXmlReport") {
            xml(defaultXmlReport()) {
                classCounter("org.jetbrains.CommonClass").assertAbsent()
                classCounter("org.jetbrains.CommonInternalClass").assertAbsent()
                classCounter("org.jetbrains.UserClass").assertFullyCovered()
            }

            subproject(subprojectName) {
                xml(defaultXmlReport()) {
                    classCounter("org.jetbrains.UserClass").assertAbsent()

                    // common class covered partially because calls from the root project are not counted
                    classCounter("org.jetbrains.CommonClass").assertCovered()
                    classCounter("org.jetbrains.CommonInternalClass").assertCovered()
                }
            }
        }
    }

    @Test
    fun testDisabledKover() {
        val build = diverseBuild(engines = ALL_ENGINES, types = ALL_TYPES)
        val subPath = build.addKoverSubproject(subprojectName) {
            sourcesFrom("multiproject-common")
            kover {
                isDisabled = true
            }
        }

        build.addKoverRootProject {
            sourcesFrom("multiproject-user")
            subproject(subPath)
            kover {
                isDisabled = true
            }
        }

        val runner = build.prepare()
        runner.run("koverReport", "koverVerify") {
            checkDefaultBinaryReport(false)

            checkOutcome("koverHtmlReport", TaskOutcome.SKIPPED)
            checkOutcome("koverXmlReport", TaskOutcome.SKIPPED)
            checkOutcome("koverVerify", TaskOutcome.SKIPPED)

            subproject(subprojectName) {
                checkDefaultBinaryReport(false)
                checkOutcome("koverHtmlReport", TaskOutcome.SKIPPED)
                checkOutcome("koverXmlReport", TaskOutcome.SKIPPED)
                checkOutcome("koverVerify", TaskOutcome.SKIPPED)
            }
        }
    }

    @Test
    fun testExcludeProject() {
        val build = diverseBuild(engines = ALL_ENGINES, types = ALL_TYPES)
        val subPath = build.addKoverSubproject(subprojectName) {
            sourcesFrom("multiproject-common")
        }

        build.addKoverRootProject {
            sourcesFrom("multiproject-user")
            subproject(subPath)

            koverMerged {
                enable()
                filters {
                    projects {
                        excludes += subprojectName
                    }
                }
            }
        }

        val runner = build.prepare()
        runner.run("koverMergedReport") {
            checkDefaultBinaryReport()
            checkDefaultReports(false)
            checkDefaultMergedReports()
            xml(defaultMergedXmlReport()) {
                classCounter("org.jetbrains.UserClass").assertFullyCovered()

                // classes from disabled project should not be included in the merged report
                classCounter("org.jetbrains.CommonClass").assertAbsent()
                classCounter("org.jetbrains.CommonInternalClass").assertAbsent()
            }

            subproject(subprojectName) {
                checkDefaultBinaryReport(false)
                checkDefaultMergedReports(false)
                checkDefaultReports(false)
            }
        }
    }

    @Test
    fun testExcludeProjectByPath() {
        val build = diverseBuild(engines = ALL_ENGINES, types = ALL_TYPES)
        val subPath = build.addKoverSubproject(subprojectName) {
            sourcesFrom("multiproject-common")
        }

        build.addKoverRootProject {
            sourcesFrom("multiproject-user")
            subproject(subPath)

            koverMerged {
                enable()
                filters {
                    projects {
                        excludes += subPath
                    }
                }
            }
        }

        val runner = build.prepare()
        runner.run("koverMergedReport") {
            checkDefaultBinaryReport()
            checkDefaultReports(false)
            checkDefaultMergedReports()
            xml(defaultMergedXmlReport()) {
                classCounter("org.jetbrains.UserClass").assertFullyCovered()

                // classes from disabled project should not be included in the merged report
                classCounter("org.jetbrains.CommonClass").assertAbsent()
                classCounter("org.jetbrains.CommonInternalClass").assertAbsent()
            }

            subproject(subprojectName) {
                checkDefaultBinaryReport(false)
                checkDefaultMergedReports(false)
                checkDefaultReports(false)
            }
        }
    }

    @Test
    fun testNestedProjectInsideEmptyProject() {

        val projectDir = Files.createTempDirectory("nested-project").toFile()

        projectDir.resolve("settings.gradle.kts").apply {
            //language=kts
            writeText(
                """  
rootProject.name = "nested-project"

include(":subprojects:alpha-project")
                """.trimIndent()
            )
        }

        projectDir.resolve("build.gradle.kts").apply {
            //language=kts
            writeText(
                """
plugins {
    base
    id("org.jetbrains.kotlinx.kover")
}

repositories { mavenCentral() }

kover {
    isDisabled.set(false) 
}

koverMerged {
    enable() 
}
                """.trimIndent()
            )
        }


        projectDir.resolve("subprojects/alpha-project/build.gradle.kts").apply {
            parentFile.mkdirs()
            //language=kts
            writeText(
                """
plugins {
    kotlin("jvm") version embeddedKotlinVersion
    id("org.jetbrains.kotlinx.kover")
}

repositories { mavenCentral() }

dependencies {
    testImplementation(kotlin("test"))
}

kover {
    isDisabled.set(false) 
}
                """.trimIndent()
            )
        }

        projectDir.resolve("subprojects/alpha-project/src/test/kotlin/MyTest.kt").apply {
            parentFile.mkdirs()
            //language=kotlin
            writeText(
                """
import kotlin.test.*

class MyTest {
    @Test
    fun foo() {
      assertEquals("123", 123.toString())
    }
}
            """.trimIndent()
            )
        }

        val gradleRunner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()

        gradleRunner
            .withArguments(":tasks", "--stacktrace", "--info")
            .build().also { result ->
                assertTrue(result.output.contains("koverMergedReport"))
                assertEquals(
                    TaskOutcome.SUCCESS,
                    result.task(":tasks")?.outcome,
                    result.output
                )
            }

        gradleRunner
            .withArguments("check", "--stacktrace", "--info")
            .build().also { result ->
                assertEquals(
                    TaskOutcome.SUCCESS,
                    result.task(":subprojects:alpha-project:test")?.outcome,
                    result.output
                )
            }

        gradleRunner
            .withArguments(":koverMergedReport", "--stacktrace", "--info")
            .build().also { result ->
                assertTrue(result.output.contains("koverMergedReport"))
                assertEquals(
                    TaskOutcome.SUCCESS,
                    result.task(":koverMergedReport")?.outcome,
                    result.output
                )
            }
    }
}
