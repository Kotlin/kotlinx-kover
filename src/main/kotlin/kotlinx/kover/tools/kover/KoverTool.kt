/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.tools.kover

import kotlinx.kover.api.KoverVersions.MINIMAL_KOVER_TOOL_VERSION
import kotlinx.kover.util.SemVer
import org.gradle.api.*

internal fun getKoverDependencies(toolVersion: String): List<String> {
    if (toolVersion.isLowVersion(MINIMAL_KOVER_TOOL_VERSION)) {
        throw GradleException("Kover tool version $toolVersion is too low, minimal version is $MINIMAL_KOVER_TOOL_VERSION")
    }
    return listOf(
        "org.jetbrains.intellij.deps:intellij-coverage-agent:$toolVersion",
        "org.jetbrains.intellij.deps:intellij-coverage-reporter:$toolVersion"
    )
}


private fun String.isLowVersion(minimalVersion: String): Boolean {
    val custom = SemVer.ofThreePartOrNull(this)
        ?: throw GradleException("Invalid custom Kover Coverage Tool version '$this'. Expected 'x.x.xxx'")

    val min = SemVer.ofThreePartOrNull(minimalVersion)
        ?: throw GradleException("Invalid minimal Kover Coverage Tool version '$this'")

    return custom < min
}
