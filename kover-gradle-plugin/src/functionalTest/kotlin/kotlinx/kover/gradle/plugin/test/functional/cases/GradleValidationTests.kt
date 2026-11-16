package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.TemplateTest
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.BuildOptions
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.buildFromTemplate
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runWithParams
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GradleValidationTests {

    // test for https://github.com/Kotlin/kotlinx-kover/issues/842
    @Test
    fun testReportsWithoutAntAccess() {
        val build = buildFromTemplate("counters").generate()
        val result = build.runWithParams(
            "koverXmlReport", "koverHtmlReport", "--rerun-tasks", "--warning-mode=all",
            options = BuildOptions(gradleVersion = "9.9.1")
        )
        assertTrue(result.isSuccessful, "Report generation failed.\nBuild log:\n${result.output}")
        assertEquals("SUCCESS", result.taskOutcome(":koverXmlReport"), result.output)
        assertEquals("SUCCESS", result.taskOutcome(":koverHtmlReport"), result.output)
        assertFalse(result.output.contains("The Task.getAnt() method has been deprecated"), result.output)
    }

    // test on validation error https://github.com/gradle/gradle/issues/26018
    @TemplateTest("counters", [":koverXmlReport"])
    fun CheckerContext.testNestedTypes() {
        assertFalse("Nested classes validation error. \n Build log:\n $output") {
            output.contains("Nested types are expected to either declare some annotated properties or some behaviour that requires capturing the type as input")
        }
    }
}
