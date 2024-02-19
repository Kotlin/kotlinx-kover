package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.checkNoAndroidSdk
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.buildFromTemplate
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runWithParams
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

        // temporarily, this behavior is now allowed, see https://github.com/gradle/gradle/issues/27019
        // build listeners also can't be used because of project isolation https://github.com/Kotlin/kotlinx-kover/issues/513
        assertTrue(buildResult.isSuccessful)
    }

    @Test
    fun testAndroidNotApplied() {
        val buildSource = buildFromTemplate("no-dependency-android")
        val build = buildSource.generate()
        val buildResult = build.runWithParams(":app:koverHtmlReportDebug")
        buildResult.checkNoAndroidSdk()

        // temporarily, this behavior is now allowed, see https://github.com/gradle/gradle/issues/27019
        // build listeners also can't be used because of project isolation https://github.com/Kotlin/kotlinx-kover/issues/513
        assertTrue(buildResult.isSuccessful)
    }

    @Test
    fun testAndroidNoVariant() {
        val buildSource = buildFromTemplate("no-dependency-variant-android")
        val build = buildSource.generate()
        val buildResult = build.runWithParams(":app-extra:koverHtmlReportExtra")
        buildResult.checkNoAndroidSdk()

        assertFalse(buildResult.isSuccessful)
        assertContains(buildResult.output, "Could not resolve all task dependencies for configuration ':app-extra:koverExternalArtifactsExtra'")
    }
}