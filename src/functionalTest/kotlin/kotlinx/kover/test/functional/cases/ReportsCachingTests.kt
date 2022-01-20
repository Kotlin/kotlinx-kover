package kotlinx.kover.test.functional.cases

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import org.gradle.testkit.runner.*
import kotlin.test.*

internal class ReportsCachingTests : BaseGradleScriptTest() {
    @Test
    fun testCaching() {
        builder("Test caching of merged reports")
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .sources("simple")
            .withLocalCache()
            .build()
            .run("build", "--build-cache") {
                checkDefaultBinaryReport()
                checkDefaultMergedReports()
                checkOutcome("test", TaskOutcome.SUCCESS)
                checkOutcome("koverMergedXmlReport", TaskOutcome.SUCCESS)
                checkOutcome("koverMergedHtmlReport", TaskOutcome.SUCCESS)
            }
            .run("clean", "--build-cache") {
                checkDefaultBinaryReport(false)
                checkDefaultMergedReports(false)
            }
            .run("build", "--build-cache") {
                checkDefaultBinaryReport()
                checkDefaultMergedReports()
                checkOutcome("test", TaskOutcome.FROM_CACHE)
                checkOutcome("koverMergedXmlReport", TaskOutcome.FROM_CACHE)
                checkOutcome("koverMergedHtmlReport", TaskOutcome.FROM_CACHE)
            }
    }

    @Test
    fun testProjectReportCaching() {
        builder("Test caching projects reports")
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .sources("simple")
            .withLocalCache()
            .build()
            .run("koverReport", "--build-cache") {
                checkDefaultBinaryReport()
                checkDefaultReports()
                checkOutcome("test", TaskOutcome.SUCCESS)
                checkOutcome("koverXmlReport", TaskOutcome.SUCCESS)
                checkOutcome("koverHtmlReport", TaskOutcome.SUCCESS)
            }
            .run("clean", "--build-cache") {
                checkDefaultBinaryReport(false)
                checkDefaultReports(false)
            }
            .run("koverReport", "--build-cache") {
                checkDefaultBinaryReport()
                checkDefaultReports()
                checkOutcome("test", TaskOutcome.FROM_CACHE)
                checkOutcome("koverXmlReport", TaskOutcome.FROM_CACHE)
                checkOutcome("koverHtmlReport", TaskOutcome.FROM_CACHE)
            }
    }

}
