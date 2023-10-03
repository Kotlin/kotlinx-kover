/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import kotlinx.kover.gradle.plugin.commons.KoverCriticalException
import kotlinx.kover.gradle.plugin.commons.ReportContext
import kotlinx.kover.gradle.plugin.commons.VerificationBound
import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.tools.*
import kotlinx.kover.gradle.plugin.tools.CoverageMeasures
import kotlinx.kover.gradle.plugin.tools.CoverageRequest
import kotlinx.kover.gradle.plugin.tools.CoverageValue
import kotlinx.kover.gradle.plugin.tools.writeToFile
import kotlinx.kover.gradle.plugin.util.ONE_HUNDRED
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import java.io.File
import java.math.BigDecimal


internal fun ReportContext.printCoverage(request: CoverageRequest, outputFile: File) {
    submitAction<CollectCoverageAction, CollectCoverageParameters> {
        this.outputFile.set(outputFile)
        this.request.convention(request)

        filters.convention(this@printCoverage.filters)
        files.convention(this@printCoverage.files)
        tempDir.set(this@printCoverage.tempDir)
        projectPath.convention(this@printCoverage.projectPath)
    }
}


internal abstract class CollectCoverageAction : AbstractReportAction<CollectCoverageParameters>() {
    override fun generate() {
        val request = parameters.request.get()
        val bound = VerificationBound(ONE_HUNDRED, BigDecimal.ZERO, request.metric, request.aggregation)
        val failRule = VerificationRule(true, null, null, request.entity, listOf(bound))

        // change API after https://youtrack.jetbrains.com/issue/IDEA-323463 will be implemented
        val violations = koverVerify(
            listOf(failRule),
            parameters.filters.get(),
            parameters.tempDir.get().asFile,
            parameters.files.get()
        )

        if (violations.isEmpty()) {
            parameters.outputFile.get().asFile.writeNoSources(parameters.request.get().header)
            return
        }

        val violation = violations.singleOrNull() ?: throw KoverCriticalException("Expected only one rule violation for Kover")
        if (violation.bounds.isEmpty()) {
            throw KoverCriticalException("Expected at least one bound violation for Kover")
        }

        val values = violation.bounds.map {
            CoverageValue(it.actualValue, it.entityName)
        }
        CoverageMeasures(values).writeToFile(
            parameters.outputFile.get().asFile,
            parameters.request.get().header,
            parameters.request.get().lineFormat
        )
    }
}

internal interface CollectCoverageParameters : ReportParameters {
    val outputFile: RegularFileProperty
    val request: Property<CoverageRequest>
}
