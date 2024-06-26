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
        reportAndCheck("SUCCESS", cached = true)

        run("clean", "--build-cache") {
            checkDefaultBinReport(false)
            checkDefaultReports(false)
        }
        reportAndCheck("FROM-CACHE", cached = true)
    }

    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testOutOfDateOnSources() {
        useLocalCache()

        addProjectWithKover {
            sourcesFrom("simple")
        }
        reportAndCheck("SUCCESS", cached = true)

        edit("src/main/kotlin/Sources.kt") {
            "$it\n class Additional"
        }
        // tasks must be restarted after the source code is edited
        reportAndCheck("SUCCESS", cached = true)

        edit("src/test/kotlin/TestClass.kt") {
            "$it\n class AdditionalTest"
        }

        // test task must be restarted after test class is edited
        // , but reports can be up-to-date because no sources changed.
        // For JaCoCo .exec binary report contains instrumentation time, so it's unstable between builds and can cause the report generation.
        // For Kover it is also not guaranteed to have a stable binary .ic file, so sometimes reports can be regenerated.
        reportAndCheck("SUCCESS", "UP-TO-DATE",true)
    }

    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testProjectReportCaching() {
        useLocalCache()

        addProjectWithKover {
            sourcesFrom("simple")
        }
        reportAndCheck("SUCCESS", cached = true)
        run("clean", "--build-cache") {
            checkDefaultBinReport(false)
            checkDefaultReports(false)
        }
        reportAndCheck("FROM-CACHE", cached = true)
    }


    private fun BuildConfigurator.reportAndCheck(
        outcome: String,
        reportsAlternativeOutcome: String = outcome,
        cached: Boolean = false
    ) {
        val args = if (cached) {
            arrayOf("koverXmlReport", "koverHtmlReport", "--build-cache", "--info")
        } else {
            arrayOf("koverXmlReport", "koverHtmlReport", "--info")
        }
        run(*args) {
            checkDefaultBinReport()
            checkDefaultReports()
            checkOutcome("test", outcome)
            checkOutcome("koverXmlReport", outcome, reportsAlternativeOutcome)
            checkOutcome("koverHtmlReport", outcome, reportsAlternativeOutcome)
        }
    }
}
