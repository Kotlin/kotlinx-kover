/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.BuildConfigurator
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.ExamplesTest
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.GeneratedTest
import kotlin.test.*

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

    /**
     * Check that Kover artifact files are not resolved during the task dependency tree construction process.
     *
     * The task tree is built at the configuration stage, so getting artifacts from dependencies can lead to premature task launches, deadlocks, and performance degradation.
     *
     * If a resolution is detected during configuration, the message "Configuration 'koverExternalArtifactsRelease' was resolved during configuration time" is printed.
     * This message may be changed in future versions, so it's worth double-checking for this error.
     */
    @ExamplesTest("android/multiproject", [":app:koverXmlReportRelease"])
    fun CheckerContext.testResolveConfigurationInExecuteTime() {
        assertFalse(
            output.contains("Configuration 'koverExternalArtifactsRelease' was resolved during configuration time"),
            "Kover Configuration was resolved during configuration time"
        )
        assertFalse(
            output.contains("This is a build performance and scalability issue"),
            "Some Configuration was resolved during configuration time, perhaps this is the Kover Configuration"
        )
    }
}