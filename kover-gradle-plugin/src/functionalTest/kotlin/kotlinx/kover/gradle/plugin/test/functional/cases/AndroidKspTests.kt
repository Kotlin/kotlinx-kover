package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.TemplateTest

internal class AndroidKspTests {
    @TemplateTest("android-ksp", ["koverXmlReport"])
    fun CheckerContext.testUnitTestsAreDisabledInAgp() {

    }
}
