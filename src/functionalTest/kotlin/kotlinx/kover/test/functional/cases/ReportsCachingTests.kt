package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.starter.*

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
