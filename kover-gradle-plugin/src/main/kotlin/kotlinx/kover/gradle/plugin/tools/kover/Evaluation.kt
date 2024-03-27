/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import kotlinx.kover.features.jvm.KoverLegacyFeatures
import kotlinx.kover.gradle.plugin.commons.ReportContext
import kotlinx.kover.gradle.plugin.tools.CoverageRequest
import kotlinx.kover.gradle.plugin.tools.writeNoSources
import kotlinx.kover.gradle.plugin.tools.writeToFile
import java.io.File

internal fun ReportContext.printCoverage(request: CoverageRequest, outputFile: File) {
    // change API after https://youtrack.jetbrains.com/issue/IDEA-323463 will be implemented
    val coverage = KoverLegacyFeatures.evalCoverage(
        request.entity.convert(),
        request.metric.convert(),
        request.aggregation.convert(),
        tempDir,
        filters.toKoverFeatures(),
        files.reports.toList(),
        files.outputs.toList()
    )

    if (coverage.isEmpty()) {
        outputFile.writeNoSources(request.header)
        return
    }
    coverage.writeToFile(
        outputFile,
        request.header,
        request.lineFormat
    )
}

