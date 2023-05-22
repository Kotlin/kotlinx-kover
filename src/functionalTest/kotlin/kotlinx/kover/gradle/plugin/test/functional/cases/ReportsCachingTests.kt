/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*

internal class ReportsCachingTests {
    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testCaching() {
        useLocalCache()

        addProjectWithKover {
            sourcesFrom("simple")
        }
        runAndCheckCached("SUCCESS")

        run("clean", "--build-cache") {
            checkDefaultRawReport(false)
            checkDefaultReports(false)
        }
        runAndCheckCached("FROM-CACHE")
    }

    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testOuOfDateOnSources() {
        useLocalCache()

        addProjectWithKover {
            sourcesFrom("simple")
        }
        runAndCheckCached("SUCCESS")

        edit("src/main/kotlin/Sources.kt") {
            "$it\n class Additional"
        }
        // tasks must be restarted after the source code is edited
        runAndCheckCached("SUCCESS")

        edit("src/test/kotlin/TestClass.kt") {
            "$it\n class AdditionalTest"
        }

        // tasks must be restarted after tests are edited
        runAndCheckCached("SUCCESS")
    }

    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testProjectReportCaching() {
        useLocalCache()

        addProjectWithKover {
            sourcesFrom("simple")
        }
        runAndCheckCached("SUCCESS")
        run("clean", "--build-cache") {
            checkDefaultRawReport(false)
            checkDefaultReports(false)
        }
        runAndCheckCached("FROM-CACHE")
    }


    private fun BuildConfigurator.runAndCheckCached(outcome: String) {
        run("koverXmlReport", "koverHtmlReport", "--build-cache") {
            checkDefaultRawReport()
            checkDefaultReports()
            checkOutcome("test", outcome)
            checkOutcome("koverXmlReport", outcome)
            checkOutcome("koverHtmlReport", outcome)
        }
    }
}
