/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.*
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.defaultXmlReport
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.GeneratedTest


internal class LogTests {

    @GeneratedTest
    fun BuildConfigurator.testHtmlPathLogs() {
        addProjectWithKover {
            sourcesFrom("simple")
            useLocalCache()
        }

        run(":koverHtmlReport", "--build-cache") {
            checkOutcome("koverHtmlReport", "SUCCESS")
            taskOutput("koverHtmlReport") {
                contains("Kover: HTML report for")
            }
        }

        run(":koverHtmlReport", "--build-cache") {
            checkOutcome("koverHtmlReport", "UP-TO-DATE")
            taskOutput("koverHtmlReport") {
                contains("Kover: HTML report for")
            }
        }

        run("clean", "--build-cache")

        run(":koverHtmlReport", "--build-cache") {
            checkOutcome("koverHtmlReport", "FROM-CACHE")
            taskOutput("koverHtmlReport") {
                contains("Kover: HTML report for")
            }
        }

    }

}
