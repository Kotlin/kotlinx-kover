package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.TemplateTest
import kotlin.test.assertFalse

internal class GradleValidationTests {

    // test on validation error https://github.com/gradle/gradle/issues/26018
    @TemplateTest("counters", [":koverXmlReport"])
    fun CheckerContext.testNestedTypes() {
        assertFalse("Nested classes validation error. \n Build log:\n $output") {
            output.contains("Nested types are expected to either declare some annotated properties or some behaviour that requires capturing the type as input")
        }
    }
}