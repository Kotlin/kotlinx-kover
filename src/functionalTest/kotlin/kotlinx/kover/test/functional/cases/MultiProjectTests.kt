package kotlinx.kover.test.functional.cases

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.cases.utils.defaultMergedXmlReport
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import kotlinx.kover.test.functional.core.ProjectType
import org.gradle.testkit.runner.*
import kotlin.test.*

internal class MultiProjectTests : BaseGradleScriptTest() {
    private val subprojectName = "common"

    @Test
    fun testMergedReports() {
        builder("Testing the generation of merged reports")
            .types(ProjectType.KOTLIN_JVM, ProjectType.KOTLIN_MULTIPLATFORM)
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .sources("multiproject-user")
            .subproject(subprojectName) {
                sources("multiproject-common")
            }
            .build()
            .run("build") {
                xml(defaultMergedXmlReport()) {
                    assertCounterFullyCovered(classCounter("org.jetbrains.CommonClass"))
                    assertCounterFullyCovered(classCounter("org.jetbrains.CommonInternalClass"))
                    assertCounterFullyCovered(classCounter("org.jetbrains.UserClass"))
                }
            }
    }

    @Test
    fun testIsolatedProjectsReports() {
        builder("Testing the generation of project reports with running tests only for this project")
            .types(ProjectType.KOTLIN_JVM, ProjectType.KOTLIN_MULTIPLATFORM)
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .sources("multiproject-user")
            .subproject(subprojectName) {
                sources("multiproject-common")
            }
            .build()
            .run("koverReport") {
                xml(defaultXmlReport()) {
                    assertCounterAbsent(classCounter("org.jetbrains.CommonClass"))
                    assertCounterAbsent(classCounter("org.jetbrains.CommonInternalClass"))
                    assertCounterFullyCovered(classCounter("org.jetbrains.UserClass"))
                }

                subproject(subprojectName) {
                    xml(defaultXmlReport()) {
                        assertCounterAbsent(classCounter("org.jetbrains.UserClass"))

                        // common class covered partially because calls from the root project are not counted
                        assertCounterCovered(classCounter("org.jetbrains.CommonClass"))
                        assertCounterCovered(classCounter("org.jetbrains.CommonInternalClass"))
                    }
                }
            }
    }

    @Test
    fun testLinkedProjectsReports() {
        builder("Testing the generation of project reports with running all tests for all projects")
            .types(ProjectType.KOTLIN_JVM, ProjectType.KOTLIN_MULTIPLATFORM)
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .configKover { runAllTestsForProjectTask = true }
            .sources("multiproject-user")
            .subproject(subprojectName) {
                sources("multiproject-common")
            }
            .build()
            .run("koverReport") {
                xml(defaultXmlReport()) {
                    assertCounterAbsent(classCounter("org.jetbrains.CommonClass"))
                    assertCounterAbsent(classCounter("org.jetbrains.CommonInternalClass"))
                    assertCounterFullyCovered(classCounter("org.jetbrains.UserClass"))
                }

                subproject(subprojectName) {
                    xml(defaultXmlReport()) {
                        assertCounterAbsent(classCounter("org.jetbrains.UserClass"))

                        // common class fully covered because calls from the root project are counted too
                        assertCounterFullyCovered(classCounter("org.jetbrains.CommonClass"))
                        assertCounterFullyCovered(classCounter("org.jetbrains.CommonInternalClass"))
                    }
                }
            }
    }

    @Test
    fun testDisabledKover() {
        builder("Testing disabling whole Kover")
            .sources("multiproject-user")
            .subproject(subprojectName) {
                sources("multiproject-common")
            }
            .configKover { disabled = true }
            .build()
            .run("build", "koverHtmlReport") {
                checkDefaultBinaryReport(false)
                checkOutcome("koverMergedHtmlReport", TaskOutcome.SKIPPED)
                checkOutcome("koverMergedVerify", TaskOutcome.SKIPPED)

                checkOutcome("koverHtmlReport", TaskOutcome.SKIPPED)
                checkOutcome("koverVerify", TaskOutcome.SKIPPED)
                checkOutcome("koverMergedHtmlReport", TaskOutcome.SKIPPED)

                subproject(subprojectName) {
                    checkDefaultBinaryReport(false)
                    checkOutcome("koverHtmlReport", TaskOutcome.SKIPPED)
                    checkOutcome("koverVerify", TaskOutcome.SKIPPED)
                }
            }
    }

    @Test
    fun testDisableSubproject() {
        builder("Testing disabling one of subproject")
            .types(ProjectType.KOTLIN_JVM, ProjectType.KOTLIN_MULTIPLATFORM)
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .sources("multiproject-user")
            .subproject(subprojectName) {
                sources("multiproject-common")
            }
            .configKover { disabledProjects += subprojectName }
            .build()
            .run("build", "koverReport") {
                checkDefaultBinaryReport()
                checkDefaultMergedReports()
                checkDefaultReports()
                xml(defaultMergedXmlReport()) {
                    assertCounterFullyCovered(classCounter("org.jetbrains.UserClass"))

                    // classes from disabled project should not be included in the merged report
                    assertCounterAbsent(classCounter("org.jetbrains.CommonClass"))
                    assertCounterAbsent(classCounter("org.jetbrains.CommonInternalClass"))
                }

                subproject(subprojectName) {
                    checkDefaultBinaryReport(false)
                    checkDefaultMergedReports(false)
                    checkDefaultReports(false)
                }
            }
    }
}
