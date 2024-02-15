package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.checkNoAndroidSdk
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.buildFromTemplate
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runWithParams
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

/**
 * Tests on dependency check https://github.com/Kotlin/kotlinx-kover/issues/478.
 *
 * Temporary disabled
 */
@Disabled
class NoDependencyTests {
    @Test
    fun testJvmNotApplied() {
        val buildSource = buildFromTemplate("no-dependency-jvm")
        val build = buildSource.generate()
        val buildResult = build.runWithParams("koverHtmlReport")
        assertFalse(buildResult.isSuccessful)
        assertContains(buildResult.output, "Kover plugin is not applied")
    }

    @Test
    fun testAndroidNotApplied() {
        val buildSource = buildFromTemplate("no-dependency-android")
        val build = buildSource.generate()
        val buildResult = build.runWithParams(":app:koverHtmlReportDebug")
        buildResult.checkNoAndroidSdk()

        assertFalse(buildResult.isSuccessful)
        assertContains(buildResult.output, "Kover plugin is not applied")
    }

    @Test
    fun testAndroidNoVariant() {
        val buildSource = buildFromTemplate("no-dependency-variant-android")
        val build = buildSource.generate()
        val buildResult = build.runWithParams(":app-extra:koverHtmlReportExtra")
        buildResult.checkNoAndroidSdk()

        assertFalse(buildResult.isSuccessful)
        assertContains(buildResult.output, "Kover android variant 'extra' was not matched with any variant from dependency")
    }
}