package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.starter.*
import org.gradle.testkit.runner.*

internal class ReportsCachingTests {
    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testCaching() {
        useLocalCache()

        addKoverProject {
            sourcesFrom("simple")
        }
        run("koverReport", "--build-cache") {
            checkDefaultBinaryReport()
            checkDefaultReports()
            checkOutcome("test", "SUCCESS")
            checkOutcome("koverXmlReport", "SUCCESS")
            checkOutcome("koverHtmlReport", "SUCCESS")
        }
        run("clean", "--build-cache") {
            checkDefaultBinaryReport(false)
            checkDefaultReports(false)
        }
        run("koverReport", "--build-cache") {
            checkDefaultBinaryReport()
            checkDefaultReports()
            checkOutcome("test", "FROM-CACHE")
            checkOutcome("koverXmlReport", "FROM-CACHE")
            checkOutcome("koverHtmlReport", "FROM-CACHE")
        }
    }

    @SlicedGeneratedTest(allTools = true)
    fun BuildConfigurator.testProjectReportCaching() {
        useLocalCache()

        addKoverProject {
            sourcesFrom("simple")
        }
        run("koverReport", "--build-cache") {
            checkDefaultBinaryReport()
            checkDefaultReports()
            checkOutcome("test", "SUCCESS")
            checkOutcome("koverXmlReport", "SUCCESS")
            checkOutcome("koverHtmlReport", "SUCCESS")
        }
        run("clean", "--build-cache") {
            checkDefaultBinaryReport(false)
            checkDefaultReports(false)
        }
        run("koverReport", "--build-cache") {
            checkDefaultBinaryReport()
            checkDefaultReports()
            checkOutcome("test", "FROM-CACHE")
            checkOutcome("koverXmlReport", "FROM-CACHE")
            checkOutcome("koverHtmlReport", "FROM-CACHE")
        }
    }

}
