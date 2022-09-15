/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.cases

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.framework.checker.*
import kotlinx.kover.test.functional.framework.common.recentKoverVersion
import kotlinx.kover.test.functional.framework.starter.*
import kotlin.test.*


internal class ExamplesTests {
    @ExamplesTest
    fun CheckerContext.buildAndCheckVersions() {
        allProjects {
            // check version of Kover plugin if applied
            if (definedKoverVersion != null) {
                assertEquals(definedKoverVersion, recentKoverVersion)
            }

            // check version of engine, it should be default
            if (engine.vendor == CoverageEngineVendor.INTELLIJ) {
                assertEquals(engine.version, KoverVersions.DEFAULT_INTELLIJ_VERSION)
            } else {
                assertEquals(engine.version, KoverVersions.DEFAULT_JACOCO_VERSION)
            }
        }
    }
}
