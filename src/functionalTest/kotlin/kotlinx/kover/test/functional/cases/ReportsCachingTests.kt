package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.starter.*
import org.gradle.testkit.runner.*

internal class ReportsCachingTests {
    @SlicedGeneratedTest(allEngines = true)
    fun BuildConfigurator.testCaching() {
        useLocalCache()

        addKoverProject {
            sourcesFrom("simple")
        }
        run("koverReport", "--build-cache") {
            checkDefaultBinaryReport()
            checkDefaultReports()
            checkOutcome("test", TaskOutcome.SUCCESS)
            checkOutcome("koverXmlReport", TaskOutcome.SUCCESS)
            checkOutcome("koverHtmlReport", TaskOutcome.SUCCESS)
        }
        run("clean", "--build-cache") {
            checkDefaultBinaryReport(false)
            checkDefaultReports(false)
        }
        run("koverReport", "--build-cache") {
            checkDefaultBinaryReport()
            checkDefaultReports()
            checkOutcome("test", TaskOutcome.FROM_CACHE)
            checkOutcome("koverXmlReport", TaskOutcome.FROM_CACHE)
            checkOutcome("koverHtmlReport", TaskOutcome.FROM_CACHE)
        }
    }

    @SlicedGeneratedTest(allEngines = true)
    fun BuildConfigurator.testProjectReportCaching() {
        useLocalCache()

        addKoverProject {
            sourcesFrom("simple")
        }
        run("koverReport", "--build-cache") {
            checkDefaultBinaryReport()
            checkDefaultReports()
            checkOutcome("test", TaskOutcome.SUCCESS)
            checkOutcome("koverXmlReport", TaskOutcome.SUCCESS)
            checkOutcome("koverHtmlReport", TaskOutcome.SUCCESS)
        }
        run("clean", "--build-cache") {
            checkDefaultBinaryReport(false)
            checkDefaultReports(false)
        }
        run("koverReport", "--build-cache") {
            checkDefaultBinaryReport()
            checkDefaultReports()
            checkOutcome("test", TaskOutcome.FROM_CACHE)
            checkOutcome("koverXmlReport", TaskOutcome.FROM_CACHE)
            checkOutcome("koverHtmlReport", TaskOutcome.FROM_CACHE)
        }
    }

}
