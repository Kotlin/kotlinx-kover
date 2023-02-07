/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin

import kotlinx.kover.gradle.plugin.appliers.ProjectApplier
import kotlinx.kover.gradle.plugin.dsl.KoverVersions.MINIMUM_GRADLE_VERSION
import kotlinx.kover.gradle.plugin.util.SemVer
import org.gradle.api.*
import org.gradle.api.invocation.*

/**
 * Gradle Plugin for Kover - Kotlin Coverage Tools.
 */
class KoverGradlePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.gradle.checkVersion()

        val applier = ProjectApplier(target)

        applier.onApply()

        target.afterEvaluate {
            applier.onAfterEvaluate()
        }
    }

    private fun Gradle.checkVersion() {
        val current = SemVer.ofVariableOrNull(gradleVersion)!!
        val min = SemVer.ofVariableOrNull(MINIMUM_GRADLE_VERSION)!!
        if (current < min) throw GradleException("Gradle version '$gradleVersion' is not supported by Kover Plugin. " +
                "Minimum supported version is '$MINIMUM_GRADLE_VERSION'")
    }
}
