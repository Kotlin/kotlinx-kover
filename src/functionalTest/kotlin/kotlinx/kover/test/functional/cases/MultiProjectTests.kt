package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.cases.utils.defaultMergedXmlReport
import kotlinx.kover.test.functional.core.*
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import org.gradle.testkit.runner.*
import kotlin.test.*

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
//
//    @Test
//    fun testLinkedProjectsReports() {
//        builder("Testing the generation of project reports with running all tests for all projects")
//            .types(ProjectType.KOTLIN_JVM, ProjectType.KOTLIN_MULTIPLATFORM)
//            .engines(CoverageEngineVendor.INTELLIJ, CoverageEngineVendor.JACOCO)
//            .configKover { runAllTestsForProjectTask = true }
//            .sources("multiproject-user")
//            .subproject(subprojectName) {
//                sources("multiproject-common")
//            }
//            .build()
//            .run("koverReport") {
//                xml(defaultXmlReport()) {
//                    classCounter("org.jetbrains.CommonClass").assertAbsent()
//                    classCounter("org.jetbrains.CommonInternalClass").assertAbsent()
//                    classCounter("org.jetbrains.UserClass").assertFullyCovered()
//                }
//
//                subproject(subprojectName) {
//                    xml(defaultXmlReport()) {
//                        classCounter("org.jetbrains.UserClass").assertAbsent()
//
//                        // common class fully covered because calls from the root project are counted too
//                        classCounter("org.jetbrains.CommonClass").assertFullyCovered()
//                        classCounter("org.jetbrains.CommonInternalClass").assertFullyCovered()
//                    }
//                }
//            }
//    }

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
    fun testIncludeProject() {
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
                        includes += rootName
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
    fun testIncludeProjectByPath() {
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
                        includes += ":"
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
}
