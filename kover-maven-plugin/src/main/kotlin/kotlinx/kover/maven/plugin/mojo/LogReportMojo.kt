/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.mojo

import kotlinx.kover.features.jvm.*
import kotlinx.kover.maven.plugin.mojo.abstracts.AbstractCoverageTaskMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

/**
 * Mojo to print coverage info to the log.
 */
@Mojo(name = "log", defaultPhase = LifecyclePhase.VERIFY)
class LogReportMojo : AbstractCoverageTaskMojo() {
    @Parameter(defaultValue = "APPLICATION")
    private lateinit var logGroupBy: GroupingBy

    @Parameter(defaultValue = "LINE")
    private lateinit var logCoverageUnit: CoverageUnit

    @Parameter(defaultValue = "COVERED_PERCENTAGE")
    private lateinit var logAggregationForGroup: AggregationType

    @Parameter(defaultValue = "{entity} line coverage: {value}%")
    private lateinit var logFormat: String

    override fun processCoverage(
        binaryReports: List<File>,
        outputDirs: List<File>,
        sourceDirs: List<File>,
        filters: ClassFilters
    ) {
        val coverageValues = KoverLegacyFeatures.evalCoverage(
            logGroupBy,
            logCoverageUnit,
            logAggregationForGroup,
            tempDirectory(),
            filters,
            binaryReports,
            outputDirs
        )

        coverageValues.forEach { coverageValue ->
            val entityName = coverageValue.entityName ?: "application"
            log.info(
                logFormat.replace("{value}", coverageValue.value.stripTrailingZeros().toPlainString())
                    .replace("{entity}", entityName)
            )
        }
    }
}