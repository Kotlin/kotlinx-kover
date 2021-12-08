package kotlinx.kover.test.functional.cases

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import org.gradle.testkit.runner.*
import kotlin.test.*

internal class ReportsCachingTests : BaseGradleScriptTest() {
    @Test
    fun testCachingForIntellij() {
        builder()
            .case("Test caching reports for IntelliJ Coverage Engine")
            .engines(CoverageEngine.INTELLIJ)
            .sources("simple")
            .build()
            .run("build", "--build-cache") {
                checkIntellijBinaryReport(DEFAULT_INTELLIJ_KJVM_BINARY, DEFAULT_INTELLIJ_KJVM_SMAP)
                checkReports(DEFAULT_XML, DEFAULT_HTML)
            }
            .run("clean", "--build-cache") {
                checkIntellijBinaryReport(DEFAULT_INTELLIJ_KJVM_BINARY, DEFAULT_INTELLIJ_KJVM_SMAP, false)
                checkReports(DEFAULT_XML, DEFAULT_HTML, false)
            }
            .run("build", "--build-cache") {
                checkIntellijBinaryReport(DEFAULT_INTELLIJ_KJVM_BINARY, DEFAULT_INTELLIJ_KJVM_SMAP)
                checkReports(DEFAULT_XML, DEFAULT_HTML)
                outcome(":test") { assertEquals(TaskOutcome.FROM_CACHE, this)}
                outcome(":koverXmlReport") { assertEquals(TaskOutcome.FROM_CACHE, this)}
                outcome(":koverHtmlReport") { assertEquals(TaskOutcome.FROM_CACHE, this)}
            }
    }

    @Test
    fun testCachingForJacoco() {
        builder()
            .case("Test caching reports for JaCoCo Coverage Engine")
            .engines(CoverageEngine.JACOCO)
            .sources("simple")
            .build()
            .run("build", "--build-cache") {
                checkJacocoBinaryReport(DEFAULT_JACOCO_KJVM_BINARY)
                checkReports(DEFAULT_XML, DEFAULT_HTML)
            }
            .run("clean", "--build-cache") {
                checkJacocoBinaryReport(DEFAULT_JACOCO_KJVM_BINARY, false)
                checkReports(DEFAULT_XML, DEFAULT_HTML, false)
            }
            .run("build", "--build-cache") {
                checkJacocoBinaryReport(DEFAULT_JACOCO_KJVM_BINARY)
                checkReports(DEFAULT_XML, DEFAULT_HTML)

                outcome(":test") { assertEquals(TaskOutcome.FROM_CACHE, this)}
                outcome(":koverXmlReport") { assertEquals(TaskOutcome.FROM_CACHE, this)}
                outcome(":koverHtmlReport") { assertEquals(TaskOutcome.FROM_CACHE, this)}
            }
    }

}
