package kotlinx.kover.test.functional.cases

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.cases.utils.defaultXmlReport
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import kotlinx.kover.test.functional.core.ProjectType
import kotlin.test.*

private const val SUBMODULE_NAME = "common"

internal class MultiModulesTest : BaseGradleScriptTest() {
    @Test
    fun testAggregateReports() {
        builder("Testing the generation of aggregating reports")
            .types(ProjectType.KOTLIN_JVM, ProjectType.KOTLIN_MULTIPLATFORM)
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .sources("multimodule-user")
            .submodule(SUBMODULE_NAME) {
                sources("multimodule-common")
            }
            .dependency(
                "implementation(project(\":$SUBMODULE_NAME\"))",
                "implementation project(':$SUBMODULE_NAME')"
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
    fun testModuleReports() {
        builder("Testing the generation of module reports")
            .types(ProjectType.KOTLIN_JVM, ProjectType.KOTLIN_MULTIPLATFORM)
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .sources("multimodule-user")
            .submodule(SUBMODULE_NAME) {
                sources("multimodule-common")
            }
            .dependency(
                "implementation(project(\":$SUBMODULE_NAME\"))",
                "implementation project(':$SUBMODULE_NAME')"
            )
            .build()
            .run("koverModuleReport") {
                xml(defaultXmlModuleReport()) {
                    assertCounterAbsent(classCounter("org.jetbrains.CommonClass"))
                    assertCounterAbsent(classCounter("org.jetbrains.CommonInternalClass"))
                    assertCounterFullyCovered(classCounter("org.jetbrains.UserClass"))
                }

                submodule(SUBMODULE_NAME) {
                    xml(defaultXmlModuleReport()) {
                        assertCounterFullyCovered(classCounter("org.jetbrains.CommonClass"))
                        assertCounterFullyCovered(classCounter("org.jetbrains.CommonInternalClass"))
                        assertCounterAbsent(classCounter("org.jetbrains.UserClass"))
                    }
                }
            }
    }

    @Test
    fun testDisableModule() {
        builder("Testing the generation of module reports")
            .types(ProjectType.KOTLIN_JVM, ProjectType.KOTLIN_MULTIPLATFORM)
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .sources("multimodule-user")
            .submodule(SUBMODULE_NAME) {
                sources("multimodule-common")
            }
            .dependency(
                "implementation(project(\":$SUBMODULE_NAME\"))",
                "implementation project(':$SUBMODULE_NAME')"
            )
            .configKover { disabledModules += SUBMODULE_NAME }
            .build()
            .run("build", "koverModuleReport") {
                checkDefaultBinaryReport()
                checkDefaultReports()
                checkDefaultModuleReports()
                xml(defaultXmlReport()) {
                    assertCounterFullyCovered(classCounter("org.jetbrains.UserClass"))

                    // classes from disabled module should not be included in the aggregated report
                    assertCounterAbsent(classCounter("org.jetbrains.CommonClass"))
                    assertCounterAbsent(classCounter("org.jetbrains.CommonInternalClass"))
                }

                submodule(SUBMODULE_NAME) {
                    checkDefaultBinaryReport(false)
                    checkDefaultReports(false)
                    checkDefaultModuleReports(false)
                }
            }
    }
}
