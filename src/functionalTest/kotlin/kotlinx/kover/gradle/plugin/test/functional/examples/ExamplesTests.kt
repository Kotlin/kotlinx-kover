/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.examples

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.*
import kotlinx.kover.gradle.plugin.test.functional.framework.common.releaseVersion
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*
import kotlin.test.*


internal class ExamplesTests {
    @ExamplesTest
    fun CheckerContext.buildAndCheckVersions() {
        allProjects {
            // check version of Kover plugin if applied
            if (definedKoverVersion != null) {
                assertEquals(releaseVersion, definedKoverVersion)
            }

            // check version of tool, it should be default
            if (toolVariant.vendor == CoverageToolVendor.KOVER) {
                assertEquals(toolVariant.version, KoverVersions.KOVER_TOOL_DEFAULT_VERSION)
            } else {
                assertEquals(toolVariant.version, KoverVersions.JACOCO_TOOL_DEFAULT_VERSION)
            }
        }
    }
}
