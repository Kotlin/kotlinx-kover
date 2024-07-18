/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.mojo

import kotlinx.kover.features.jvm.*
import kotlinx.kover.maven.plugin.MavenBound
import kotlinx.kover.maven.plugin.MavenRule
import kotlinx.kover.maven.plugin.MavenRuleFilters
import kotlinx.kover.maven.plugin.mojo.abstracts.AbstractCoverageTaskMojo
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

/**
 * Mojo to verify coverage by specified rules.
 */
@Mojo(name = "verify", defaultPhase = LifecyclePhase.VERIFY)
class VerifyMojo : AbstractCoverageTaskMojo() {
    @Parameter(property = "kover.warningInsteadOfFailure", defaultValue = "false", required = true)
    private var warningInsteadOfFailure: Boolean = false

    @Parameter
    private val rules: MutableList<MavenRule> = mutableListOf()

    override fun processCoverage(
        binaryReports: List<File>,
        outputDirs: List<File>,
        sourceDirs: List<File>,
        filters: ClassFilters
    ) {
        if (rules.isEmpty()) {
            log.info("No Kover verification rules")
            return
        }

        val tempDir = tempDirectory()

        val violations = mutableListOf<RuleViolations>()
        // verify rules with filters inherited from mojo
        val rulesWithDefaultFilters = rules.filter { filter -> filter.filters == null }
        violations += KoverLegacyFeatures.verify(
            rulesWithDefaultFilters.map { it.convert() },
            tempDir,
            filters,
            binaryReports,
            outputDirs
        )

        // verify rules with own filters
        rules.filter { filter -> filter.filters != null }.forEach { rule ->
            violations += KoverLegacyFeatures.verify(
                listOf(rule.convert()),
                tempDir,
                rule.filters!!.convert(),
                binaryReports,
                outputDirs
            )
        }

        if (violations.isEmpty()) {
            log.info("Coverage rule checks passed successfully")
        } else {
            val message = "Kover Verification Error\n" + KoverLegacyFeatures.violationMessage(violations)
            if (warningInsteadOfFailure) {
                log.warn(message)
            } else {
                throw MojoFailureException(message)
            }
        }
    }

    private fun MavenRule.convert(): Rule {
        return Rule(name, groupBy, bounds.map { it.convert() })
    }

    private fun MavenBound.convert(): Bound {
        return Bound(minValue?.toBigDecimal(), maxValue?.toBigDecimal(), coverageUnits, aggregationForGroup)
    }

    private fun MavenRuleFilters.convert(): ClassFilters {
        return ClassFilters(
            includes?.classes?.toSet() ?: emptySet(),
            excludes?.classes?.toSet() ?: emptySet(),
            includes?.annotatedBy?.toSet() ?: emptySet(),
            excludes?.annotatedBy?.toSet() ?: emptySet(),
            includes?.inheritedFrom?.toSet() ?: emptySet(),
            excludes?.inheritedFrom?.toSet() ?: emptySet()
        )
    }
}