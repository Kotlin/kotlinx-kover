package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.TemplateTest

internal class FinalizingEnqueueTests {
    @TemplateTest("android-subproject-apply", [":app:koverHtmlReportDebug"])
    fun CheckerContext.testPluginsOrder() {
        // if Kotlin Gradle Plugin is applied before any Android Gradle Plugin and Kover is applied from `subprojects` block, then `Attempt to queue after finalizing` error occurs
        // see https://github.com/Kotlin/kotlinx-kover/issues/610
    }
}
