package kotlinx.kover.test.functional.cases

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import org.gradle.testkit.runner.*
import kotlin.test.*

internal class ReportsCachingTests : BaseGradleScriptTest() {
    @Test
    fun testCachingForIntellij() {
        runner()
            .case("Test caching reports for IntelliJ Coverage Engine")
            .engines(CoverageEngine.INTELLIJ)
            .sources("simple-single")
            .check("build", "--build-cache") {
                checkIntellijBinaryReport(DEFAULT_INTELLIJ_KJVM_BINARY, DEFAULT_INTELLIJ_KJVM_SMAP)
                checkReports(DEFAULT_XML, DEFAULT_HTML)
            }
            .check("clean", "--build-cache") {
                checkIntellijBinaryReport(DEFAULT_INTELLIJ_KJVM_BINARY, DEFAULT_INTELLIJ_KJVM_SMAP, false)
                checkReports(DEFAULT_XML, DEFAULT_HTML, false)
            }
            .check("build", "--build-cache") {
                checkIntellijBinaryReport(DEFAULT_INTELLIJ_KJVM_BINARY, DEFAULT_INTELLIJ_KJVM_SMAP)
                checkReports(DEFAULT_XML, DEFAULT_HTML)
                assertEquals(TaskOutcome.FROM_CACHE, outcome(":test"))
                assertEquals(TaskOutcome.FROM_CACHE, outcome(":koverXmlReport"))
                assertEquals(TaskOutcome.FROM_CACHE, outcome(":koverHtmlReport"))
            }
    }

    @Test
    fun testCachingForJacoco() {
        runner()
            .case("Test caching reports for JaCoCo Coverage Engine")
            .engines(CoverageEngine.JACOCO)
            .sources("simple-single")
            .check("build", "--build-cache") {
                checkJacocoBinaryReport(DEFAULT_JACOCO_KJVM_BINARY)
                checkReports(DEFAULT_XML, DEFAULT_HTML)
            }
            .check("clean", "--build-cache") {
                checkJacocoBinaryReport(DEFAULT_JACOCO_KJVM_BINARY, false)
                checkReports(DEFAULT_XML, DEFAULT_HTML, false)
            }
            .check("build", "--build-cache") {
                checkJacocoBinaryReport(DEFAULT_JACOCO_KJVM_BINARY)
                checkReports(DEFAULT_XML, DEFAULT_HTML)

                assertEquals(TaskOutcome.FROM_CACHE, outcome(":test"))
                assertEquals(TaskOutcome.FROM_CACHE, outcome(":koverXmlReport"))
                assertEquals(TaskOutcome.FROM_CACHE, outcome(":koverHtmlReport"))
            }
    }

}
