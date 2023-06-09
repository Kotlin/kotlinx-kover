/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.buildFromTemplate
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.generateBuild
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runWithParams
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.TemplateTest
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class ConfigurationOrderTests {
    /**
     * A test to verify that the order of application of the Kover plugin does not affect the correct operation.
     * Kover + Kotlin Android Plugin
     */
    @TemplateTest("android-inverse-order", [":app:koverXmlReport", ":app:koverXmlReportRelease"])
    fun CheckerContext.testAndroidInverseOrder() {
        subproject(":app") {
            checkXmlReport()
            checkXmlReport("release")
            checkOutcome(":app:koverXmlReport", "SUCCESS")
            checkOutcome(":app:koverXmlReportRelease", "SUCCESS")
        }
    }

    /**
     * A test to verify that the order of application of the Kover plugin does not affect the correct operation.
     * Kover + Kotlin Multiplatform Plugin with Android target
     */
    @TemplateTest("android-mpp-inverse-order", [":koverXmlReport", ":koverXmlReportRelease"])
    fun CheckerContext.testAndroidMppInverseOrder() {
        checkXmlReport()
        checkXmlReport("release")
        checkOutcome(":koverXmlReport", "SUCCESS")
        checkOutcome(":koverXmlReportRelease", "SUCCESS")
    }

    @Test
    fun testIllegalVariantNameInConfig() {
        val buildSource = buildFromTemplate("android-no-variant-for-config")
        val build = buildSource.generate("No variant config", "template")
        val buildResult = build.runWithParams("clean")

        assertFalse(buildResult.isSuccessful, "Build must fall")
        assertContains(buildResult.output, "impossible to configure Android reports for it")
    }

    @Test
    fun testIllegalVariantNameInMerge() {
        val buildSource = buildFromTemplate("android-no-variant-for-merge")
        val build = buildSource.generate("No variant merge", "template")
        val buildResult = build.runWithParams("clean")

        assertFalse(buildResult.isSuccessful, "Build must fall")
        assertContains(buildResult.output, "impossible to merge default reports with its measurements")
    }

}