/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.defaultTestTaskName
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.SlicedBuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.SlicedGeneratedTest


internal class TaskFilteringTests {
    /**
     * Compile tasks must be executed even if all test tasks are disabled.
     */
    @SlicedGeneratedTest(allLanguages = true, allTools = true)
    fun SlicedBuildConfigurator.testDisableInstrumentationOfTask() {
        addProjectWithKover {
            sourcesFrom("simple")
            kover {
                currentProject {
                    instrumentation {
                        disabledForTestTasks.add(defaultTestTaskName(slice.type))
                    }
                }
            }
        }

        run(":koverXmlReport") {
            // compile tasks must be invoked
            checkOutcome("compileKotlin", "SUCCESS")
            checkOutcome("compileJava", "NO-SOURCE")

            taskNotCalled(defaultTestTaskName(slice.type))

            // if task `test` is excluded from instrumentation then the binary report is not created for it
            checkDefaultBinReport(false)
        }
    }

    @SlicedGeneratedTest(allTools = true)
    fun SlicedBuildConfigurator.testDisableTestTask() {
        addProjectWithKover {
            sourcesFrom("simple")
        }

        run(":koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.SecondClass").assertCovered()
            }
        }

        run(":koverXmlReport", "-x", defaultTestTaskName(slice.type)) {
            taskNotCalled(defaultTestTaskName(slice.type))
            checkOutcome("koverGenerateArtifactJvm", "SUCCESS")
            xmlReport {
                classCounter("org.jetbrains.SecondClass").assertFullyMissed()
            }
        }
    }

    @SlicedGeneratedTest(allTools = true)
    fun SlicedBuildConfigurator.testSkipTestTask() {
        addProjectWithKover {
            sourcesFrom("simple")
        }

        run(":koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.SecondClass").assertCovered()
            }
        }

        edit("build.gradle.kts") {
            "$it\n\ntasks.test { onlyIf { false } }"
        }
        run(":koverXmlReport") {
            checkOutcome(defaultTestTaskName(slice.type), "SKIPPED")
            checkOutcome("koverGenerateArtifactJvm", "SUCCESS")
            xmlReport {
                classCounter("org.jetbrains.SecondClass").assertFullyMissed()
            }
        }
    }

    @SlicedGeneratedTest(allTools = true)
    fun SlicedBuildConfigurator.testExcludeAllTests() {
        addProjectWithKover {
            sourcesFrom("simple")
        }

        run(":koverXmlReport") {
            xmlReport {
                classCounter("org.jetbrains.SecondClass").assertCovered()
            }
        }

        edit("build.gradle.kts") {
            "$it\n\ntasks.test { filter { isFailOnNoMatchingTests = false; excludeTestsMatching(\"*\") } }"
        }
        run(":koverXmlReport") {
            checkOutcome(defaultTestTaskName(slice.type), "SUCCESS")
            checkOutcome("koverGenerateArtifactJvm", "SUCCESS")
            xmlReport {
                classCounter("org.jetbrains.SecondClass").assertFullyMissed()
            }
        }
    }

}
