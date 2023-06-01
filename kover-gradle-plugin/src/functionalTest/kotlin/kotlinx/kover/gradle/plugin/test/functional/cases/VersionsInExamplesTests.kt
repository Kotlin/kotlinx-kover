/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.*
import kotlinx.kover.gradle.plugin.test.functional.framework.common.releaseVersion
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue


internal class VersionsInExamplesTests {
    @Test
    fun testVersions() {
        val examplesWithWrongVersions = allExamples().filterNot { it.analyzeProject().checkVersionsInBuild() }
        assertTrue(examplesWithWrongVersions.isEmpty(), "Incorrect Kover versions in projects: $examplesWithWrongVersions")
    }

    private fun ProjectAnalysisData.checkVersionsInBuild(): Boolean {
        return allProjects().all { checkVersionsInProject() }
    }

    private fun ProjectAnalysisData.checkVersionsInProject(): Boolean {
        // check version of Kover plugin if applied
        if (definedKoverVersion != null) {
            if (releaseVersion != definedKoverVersion) {
                return false
            }
        }

        // check version of tool, it should be default
        return !(toolVariant.vendor == CoverageToolVendor.JACOCO && toolVariant.version != KoverVersions.JACOCO_TOOL_DEFAULT_VERSION)
    }
}
