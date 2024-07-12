/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.mojo

import kotlinx.kover.features.jvm.*
import kotlinx.kover.maven.plugin.Constants
import kotlinx.kover.maven.plugin.mojo.abstracts.AbstractCoverageTaskMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

/**
 * Mojo to export coverage report in IC (intellij agent) format.
 */
@Mojo(name = "report-ic", defaultPhase = LifecyclePhase.VERIFY)
class IcReportMojo : AbstractCoverageTaskMojo() {
    @Parameter(defaultValue = "\${project.reporting.outputDirectory}/${Constants.KOVER_IC_REPORT_NAME}")
    private lateinit var icFile: File

    override fun processCoverage(
        binaryReports: List<File>,
        outputDirs: List<File>,
        sourceDirs: List<File>,
        filters: ClassFilters
    ) {
        KoverLegacyFeatures.aggregateIc(
            icFile,
            filters,
            tempDirectory(),
            binaryReports,
            outputDirs
        )
    }
}