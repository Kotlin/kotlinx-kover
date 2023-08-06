package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.ExamplesTest

internal class DynamicFeatureTests {
    @ExamplesTest("android/dynamic", commands = [":dyn:koverXmlReportRelease"])
    fun CheckerContext.test() {
        subproject("dyn") {
            xmlReport("release") {
                classCounter("kotlinx.kover.test.android.DebugUtil").assertAbsent()
                methodCounter("kotlinx.kover.test.android.Maths", "sum").assertCovered()
                methodCounter("kotlinx.kover.test.android.dyn.MagicFactory", "generate").assertCovered()
            }
        }
    }
}