/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.*
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.defaultTestTaskName
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*

internal class MultiProjectTests {
    private val subprojectPath = ":common"

    @SlicedGeneratedTest(allTypes = true, allTools = true)
    fun BuildConfigurator.testMergedReports() {
        addProjectWithKover(subprojectPath) {
            sourcesFrom("multiproject-common")
        }

        addProjectWithKover {
            sourcesFrom("multiproject-user")
            dependencyKover(subprojectPath)
        }

        run(":koverXmlReport", ":koverHtmlReport") {
            xml(defaultXmlReport()) {
                classCounter("org.jetbrains.CommonClass").assertFullyCovered()
                classCounter("org.jetbrains.CommonInternalClass").assertFullyCovered()
                classCounter("org.jetbrains.UserClass").assertFullyCovered()
            }
        }
    }

    @SlicedGeneratedTest(allTypes = true, allTools = true)
    fun BuildConfigurator.testIsolatedProjectsReports() {
        addProjectWithKover(subprojectPath) {
            sourcesFrom("multiproject-common")
        }

        addProjectWithKover {
            sourcesFrom("multiproject-user")
            dependencyKover(subprojectPath)
        }

        run("koverXmlReport") {
            subproject(subprojectPath) {
                xml(defaultXmlReport()) {
                    classCounter("org.jetbrains.UserClass").assertAbsent()

                    classCounter("org.jetbrains.CommonInternalClass").assertFullyCovered()

                    // common class covered partially because calls from the root project are not counted
                    classCounter("org.jetbrains.CommonClass").assertCoveredPartially()
                }
            }
        }
    }

    @SlicedGeneratedTest(allTypes = true, allTools = true)
    fun SlicedBuildConfigurator.testDisabledKover() {
        addProjectWithKover(subprojectPath) {
            sourcesFrom("multiproject-common")
            kover {
                disable()
            }
        }

        addProjectWithKover {
            sourcesFrom("multiproject-user")
            dependencyKover(subprojectPath)
            kover {
                disable()
            }
        }

        run("koverXmlReport", "koverHtmlReport", "koverVerify") {
            checkDefaultBinReport(false)
            taskNotCalled(defaultTestTaskName(slice.type))


            subproject(subprojectPath) {
                checkDefaultBinReport(false)
                taskNotCalled(defaultTestTaskName(slice.type))
            }
        }
    }

    @SlicedGeneratedTest(allTypes = true, allTools = true)
    fun SlicedBuildConfigurator.testDisabledTestTasks() {
        addProjectWithKover(subprojectPath) {
            sourcesFrom("multiproject-common")
            kover {
                excludeTests{
                    tasks(defaultTestTaskName(slice.type))
                }
            }
        }

        addProjectWithKover {
            sourcesFrom("multiproject-user")
            dependencyKover(subprojectPath)
            kover {
                excludeTests{
                    tasks(defaultTestTaskName(slice.type))
                }
            }
        }

        run("koverXmlReport", "koverHtmlReport", "koverVerify") {
            checkDefaultBinReport(false)
            taskNotCalled(defaultTestTaskName(slice.type))

            subproject(subprojectPath) {
                checkDefaultBinReport(false)
                taskNotCalled(defaultTestTaskName(slice.type))
                checkOutcome("koverXmlReport", "SUCCESS")
                checkOutcome("koverHtmlReport", "SUCCESS")
                checkOutcome("koverVerify", "SUCCESS")
            }
        }
    }

    /*
     *Test on error-fix "Kover plugin not applied in projects" when there are empty nested subproject.
     *Issue https://github.com/Kotlin/kotlinx-kover/issues/222
     */
    @TemplateTest("nested-project", [":koverXmlReport"])
    fun CheckerContext.testNestedProjectInsideEmptyProject() {
        checkOutcome(":subprojects:alpha-project:test", "SUCCESS")
        checkOutcome(":koverXmlReport", "SUCCESS")
    }
}
