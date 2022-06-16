package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.*
import kotlinx.kover.test.functional.core.ALL_ENGINES
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import org.gradle.testkit.runner.*
import kotlin.test.*

internal class ReportsCachingTests : BaseGradleScriptTest() {
    @Test
    fun testCaching() {
        val build = diverseBuild(engines = ALL_ENGINES, withCache = true)
        build.addKoverRootProject {
            sourcesFrom("simple")
        }
        val runner = build.prepare()
        runner.run("koverReport") {
            checkDefaultBinaryReport()
            checkDefaultReports()
            checkOutcome("test", TaskOutcome.SUCCESS)
            checkOutcome("koverXmlReport", TaskOutcome.SUCCESS)
            checkOutcome("koverHtmlReport", TaskOutcome.SUCCESS)
        }
        runner.run("clean") {
            checkDefaultBinaryReport(false)
            checkDefaultReports(false)
        }
        runner.run("koverReport") {
            checkDefaultBinaryReport()
            checkDefaultReports()
            checkOutcome("test", TaskOutcome.FROM_CACHE)
            checkOutcome("koverXmlReport", TaskOutcome.FROM_CACHE)
            checkOutcome("koverHtmlReport", TaskOutcome.FROM_CACHE)
        }
    }

    @Test
    fun testProjectReportCaching() {
        val build = diverseBuild(engines = ALL_ENGINES, withCache = true)
        build.addKoverRootProject {
            sourcesFrom("simple")
        }
        val runner = build.prepare()
        runner.run("koverReport") {
            checkDefaultBinaryReport()
            checkDefaultReports()
            checkOutcome("test", TaskOutcome.SUCCESS)
            checkOutcome("koverXmlReport", TaskOutcome.SUCCESS)
            checkOutcome("koverHtmlReport", TaskOutcome.SUCCESS)
        }
        runner.run("clean") {
            checkDefaultBinaryReport(false)
            checkDefaultReports(false)
        }
        runner.run("koverReport") {
            checkDefaultBinaryReport()
            checkDefaultReports()
            checkOutcome("test", TaskOutcome.FROM_CACHE)
            checkOutcome("koverXmlReport", TaskOutcome.FROM_CACHE)
            checkOutcome("koverHtmlReport", TaskOutcome.FROM_CACHE)
        }
    }

}
