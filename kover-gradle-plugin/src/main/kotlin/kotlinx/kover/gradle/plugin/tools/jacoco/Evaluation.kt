/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import kotlinx.kover.features.jvm.KoverLegacyFeatures.CoverageValue
import kotlinx.kover.gradle.plugin.commons.KoverCriticalException
import kotlinx.kover.gradle.plugin.commons.ReportContext
import kotlinx.kover.gradle.plugin.commons.VerificationBound
import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.tools.*
import kotlinx.kover.gradle.plugin.tools.CoverageRequest
import kotlinx.kover.gradle.plugin.tools.writeToFile
import kotlinx.kover.gradle.plugin.util.ONE_HUNDRED
import java.io.File
import java.math.BigDecimal

internal fun ReportContext.printJacocoCoverage(request: CoverageRequest, outputFile: File) {
    val bound = VerificationBound(ONE_HUNDRED, BigDecimal.ZERO, request.metric, request.aggregation)
    val failRule = VerificationRule(true, "", request.entity, listOf(bound))

    val violations = doJacocoVerify(listOf(failRule))
    if (violations.isEmpty()) {
        outputFile.writeNoSources(request.header)
        return
    }

    val values = violations.flatMap { rule ->
        if (rule.violations.isEmpty()) {
            throw KoverCriticalException("Expected at least one bound violation for JaCoCo")
        }
        rule.violations.map {
            CoverageValue(it.entityName, it.value)
        }
    }

    values.writeToFile(
        outputFile,
        request.header,
        request.lineFormat
    )
}