/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.TemplateTest

internal class ClassDupTest {
    /**
     * Checking that if there are duplicate files in different versions, the JaCoCo report still continues to be generated.
     * This behavior is identical to the behavior in Kover reporter.
     */
    @TemplateTest("android-class-dup", [":app:koverXmlReport"])
    fun CheckerContext.test() {
        subproject(":app") {
            xmlReport {
                // class present in report
                classCounter("kotlinx.kover.test.android.DupClass").assertFullyMissed()
            }
        }
    }

}