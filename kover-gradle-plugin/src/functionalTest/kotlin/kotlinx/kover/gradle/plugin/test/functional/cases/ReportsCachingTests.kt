/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*

internal class ReportsCachingTests {
    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testNoCaching() {
        addProjectWithKover {
            sourcesFrom("simple")
        }
        reportAndCheck("SUCCESS")
        reportAndCheck("UP-TO-DATE")

        run("clean") {
            checkDefaultBinReport(false)
            checkDefaultReports(false)
        }
        reportAndCheck("SUCCESS")
    }

    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testCaching() {
        useLocalCache()

        addProjectWithKover {
            sourcesFrom("simple")
        }
        reportAndCheck("SUCCESS", true)

        run("clean", "--build-cache") {
            checkDefaultBinReport(false)
            checkDefaultReports(false)
        }
        reportAndCheck("FROM-CACHE", true)
    }

    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testOuOfDateOnSources() {
        useLocalCache()

        addProjectWithKover {
            sourcesFrom("simple")
        }
        reportAndCheck("SUCCESS", true)

        edit("src/main/kotlin/Sources.kt") {
            "$it\n class Additional"
        }
        // tasks must be restarted after the source code is edited
        reportAndCheck("SUCCESS", true)

        edit("src/test/kotlin/TestClass.kt") {
            "$it\n class AdditionalTest"
        }

        // tasks must be restarted after tests are edited
        reportAndCheck("SUCCESS", true)
    }

    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testProjectReportCaching() {
        useLocalCache()

        addProjectWithKover {
            sourcesFrom("simple")
        }
        reportAndCheck("SUCCESS", true)
        run("clean", "--build-cache") {
            checkDefaultBinReport(false)
            checkDefaultReports(false)
        }
        reportAndCheck("FROM-CACHE", true)
    }


    private fun BuildConfigurator.reportAndCheck(outcome: String, cached: Boolean = false) {
        val args = if (cached) {
            arrayOf("koverXmlReport", "koverHtmlReport", "--build-cache")
        } else {
            arrayOf("koverXmlReport", "koverHtmlReport")
        }
        run(*args) {
            checkDefaultBinReport()
            checkDefaultReports()
            checkOutcome("test", outcome)
            checkOutcome("koverXmlReport", outcome)
            checkOutcome("koverHtmlReport", outcome)
        }
    }
}
