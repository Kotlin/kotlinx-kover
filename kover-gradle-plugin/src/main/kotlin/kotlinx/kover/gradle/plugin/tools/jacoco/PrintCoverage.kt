/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import kotlinx.kover.features.jvm.CoverageValue
import kotlinx.kover.features.jvm.RuleViolations
import kotlinx.kover.gradle.plugin.commons.KoverCriticalException
import kotlinx.kover.gradle.plugin.commons.ReportContext
import kotlinx.kover.gradle.plugin.commons.VerificationBound
import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.tools.CoverageRequest
import kotlinx.kover.gradle.plugin.tools.writeNoSources
import kotlinx.kover.gradle.plugin.tools.writeToFile
import kotlinx.kover.gradle.plugin.util.ONE_HUNDRED
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkQueue
import java.io.File
import java.math.BigDecimal

internal fun ReportContext.printJacocoCoverage(request: CoverageRequest, outputFile: File) {
    val bound = VerificationBound(ONE_HUNDRED, BigDecimal.ZERO, request.metric, request.aggregation)
    // Since JaCoCo doesn't have an API for explicitly obtaining coverage values, we get them indirectly through verification
    val failRule = VerificationRule(true, "", request.entity, listOf(bound))

    val workQueue: WorkQueue = services.workerExecutor.classLoaderIsolation {
        classpath.from(this@printJacocoCoverage.classpath)
    }

    workQueue.submit(JacocoPrintCoverageAction::class.java) {
        this.outputFile.set(outputFile)
        header.convention(request.header)
        lineFormat.convention(request.lineFormat)
        rulesProperty.convention(listOf(failRule))

        fillCommonParameters(this@printJacocoCoverage)
    }
}

internal interface PrintCoverageParameters: AbstractVerifyParameters {
    val outputFile: RegularFileProperty
    val header: Property<String>
    val lineFormat: Property<String>
}

internal abstract class JacocoPrintCoverageAction : AbstractJacocoVerifyAction<PrintCoverageParameters>() {
    override fun processResult(violations: List<RuleViolations>) {
        val outputFile = parameters.outputFile.get().asFile
        if (violations.isEmpty()) {
            outputFile.writeNoSources(parameters.header.orNull)
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
            parameters.header.orNull,
            parameters.lineFormat.get()
        )
    }
}