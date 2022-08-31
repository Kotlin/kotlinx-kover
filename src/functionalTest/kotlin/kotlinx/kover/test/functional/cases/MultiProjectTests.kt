package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.framework.checker.*
import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.starter.*
import org.gradle.testkit.runner.*

internal class MultiProjectTests {
    private val subprojectPath = ":common"

    @SlicedGeneratedTest(allTypes = true, allEngines = true)
    fun BuildConfigurator.testMergedReports() {
        addKoverProject(subprojectPath) {
            sourcesFrom("multiproject-common")
        }

        addKoverProject {
            sourcesFrom("multiproject-user")
            dependencyOnProject(subprojectPath)

            koverMerged {
                enable()
            }
        }

        run(":koverMergedXmlReport") {
            xml(defaultMergedXmlReport()) {
                classCounter("org.jetbrains.CommonClass").assertFullyCovered()
                classCounter("org.jetbrains.CommonInternalClass").assertFullyCovered()
                classCounter("org.jetbrains.UserClass").assertFullyCovered()
            }
        }
    }

    @SlicedGeneratedTest(allTypes = true, allEngines = true)
    fun BuildConfigurator.testIsolatedProjectsReports() {
        addKoverProject(subprojectPath) {
            sourcesFrom("multiproject-common")
        }

        addKoverProject {
            sourcesFrom("multiproject-user")
            dependencyOnProject(subprojectPath)
        }

        run("koverXmlReport") {
            xml(defaultXmlReport()) {
                classCounter("org.jetbrains.CommonClass").assertAbsent()
                classCounter("org.jetbrains.CommonInternalClass").assertAbsent()
                classCounter("org.jetbrains.UserClass").assertFullyCovered()
            }

            subproject(subprojectPath) {
                xml(defaultXmlReport()) {
                    classCounter("org.jetbrains.UserClass").assertAbsent()

                    // common class covered partially because calls from the root project are not counted
                    classCounter("org.jetbrains.CommonClass").assertCovered()
                    classCounter("org.jetbrains.CommonInternalClass").assertCovered()
                }
            }
        }
    }

    @SlicedGeneratedTest(allTypes = true, allEngines = true)
    fun BuildConfigurator.testDisabledKover() {
        addKoverProject(subprojectPath) {
            sourcesFrom("multiproject-common")
            kover {
                isDisabled = true
            }
        }

        addKoverProject {
            sourcesFrom("multiproject-user")
            dependencyOnProject(subprojectPath)
            kover {
                isDisabled = true
            }
        }

        run("koverReport", "koverVerify") {
            checkDefaultBinaryReport(false)

            checkOutcome("koverHtmlReport", TaskOutcome.SKIPPED)
            checkOutcome("koverXmlReport", TaskOutcome.SKIPPED)
            checkOutcome("koverVerify", TaskOutcome.SKIPPED)

            subproject(subprojectPath) {
                checkDefaultBinaryReport(false)
                checkOutcome("koverHtmlReport", TaskOutcome.SKIPPED)
                checkOutcome("koverXmlReport", TaskOutcome.SKIPPED)
                checkOutcome("koverVerify", TaskOutcome.SKIPPED)
            }
        }
    }

    @SlicedGeneratedTest(allTypes = true, allEngines = true)
    fun BuildConfigurator.testExcludeProject() {
        addKoverProject(subprojectPath) {
            sourcesFrom("multiproject-common")
        }

        addKoverProject {
            sourcesFrom("multiproject-user")
            dependencyOnProject(subprojectPath)

            koverMerged {
                enable()
                filters {
                    projects {
                        excludes += subprojectPath.removePrefix(":")
                    }
                }
            }
        }

        run("koverMergedReport") {
            checkDefaultBinaryReport()
            checkDefaultReports(false)
            checkDefaultMergedReports()
            xml(defaultMergedXmlReport()) {
                classCounter("org.jetbrains.UserClass").assertFullyCovered()

                // classes from disabled project should not be included in the merged report
                classCounter("org.jetbrains.CommonClass").assertAbsent()
                classCounter("org.jetbrains.CommonInternalClass").assertAbsent()
            }

            subproject(subprojectPath) {
                checkDefaultBinaryReport(false)
                checkDefaultMergedReports(false)
                checkDefaultReports(false)
            }
        }
    }

    @SlicedGeneratedTest(allTypes = true, allEngines = true)
    fun BuildConfigurator.testExcludeProjectByPath() {
        addKoverProject(subprojectPath) {
            sourcesFrom("multiproject-common")
        }

        addKoverProject {
            sourcesFrom("multiproject-user")
            dependencyOnProject(subprojectPath)

            koverMerged {
                enable()
                filters {
                    projects {
                        excludes += subprojectPath
                    }
                }
            }
        }

        run("koverMergedReport") {
            checkDefaultBinaryReport()
            checkDefaultReports(false)
            checkDefaultMergedReports()
            xml(defaultMergedXmlReport()) {
                classCounter("org.jetbrains.UserClass").assertFullyCovered()

                // classes from disabled project should not be included in the merged report
                classCounter("org.jetbrains.CommonClass").assertAbsent()
                classCounter("org.jetbrains.CommonInternalClass").assertAbsent()
            }

            subproject(subprojectPath) {
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
