/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.engines.intellij

import kotlinx.kover.api.KoverVersions.MINIMAL_INTELLIJ_VERSION
import org.gradle.api.*

internal fun getIntellijDependencies(engineVersion: String): List<String> {
    if (engineVersion.isLowVersion(MINIMAL_INTELLIJ_VERSION)) {
        throw GradleException("IntelliJ engine version $engineVersion is too low, minimal version is $MINIMAL_INTELLIJ_VERSION")
    }
    return listOf(
        "org.jetbrains.intellij.deps:intellij-coverage-agent:$engineVersion",
        "org.jetbrains.intellij.deps:intellij-coverage-reporter:$engineVersion"
    )
}


private fun String.isLowVersion(minimalVersion: String): Boolean {
    val customParts = this.split(".")
    if (customParts.size != 3) {
        throw GradleException("Invalid custom IntelliJ Coverage Engine version '$this'. Expected 'x.x.xxx'")
    }

    val minimalParts = minimalVersion.split(".")
    if (minimalParts.size != 3) {
        throw GradleException("Invalid minimal IntelliJ Coverage Engine version '$this'")
    }

    for (i in 0..2) {
        if (customParts[i] > minimalParts[i]) {
            return false
        } else if (customParts[i] < minimalParts[i]) {
            return true
        }
    }

    return false
}
