/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.commons

import kotlinx.kover.gradle.plugin.tools.*
import java.io.File


internal fun agentFilePath(toolVariant: CoverageToolVariant): String {
    return if (toolVariant.vendor == CoverageToolVendor.KOVER) {
        "kover${separator}intellij-coverage-agent-${toolVariant.version}.jar"
    } else {
        "kover${separator}jacoco-coverage-agent-${toolVariant.version}.jar"
    }
}

internal fun rawReportsRootPath() = "kover${separator}raw-reports"

internal fun rawReportPath(taskName: String, toolVendor: CoverageToolVendor): String {
    return "${rawReportsRootPath()}${separator}${rawReportName(taskName, toolVendor)}"
}

internal fun htmlReportPath(variant: String): String {
    return "reports${separator}kover${separator}html${variant.capitalized()}"
}

internal fun xmlReportPath(variant: String): String {
    return "reports${separator}kover${separator}report${variant.capitalized()}.xml"
}

internal fun verificationErrorsPath(variant: String): String {
    return "reports${separator}kover${separator}verify${variant.capitalized()}.err"
}

internal fun artifactFilePath(variant: String): String {
    return if (variant == DEFAULT_KOVER_VARIANT_NAME) {
        "kover${separator}default.artifact"
    } else {
        "kover${separator}$variant.artifact"
    }
}

private val separator = File.separatorChar
