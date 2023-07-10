package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.TemplateTest

internal class AndroidConfigsTests {
    @TemplateTest("disabledUnitTests", [":app:koverXmlReportDebug"])
    fun CheckerContext.testUnitTestsAreDisabledInAgp() {
        subproject(":app") {
            taskNotCalled("testDebugUnitTest")
            checkOutcome("koverXmlReportDebug", "SUCCESS")
            xmlReport("debug") {
                classCounter("kotlinx.kover.test.android.Maths").assertFullyMissed()
            }
        }
    }
}
