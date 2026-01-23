/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*

internal class VariantUsageTests {
    @ExamplesTest("android/variantUsage", [":app:koverXmlReportCustom"])
    fun CheckerContext.testAndroidVariantUsage() {
        subproject(":app") {
            xmlReport("custom") {
                // check test tasks
                checkOutcome(":app:testDebugUnitTest", "SUCCESS")
                checkOutcome(":lib:testDebugUnitTest", "SUCCESS")

                // check artifact generation tasks
                checkOutcome(":app:koverGenerateArtifactDebug", "SUCCESS")
                checkOutcome(":app:koverGenerateArtifactCustom", "SUCCESS")
                checkOutcome(":lib:koverGenerateArtifactDebug", "SUCCESS")
                checkOutcome(":lib:koverGenerateArtifactCustom", "SUCCESS")

                classCounter("kotlinx.kover.test.android.DebugUtil").assertFullyCovered()
                classCounter("kotlinx.kover.test.android.lib.DebugUtil").assertFullyCovered()
                classCounter("kotlinx.kover.test.android.lib.DebugLibClass").assertFullyMissed()
                classCounter("kotlinx.kover.test.android.DebugAppClass").assertFullyMissed()
            }
        }
    }

    @ExamplesTest("android/multiplatform", [":koverXmlReportCustom"])
    fun CheckerContext.testMultiplatformVariantUsage() {
        xmlReport("custom") {
            // check test tasks
            checkOutcome(":app:testDebugUnitTest", "SUCCESS")
            checkOutcome(":lib:testAndroidHostTest", "SUCCESS")

            // check artifact generation tasks
            checkOutcome(":lib:koverGenerateArtifactAndroid", "SUCCESS")
            checkOutcome(":app:koverGenerateArtifactDebug", "SUCCESS")
            checkOutcome(":app:koverGenerateArtifactCustom", "SUCCESS")

            // check android classes from :lib
            classCounter("kotlinx.kover.test.android.lib.DebugUtil").assertFullyCovered()
            // check android classes from :app
            classCounter("kotlinx.kover.test.android.DebugUtil").assertFullyCovered()
            classCounter("kotlinx.kover.test.android.DebugAppClass").assertFullyMissed()
            // check JVM classes from :app
            classCounter("kotlinx.kover.test.jvm.JvmClass").assertFullyCovered()
        }
    }

    @ExamplesTest("android/flavors-8", [":app:koverXmlReportCustom"])
    fun CheckerContext.testFlavoursFallbacksAndMissingDimensions() {
        // check test tasks
        checkOutcome(":app:testApp1AppDebugUnitTest", "SUCCESS")
        checkOutcome(":lib:testLib1LibDebugUnitTest", "SUCCESS")

        // check artifact generation tasks
        checkOutcome(":app:koverGenerateArtifactApp1AppDebug", "SUCCESS")
        checkOutcome(":lib:koverGenerateArtifactLib1LibDebug", "SUCCESS")
        checkOutcome(":app:koverGenerateArtifactCustom", "SUCCESS")
    }

}
