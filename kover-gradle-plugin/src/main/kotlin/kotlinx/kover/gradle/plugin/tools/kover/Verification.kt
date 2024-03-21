/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import kotlinx.kover.features.jvm.KoverLegacyFeatures
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.tools.generateErrorMessage
import java.io.File

internal fun ReportContext.koverVerify(specifiedRules: List<VerificationRule>, outputReportFile: File) {
    val violations = KoverLegacyFeatures.verify(
        specifiedRules.map { it.convert() },
        tempDir,
        filters.toKoverFeatures(),
        files.reports.toList(),
        files.outputs.toList()
    )

    val errorMessage = generateErrorMessage(violations)
    outputReportFile.writeText(errorMessage)

    if (violations.isNotEmpty()) {
        throw KoverVerificationException(errorMessage)
    }
}
