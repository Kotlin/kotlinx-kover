package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.TemplateTest

internal class MetadataCompatibilityTests {

    @TemplateTest("buildsrc-usage", [":koverXmlReport"])
    fun CheckerContext.test() {
        // no-op
    }
}