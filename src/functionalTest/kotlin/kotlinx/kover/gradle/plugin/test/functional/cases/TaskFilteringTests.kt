/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.*
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.defaultTestTaskName
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*
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
                excludeTests {
                    taskName(defaultTestTaskName(slice.type))
                }
            }
        }

        run(":koverXmlReport") {
            // compile tasks must be invoked
            checkOutcome("compileKotlin", "SUCCESS")
            checkOutcome("compileJava", "NO-SOURCE")

            // if task `test` is excluded from instrumentation then the raw report is not created for it
            checkDefaultRawReport(false)
        }
    }
}