package kotlinx.kover.test.functional.cases

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.cases.utils.defaultXmlReport
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import kotlinx.kover.test.functional.core.ProjectType
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
                xml(defaultXmlReport()) {
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
                xml(defaultXmlProjectReport()) {
                    assertCounterAbsent(classCounter("org.jetbrains.CommonClass"))
                    assertCounterAbsent(classCounter("org.jetbrains.CommonInternalClass"))
                    assertCounterFullyCovered(classCounter("org.jetbrains.UserClass"))
                }

                subproject(subprojectName) {
                    xml(defaultXmlProjectReport()) {
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
                xml(defaultXmlProjectReport()) {
                    assertCounterAbsent(classCounter("org.jetbrains.CommonClass"))
                    assertCounterAbsent(classCounter("org.jetbrains.CommonInternalClass"))
                    assertCounterFullyCovered(classCounter("org.jetbrains.UserClass"))
                }

                subproject(subprojectName) {
                    xml(defaultXmlProjectReport()) {
                        assertCounterAbsent(classCounter("org.jetbrains.UserClass"))

                        // common class fully covered because calls from the root project are counted too
                        assertCounterFullyCovered(classCounter("org.jetbrains.CommonClass"))
                        assertCounterFullyCovered(classCounter("org.jetbrains.CommonInternalClass"))
                    }
                }
            }
    }

    @Test
    fun testDisableSubproject() {
        builder("Testing disabling tests of subproject")
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
                checkDefaultReports()
                checkDefaultProjectReports()
                xml(defaultXmlReport()) {
                    assertCounterFullyCovered(classCounter("org.jetbrains.UserClass"))

                    // classes from disabled project should not be included in the merged report
                    assertCounterAbsent(classCounter("org.jetbrains.CommonClass"))
                    assertCounterAbsent(classCounter("org.jetbrains.CommonInternalClass"))
                }

                subproject(subprojectName) {
                    checkDefaultBinaryReport(false)
                    checkDefaultReports(false)
                    checkDefaultProjectReports(false)
                }
            }
    }
}
