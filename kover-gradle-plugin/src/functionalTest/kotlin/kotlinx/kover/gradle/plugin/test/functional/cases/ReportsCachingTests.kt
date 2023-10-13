/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.commons.CoverageToolVendor
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

    @GeneratedTest(tool = CoverageToolVendor.KOVER)
    fun BuildConfigurator.testKoverOutOfDateOnSources() {
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

        // test task must be restarted after test class is edited, but reports is up-to-date because no sources changed
        reportAndCheck("SUCCESS", "UP-TO-DATE",true)
    }

    @GeneratedTest(tool = CoverageToolVendor.JACOCO)
    fun BuildConfigurator.testJaCoCoOutOfDateOnSources() {
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

        // test task must be restarted after test class is edited,
        // reports is not up-to-date because JaCoCo .exec binary report contains instrumentation time
        reportAndCheck("SUCCESS", "SUCCESS",true)
    }

    @GeneratedTest(tool = CoverageToolVendor.KOVER)
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



    private fun BuildConfigurator.reportAndCheck(testOutcome: String, reportsOutcome: String = testOutcome, cached: Boolean = false) {
        val args = if (cached) {
            arrayOf("koverXmlReport", "koverHtmlReport", "--build-cache", "--info")
        } else {
            arrayOf("koverXmlReport", "koverHtmlReport", "--info")
        }
        run(*args) {
            checkDefaultBinReport()
            checkDefaultReports()
            checkOutcome("test", testOutcome)
            checkOutcome("koverXmlReport", reportsOutcome)
            checkOutcome("koverHtmlReport", reportsOutcome)
        }
    }
}
