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
     * Kover generation tasks compute a cache key based on the content of the relative paths
     * in their input field. If there are no additional inputs defiend that differentiate task input
     * between projects then the cache key of the generate tasks in one project is the same as the cache key
     * in other projects.
     */
    @GeneratedTest
    fun BuildConfigurator.testBuildCacheEntriesAreNotReusedAmongEmptyProjects() {
        addProjectWithKover { }
        addProjectWithKover(":project-a", "project-a") {}
        addProjectWithKover(":project-b", "project-b") {}
        run("check", "--build-cache") {}
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