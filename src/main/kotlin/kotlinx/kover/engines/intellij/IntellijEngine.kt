/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.engines.intellij

import kotlinx.kover.api.KoverVersions.MINIMAL_INTELLIJ_VERSION
import kotlinx.kover.util.SemVer
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
    val custom = SemVer.ofThreePartOrNull(this)
        ?: throw GradleException("Invalid custom IntelliJ Coverage Engine version '$this'. Expected 'x.x.xxx'")

    val min = SemVer.ofThreePartOrNull(minimalVersion)
        ?: throw GradleException("Invalid minimal IntelliJ Coverage Engine version '$this'")

    return custom < min
}
