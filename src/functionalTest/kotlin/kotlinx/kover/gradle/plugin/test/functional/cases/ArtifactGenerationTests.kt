/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.GeneratedTest

internal class ArtifactGenerationTests {

    @GeneratedTest
    fun BuildConfigurator.testAssemble() {
        // generation of Kover artifacts should be disabled on `assemble`
        // fix of https://github.com/Kotlin/kotlinx-kover/issues/353
        addProjectWithKover {
            sourcesFrom("simple")
        }

        run("assemble") {
            taskNotCalled("test")
            taskNotCalled("koverGenerateArtifact")
        }
    }
}