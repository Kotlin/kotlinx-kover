/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import kotlinx.kover.features.jvm.KoverLegacyFeatures
import kotlinx.kover.gradle.plugin.commons.ReportContext
import java.io.File


internal fun ReportContext.koverHtmlReport(htmlReportDir: File, htmlTitle: String, charsetName: String?) {
    htmlReportDir.mkdirs()

    KoverLegacyFeatures.generateHtmlReport(
        htmlReportDir,
        charsetName,
        files.reports.toList(),
        files.outputs.toList(),
        files.sources.toList(),
        htmlTitle,
        filters.toKoverFeatures()
    )
}

internal fun ReportContext.koverXmlReport(xmlReportFile: File, xmlTitle: String) {
    KoverLegacyFeatures.generateXmlReport(
        xmlReportFile,
        files.reports.toList(),
        files.outputs.toList(),
        files.sources.toList(),
        xmlTitle,
        filters.toKoverFeatures()
    )
}
