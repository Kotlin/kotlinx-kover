/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin

import kotlinx.kover.gradle.plugin.appliers.finalizing
import kotlinx.kover.gradle.plugin.appliers.prepare
import kotlinx.kover.gradle.plugin.dsl.KoverVersions.MINIMUM_GRADLE_VERSION
import kotlinx.kover.gradle.plugin.locators.ProvidedVariantsLocator
import kotlinx.kover.gradle.plugin.util.SemVer
import org.gradle.api.*
import org.gradle.api.invocation.*

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

        val context = prepare(target)
        ProvidedVariantsLocator(target) { provided ->
            // this code will be executed in after evaluate stage, after all used plugins are finalized
            context.finalizing(provided)
        }
    }

    /**
     * Check supported Gradle versions.
     */
    private fun Gradle.checkVersion() {
        val current = SemVer.ofVariableOrNull(gradleVersion)!!
        val min = SemVer.ofVariableOrNull(MINIMUM_GRADLE_VERSION)!!
        if (current < min) throw GradleException(
            "Gradle version '$gradleVersion' is not supported by Kover Plugin. " +
                    "Minimal supported version is '$MINIMUM_GRADLE_VERSION'"
        )
    }

}
