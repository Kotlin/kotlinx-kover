/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin

import kotlinx.kover.api.*
import kotlinx.kover.gradle.plugin.appliers.ProjectApplier
import kotlinx.kover.gradle.plugin.dsl.KoverVersions.MINIMUM_GRADLE_VERSION
import kotlinx.kover.gradle.plugin.util.SemVer
import org.gradle.api.*
import org.gradle.api.invocation.*
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*

/**
 * Gradle Plugin for JVM Coverage Tools.
 *
 * The main one is Kover - with extended support for language constructs of the Kotlin language.
 */
class KoverGradlePlugin : Plugin<Project> {

    /**
     * Apply plugin to a given project.
     */
    override fun apply(target: Project) {
        target.gradle.checkVersion()

        val applier = ProjectApplier(target)
        applier.onApply()

        target.afterEvaluate {
            applier.onAfterEvaluate()
        }

        target.addDeprecations()
    }

    /**
     * Check supported Gradle versions.
     */
    private fun Gradle.checkVersion() {
        val current = SemVer.ofVariableOrNull(gradleVersion)!!
        val min = SemVer.ofVariableOrNull(MINIMUM_GRADLE_VERSION)!!
        if (current < min) throw GradleException(
            "Gradle version '$gradleVersion' is not supported by Kover Plugin. " +
                    "Minimum supported version is '$MINIMUM_GRADLE_VERSION'"
        )
    }

    @Suppress("DEPRECATION")
    private fun Project.addDeprecations() {

        extensions.create<KoverMergedConfig>("koverMerged")
        tasks.withType<Test>().configureEach {
            this.extensions.create<KoverTaskExtension>("kover")
        }
    }
}
