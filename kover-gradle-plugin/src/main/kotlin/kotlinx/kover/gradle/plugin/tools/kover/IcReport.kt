package kotlinx.kover.gradle.plugin.tools.kover

import kotlinx.kover.features.jvm.KoverLegacyFeatures
import kotlinx.kover.gradle.plugin.commons.ReportContext
import java.io.File


internal fun ReportContext.koverBinaryReport(binaryFile: File) {
    KoverLegacyFeatures.aggregateIc(
        binaryFile,
        filters.toKoverFeatures(),
        tempDir,
        files.reports.toList(),
        files.outputs.toList()
    )
}

