package kotlinx.kover.test.functional.cases

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import org.gradle.testkit.runner.*
import kotlin.test.*

internal class ReportsCachingTests : BaseGradleScriptTest() {
    @Test
    fun testCaching() {
        builder("Test caching aggregate reports")
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .sources("simple")
            .withLocalCache()
            .build()
            .run("build", "--build-cache") {
                checkDefaultBinaryReport()
                checkDefaultReports()
                outcome(":test") { assertEquals(TaskOutcome.SUCCESS, this) }
                outcome(":koverXmlReport") { assertEquals(TaskOutcome.SUCCESS, this) }
                outcome(":koverHtmlReport") { assertEquals(TaskOutcome.SUCCESS, this) }
            }
            .run("clean", "--build-cache") {
                checkDefaultBinaryReport(false)
                checkDefaultReports(false)
            }
            .run("build", "--build-cache") {
                checkDefaultBinaryReport()
                checkDefaultReports()
                outcome(":test") { assertEquals(TaskOutcome.FROM_CACHE, this) }
                outcome(":koverXmlReport") { assertEquals(TaskOutcome.FROM_CACHE, this) }
                outcome(":koverHtmlReport") { assertEquals(TaskOutcome.FROM_CACHE, this) }
            }
    }

    @Test
    fun testModuleCaching() {
        builder("Test caching module reports")
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .sources("simple")
            .withLocalCache()
            .build()
            .run("koverModuleReport", "--build-cache") {
                checkDefaultBinaryReport()
                checkDefaultModuleReports()
                outcome(":test") { assertEquals(TaskOutcome.SUCCESS, this) }
                outcome(":koverXmlModuleReport") { assertEquals(TaskOutcome.SUCCESS, this) }
                outcome(":koverHtmlModuleReport") { assertEquals(TaskOutcome.SUCCESS, this) }
            }
            .run("clean", "--build-cache") {
                checkDefaultBinaryReport(false)
                checkDefaultModuleReports(false)
            }
            .run("koverModuleReport", "--build-cache") {
                checkDefaultBinaryReport()
                checkDefaultModuleReports()
                outcome(":test") { assertEquals(TaskOutcome.FROM_CACHE, this) }
                outcome(":koverXmlModuleReport") { assertEquals(TaskOutcome.FROM_CACHE, this) }
                outcome(":koverHtmlModuleReport") { assertEquals(TaskOutcome.FROM_CACHE, this) }
            }
    }

}
