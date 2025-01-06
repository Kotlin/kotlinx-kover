/*
 * Copyright 2017-2025 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.GeneratedTest
import kotlin.test.assertFalse

internal class CompatibilityTests {

    @GeneratedTest
    fun BuildConfigurator.testGradle9Compatibility() {
        addProjectWithKover {
            sourcesFrom("simple")
        }

        run("test", "--warning-mode", "all") {
            assertFalse(output.contains("Deprecated Gradle features were used in this build"), "There should be no deprecated Gradle features")
        }

    }
}