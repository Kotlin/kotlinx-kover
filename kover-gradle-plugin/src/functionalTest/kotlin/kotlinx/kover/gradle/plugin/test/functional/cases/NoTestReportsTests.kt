package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.createCheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.defaultXmlReport
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.GradleBuild
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.buildFromTemplate
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runWithParams
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class NoTestReportsTests {
    @Test
    fun testNoTestsJvm() {
        val build = buildFromTemplate("no-tests-jvm").generate("no-tests-jvm", "templates")
        checkReportsGenerated(build)
    }

    @Test
    fun testNoTestsMpp() {
        val build = buildFromTemplate("no-tests-mpp").generate("no-tests-mpp", "templates")
        checkReportsGenerated(build)
    }

    private fun checkReportsGenerated(build: GradleBuild) {
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