/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.GeneratedTest
import kotlin.test.assertTrue


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
                assertTrue { contains("Kover: HTML report for") }
            }
        }

        run(":koverHtmlReport", "--build-cache") {
            checkOutcome("koverHtmlReport", "UP-TO-DATE")
            taskOutput("koverHtmlReport") {
                assertTrue { contains("Kover: HTML report for") }
            }
        }

        run("clean", "--build-cache")

        run(":koverHtmlReport", "--build-cache") {
            checkOutcome("koverHtmlReport", "FROM-CACHE")
            taskOutput("koverHtmlReport") {
                assertTrue { contains("Kover: HTML report for") }
            }
        }

    }

}
