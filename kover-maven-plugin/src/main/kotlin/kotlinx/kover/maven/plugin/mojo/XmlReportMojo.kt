/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.mojo

import kotlinx.kover.features.jvm.ClassFilters
import kotlinx.kover.features.jvm.KoverLegacyFeatures
import kotlinx.kover.maven.plugin.Constants
import kotlinx.kover.maven.plugin.mojo.abstracts.AbstractCoverageTaskMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

/**
 * Mojo to generate report in JaCoCo XML format.
 */
@Mojo(name = "report-xml", defaultPhase = LifecyclePhase.VERIFY)
class XmlReportMojo: AbstractCoverageTaskMojo() {
    @Parameter(defaultValue = "\${project.reporting.outputDirectory}/${Constants.XML_REPORT_NAME}")
    private lateinit var xmlFile: File

    @Parameter(defaultValue = "\${project.name}")
    private lateinit var title: String

    override fun processCoverage(
        binaryReports: List<File>,
        outputDirs: List<File>,
        sourceDirs: List<File>,
        filters: ClassFilters
    ) {
        KoverLegacyFeatures.generateXmlReport(xmlFile, binaryReports, outputDirs, sourceDirs, title, filters)
    }
}