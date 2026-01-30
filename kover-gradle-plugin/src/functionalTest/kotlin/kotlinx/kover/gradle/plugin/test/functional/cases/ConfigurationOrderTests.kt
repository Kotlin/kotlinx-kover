/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.checkNoAndroidSdk
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
    @TemplateTest("android-inverse-order", [":app:koverXmlReportCustom", ":app:koverXmlReportRelease"])
    fun CheckerContext.testAndroidInverseOrder() {
        subproject(":app") {
            checkXmlReport("custom")
            checkXmlReport("release")
            checkOutcome(":app:koverXmlReportCustom", "SUCCESS")
            checkOutcome(":app:koverXmlReportRelease", "SUCCESS")
        }
    }

    @TemplateTest("android-inverse-order-8", [":app:koverXmlReportCustom", ":app:koverXmlReportRelease"])
    fun CheckerContext.testAndroidInverseOrderBefore9() {
        subproject(":app") {
            checkXmlReport("custom")
            checkXmlReport("release")
            checkOutcome(":app:koverXmlReportCustom", "SUCCESS")
            checkOutcome(":app:koverXmlReportRelease", "SUCCESS")
        }
    }

    /**
     * A test to verify that the order of application of the Kover plugin does not affect the correct operation.
     * Kover + Kotlin Multiplatform Plugin with Android target
     */
    @TemplateTest("android-mpp-inverse-order", [":koverXmlReportCustom", ":koverXmlReportRelease"])
    fun CheckerContext.testAndroidMppInverseOrder() {
        checkXmlReport("custom")
        checkXmlReport("release")
        checkOutcome(":koverXmlReportCustom", "SUCCESS")
        checkOutcome(":koverXmlReportRelease", "SUCCESS")
    }

    @Test
    fun testIllegalVariantNameInConfig() {
        val buildSource = buildFromTemplate("android-no-variant-for-config")
        val build = buildSource.generate()
        val buildResult = build.runWithParams("clean")

        buildResult.checkNoAndroidSdk()
        assertFalse(buildResult.isSuccessful, "Build must fall")
        assertContains(buildResult.output, "variant because it does not exist")
    }

    @Test
    fun testIllegalVariantNameInConfigBefore9() {
        val buildSource = buildFromTemplate("android-no-variant-for-config-8")
        val build = buildSource.generate()
        val buildResult = build.runWithParams("clean")

        buildResult.checkNoAndroidSdk()
        assertFalse(buildResult.isSuccessful, "Build must fall")
        assertContains(buildResult.output, "variant because it does not exist")
    }

    @Test
    fun testIllegalVariantNameInMerge() {
        val buildSource = buildFromTemplate("android-no-variant-for-merge")
        val build = buildSource.generate()
        val buildResult = build.runWithParams("clean")

        buildResult.checkNoAndroidSdk()
        assertFalse(buildResult.isSuccessful, "Build must fall")
        assertContains(buildResult.output, "Could not find the provided variant")
    }

    @Test
    fun testIllegalVariantNameInMergeBefore9() {
        val buildSource = buildFromTemplate("android-no-variant-for-merge-8")
        val build = buildSource.generate()
        val buildResult = build.runWithParams("clean")

        buildResult.checkNoAndroidSdk()
        assertFalse(buildResult.isSuccessful, "Build must fall")
        assertContains(buildResult.output, "Could not find the provided variant")
    }

}