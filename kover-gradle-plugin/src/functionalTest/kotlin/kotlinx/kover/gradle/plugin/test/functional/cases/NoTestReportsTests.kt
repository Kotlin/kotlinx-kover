package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.createCheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.defaultXmlReport
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.BuildSource
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.buildFromTemplate
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runWithParams
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NoTestReportsTests {
    @Test
    fun testNoTestsJvm() {
        val build = buildFromTemplate("no-tests-jvm")
        check(build)
        checkWithVerify(build)
    }

    @Test
    fun testNoTestsMpp() {
        val build = buildFromTemplate("no-tests-mpp")
        check(build)
        checkWithVerify(build)
    }

    private fun check(buildSource: BuildSource) {
        val build = buildSource.generate()
        val buildResult = build.runWithParams("koverXmlReport", "koverHtmlReport")
        val checkerContext = build.createCheckerContext(buildResult)

        checkerContext.xml(defaultXmlReport()) {
            classCounter("kotlinx.kover.templates.ExampleClass").assertFullyMissed()
        }
        assertTrue(buildResult.isSuccessful)
    }

    private fun checkWithVerify(buildSource: BuildSource) {
        val build = buildSource.generate()
        val buildResult = build.runWithParams("koverXmlReport", "koverHtmlReport", "koverVerify")
        val checkerContext = build.createCheckerContext(buildResult)

        checkerContext.xml(defaultXmlReport()) {
            classCounter("kotlinx.kover.templates.ExampleClass").assertFullyMissed()
        }
        checkerContext.checkHtmlReport()
        assertFalse(buildResult.isSuccessful)
        assertContains(buildResult.output, "Rule violated: lines covered percentage is 0.000000, but expected minimum is 50")
    }

}