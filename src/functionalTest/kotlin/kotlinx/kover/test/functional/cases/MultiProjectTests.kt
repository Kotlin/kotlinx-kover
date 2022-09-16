package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.framework.checker.*
import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.starter.*
import org.gradle.testkit.runner.*
import kotlin.test.*

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

    /*
    Test on error-fix "Kover plugin not applied in projects" when there are empty nested subproject.
    Issue https://github.com/Kotlin/kotlinx-kover/issues/222
     */
    @TemplateTest("nested-project", [":koverMergedReport"])
    fun CheckerContext.testNestedProjectInsideEmptyProject() {
        outcome(":subprojects:alpha-project:test") {
            assertEquals(TaskOutcome.SUCCESS, this)
        }
        outcome(":koverMergedReport") {
            assertEquals(TaskOutcome.SUCCESS, this)
        }
    }
}
