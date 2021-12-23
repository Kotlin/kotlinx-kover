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
    fun testAggregateReports() {
        builder("Testing the generation of aggregating reports")
            .types(ProjectType.KOTLIN_JVM, ProjectType.KOTLIN_MULTIPLATFORM)
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .sources("multiproject-user")
            .subproject(subprojectName) {
                sources("multiproject-common")
            }
            .dependency(
                "implementation(project(\":$subprojectName\"))",
                "implementation project(':$subprojectName')"
            )
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
    fun testProjectsReports() {
        builder("Testing the generation of project reports")
            .types(ProjectType.KOTLIN_JVM, ProjectType.KOTLIN_MULTIPLATFORM)
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .sources("multiproject-user")
            .subproject(subprojectName) {
                sources("multiproject-common")
            }
            .dependency(
                "implementation(project(\":$subprojectName\"))",
                "implementation project(':$subprojectName')"
            )
            .build()
            .run("koverProjectReport") {
                xml(defaultXmlProjectReport()) {
                    assertCounterAbsent(classCounter("org.jetbrains.CommonClass"))
                    assertCounterAbsent(classCounter("org.jetbrains.CommonInternalClass"))
                    assertCounterFullyCovered(classCounter("org.jetbrains.UserClass"))
                }

                subproject(subprojectName) {
                    xml(defaultXmlProjectReport()) {
                        assertCounterFullyCovered(classCounter("org.jetbrains.CommonClass"))
                        assertCounterFullyCovered(classCounter("org.jetbrains.CommonInternalClass"))
                        assertCounterAbsent(classCounter("org.jetbrains.UserClass"))
                    }
                }
            }
    }

    @Test
    fun testDisableProject() {
        builder("Testing the generation of projects reports")
            .types(ProjectType.KOTLIN_JVM, ProjectType.KOTLIN_MULTIPLATFORM)
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .sources("multiproject-user")
            .subproject(subprojectName) {
                sources("multiproject-common")
            }
            .dependency(
                "implementation(project(\":$subprojectName\"))",
                "implementation project(':$subprojectName')"
            )
            .configKover { disabledProjects += subprojectName }
            .build()
            .run("build", "koverProjectReport") {
                checkDefaultBinaryReport()
                checkDefaultReports()
                checkDefaultProjectReports()
                xml(defaultXmlReport()) {
                    assertCounterFullyCovered(classCounter("org.jetbrains.UserClass"))

                    // classes from disabled project should not be included in the aggregated report
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
