/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.mojo

import kotlinx.kover.features.jvm.ClassFilters
import kotlinx.kover.features.jvm.KoverLegacyFeatures
import kotlinx.kover.maven.plugin.Constants
import kotlinx.kover.maven.plugin.Constants.KOVER_REPORTS_PATH
import kotlinx.kover.maven.plugin.mojo.abstracts.AbstractReportTaskMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File
import java.net.URI
import java.util.*

/**
 * Mojo to generate Kover HTML report.
 */
@Mojo(name = "report-html", defaultPhase = LifecyclePhase.VERIFY)
class HtmReportMojo: AbstractReportTaskMojo() {
    @Parameter(defaultValue = Constants.HTML_REPORT_DIR_NAME)
    private lateinit var htmlDirName: String

    @Parameter(defaultValue = "\${project.name}")
    private lateinit var title: String

    @Parameter(defaultValue = "UTF-8")
    private lateinit var charset: String

    override fun processCoverage(
        binaryReports: List<File>,
        outputDirs: List<File>,
        sourceDirs: List<File>,
        filters: ClassFilters
    ) {
        val htmlDir = reportOutputDirectory.resolve(htmlDirName)
        KoverLegacyFeatures.generateHtmlReport(htmlDir, charset, binaryReports, outputDirs, sourceDirs, title, filters)

        val clickablePath = URI(
            "file",
            "",
            File(htmlDir.canonicalPath, "index.html").toURI().path,
            null,
            null,
        ).toASCIIString()

        log.info("Kover: HTML report for '${project.name}' $clickablePath")
    }

    override fun getOutputName(): String = KOVER_REPORTS_PATH + "/" + Constants.HTML_REPORT_DIR_NAME + "/index"

    override fun getName(locale: Locale?): String {
        return "Coverage report"
    }

    override fun getDescription(locale: Locale?): String {
        return "Project coverage report produced by Kover"
    }
}