/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.commons

import java.io.File



internal fun agentLinkFilePath() = "kover${separator}agent-jar.file"

internal fun rawReportsRootPath() = "kover${separator}raw-reports"

internal fun rawReportPath(taskName: String, toolVendor: CoverageToolVendor): String {
    return "${rawReportsRootPath()}/${rawReportName(taskName, toolVendor)}"
}

internal fun htmlReportPath(setupId: SetupId): String {
    return "reports${separator}kover${separator}html${setupId.capitalized}"
}

internal fun xmlReportPath(setupId: SetupId): String {
    return "reports${separator}kover${separator}report${setupId.capitalized}.xml"
}

internal fun verificationErrorsPath(setupId: SetupId): String {
    return "reports${separator}kover${separator}verify${setupId.capitalized}.err"
}

internal fun setupArtifactFile(setupId: SetupId) = "kover${separator}setup${setupId.capitalized}.files"

private val separator = File.separatorChar

