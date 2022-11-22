/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover

import kotlinx.kover.api.KoverVersions.MINIMUM_GRADLE_VERSION
import kotlinx.kover.appliers.*
import kotlinx.kover.util.*
import org.gradle.api.*
import org.gradle.api.invocation.*

class KoverPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.gradle.checkVersion()
        target.applyToProject()
        target.applyMerged()
    }

    private fun Gradle.checkVersion() {
        val current = SemVer.ofVariableOrNull(gradleVersion)!!
        val min = SemVer.ofVariableOrNull(MINIMUM_GRADLE_VERSION)!!
        if (current < min) throw GradleException("Gradle version '$gradleVersion' is not supported by Kover Plugin. " +
                "Minimum supported version is '$MINIMUM_GRADLE_VERSION'")

    }
}
