/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.commons

import kotlinx.kover.gradle.plugin.tools.*
import org.gradle.configurationcache.extensions.capitalized
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

internal fun htmlReportPath(namespace: String): String {
    return "reports${separator}kover${separator}html${namespace.capitalized()}"
}

internal fun xmlReportPath(namespace: String): String {
    return "reports${separator}kover${separator}report${namespace.capitalized()}.xml"
}

internal fun verificationErrorsPath(namespace: String): String {
    return "reports${separator}kover${separator}verify${namespace.capitalized()}.err"
}

internal fun artifactFilePath(namespace: String): String {
    return if (namespace == DEFAULT_KOVER_NAMESPACE_NAME) {
        "kover${separator}default.artifact"
    } else {
        "kover${separator}$namespace.artifact"
    }
}

private val separator = File.separatorChar
