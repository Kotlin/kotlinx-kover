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
        run("koverXmlReport", "koverHtmlReport", "--build-cache") {
            checkDefaultRawReport()
            checkDefaultReports()
            checkOutcome("test", "SUCCESS")
            checkOutcome("koverXmlReport", "SUCCESS")
            checkOutcome("koverHtmlReport", "SUCCESS")
        }
        run("clean", "--build-cache") {
            checkDefaultRawReport(false)
            checkDefaultReports(false)
        }
        run("koverXmlReport", "koverHtmlReport", "--build-cache") {
            checkDefaultRawReport()
            checkDefaultReports()
            checkOutcome("test", "FROM-CACHE")
            checkOutcome("koverXmlReport", "FROM-CACHE")
            checkOutcome("koverHtmlReport", "FROM-CACHE")
        }
    }

    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testProjectReportCaching() {
        useLocalCache()

        addProjectWithKover {
            sourcesFrom("simple")
        }
        run("koverXmlReport", "koverHtmlReport", "--build-cache") {
            checkDefaultRawReport()
            checkDefaultReports()
            checkOutcome("test", "SUCCESS")
            checkOutcome("koverXmlReport", "SUCCESS")
            checkOutcome("koverHtmlReport", "SUCCESS")
        }
        run("clean", "--build-cache") {
            checkDefaultRawReport(false)
            checkDefaultReports(false)
        }
        run("koverXmlReport", "koverHtmlReport", "--build-cache") {
            checkDefaultRawReport()
            checkDefaultReports()
            checkOutcome("test", "FROM-CACHE")
            checkOutcome("koverXmlReport", "FROM-CACHE")
            checkOutcome("koverHtmlReport", "FROM-CACHE")
        }
    }

}
