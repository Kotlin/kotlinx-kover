package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.defaultXmlReport
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.TemplateTest

internal class CompilationFiltersTests {
    @TemplateTest("sourcesets", ["koverXmlReport"])
    fun CheckerContext.testJvmSourceSetFilter() {
        xml(defaultXmlReport()) {
            classCounter("kotlinx.kover.examples.sourcesets.ExtraClass").assertAbsent()
        }
    }

    @TemplateTest("sourcesets-mpp", ["koverXmlReport"])
    fun CheckerContext.testMppSourceSetFilter() {
        xml(defaultXmlReport()) {
            classCounter("kotlinx.kover.examples.sourcesets.ExtraClass").assertAbsent()
        }
    }

}