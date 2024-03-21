/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import groovy.lang.GroovyObject
import kotlinx.kover.features.jvm.KoverLegacyFeatures.Bound
import kotlinx.kover.features.jvm.KoverLegacyFeatures.BoundViolation
import kotlinx.kover.features.jvm.KoverLegacyFeatures.Rule
import kotlinx.kover.features.jvm.KoverLegacyFeatures.RuleViolations
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.tools.generateErrorMessage
import kotlinx.kover.gradle.plugin.tools.kover.convert
import kotlinx.kover.gradle.plugin.util.ONE_HUNDRED
import org.gradle.internal.reflect.JavaMethod
import java.io.File
import java.math.BigDecimal
import java.util.*


internal fun ReportContext.jacocoVerify(
    rules: List<VerificationRule>,
    outputFile: File
) {
    val violations = doJacocoVerify(rules)

    val errorMessage = generateErrorMessage(violations)
    outputFile.writeText(errorMessage)

    if (violations.isNotEmpty()) {
        throw KoverVerificationException(errorMessage)
    }
}


internal fun ReportContext.doJacocoVerify(rules: List<VerificationRule>): List<RuleViolations> {

    callAntReport(projectPath) {
        invokeWithBody("check", mapOf("failonviolation" to "false", "violationsproperty" to "jacocoErrors")) {
            rules.forEach {
                val entityType = when (it.entityType) {
                    GroupingEntityType.APPLICATION -> "BUNDLE"
                    GroupingEntityType.CLASS -> "CLASS"
                    GroupingEntityType.PACKAGE -> "PACKAGE"
                }
                invokeWithBody("rule", mapOf("element" to entityType)) {
                    it.bounds.forEach { b ->
                        val limitArgs = mutableMapOf<String, String>()
                        limitArgs["counter"] = when (b.metric) {
                            CoverageUnit.LINE -> "LINE"
                            CoverageUnit.INSTRUCTION -> "INSTRUCTION"
                            CoverageUnit.BRANCH -> "BRANCH"
                        }

                        var min: BigDecimal? = b.minValue
                        var max: BigDecimal? = b.maxValue
                        when (b.aggregation) {
                            AggregationType.COVERED_COUNT -> {
                                limitArgs["value"] = "COVEREDCOUNT"
                            }

                            AggregationType.MISSED_COUNT -> {
                                limitArgs["value"] = "MISSEDCOUNT"
                            }

                            AggregationType.COVERED_PERCENTAGE -> {
                                limitArgs["value"] = "COVEREDRATIO"
                                min = min?.divide(ONE_HUNDRED)?.setScale(4)
                                max = max?.divide(ONE_HUNDRED)?.setScale(4)
                            }

                            AggregationType.MISSED_PERCENTAGE -> {
                                limitArgs["value"] = "MISSEDRATIO"
                                min = min?.divide(ONE_HUNDRED)?.setScale(4)
                                max = max?.divide(ONE_HUNDRED)?.setScale(4)
                            }
                        }

                        if (min != null) {
                            limitArgs["minimum"] = min.toPlainString()
                        }

                        if (max != null) {
                            limitArgs["maximum"] = max.toPlainString()
                        }
                        invokeMethod("limit", limitArgs)
                    }
                }
            }
        }
    }

    return services.antBuilder.violations()
}


private val errorMessageRegex =
    "Rule violated for (\\w+) (.+): (\\w+) (.+) is ([\\d\\.]+), but expected (\\w+) is ([\\d\\.]+)".toRegex()

private fun GroovyObject.violations(): List<RuleViolations> {
    val project = getProperty("project")
    val properties = JavaMethod.of(
        project,
        Hashtable::class.java, "getProperties"
    ).invoke(project, *arrayOfNulls(0))
    val allErrorsString = properties["jacocoErrors"] as String? ?: return emptyList()

    // sorting lines to get a stable order of errors
    return allErrorsString.lines().sorted().map {
        val match = errorMessageRegex.find(it)
            ?: throw KoverCriticalException("Can't parse JaCoCo verification error string:\n$it")

        val entityType = match.groupValues[1].asEntityType(it)
        val entityName = match.groupValues[2].run { if (this == ":") null else this }
        val coverageUnits = match.groupValues[3].asCoverageUnit(it)
        val agg = match.groupValues[4].asAggType(it)
        val value = match.groupValues[5].asValue(it, agg)
        val isMax = match.groupValues[6].asIsMax(it)
        val expected = match.groupValues[7].asValue(it, agg)

        val bound =
            Bound(if (!isMax) expected else null, if (isMax) expected else null, coverageUnits.convert(), agg.convert())
        val rule = Rule("", entityType.convert(), listOf(bound))

        RuleViolations(rule, listOf(BoundViolation(bound, isMax, value, entityName)))
    }.toList()
}

private fun String.asEntityType(line: String): GroupingEntityType = when (this) {
    "bundle" -> GroupingEntityType.APPLICATION
    "class" -> GroupingEntityType.CLASS
    "package" -> GroupingEntityType.PACKAGE
    else -> throw KoverCriticalException("Unknown JaCoCo entity type '$this' in verification error:\n$line")
}

private fun String.asCoverageUnit(line: String): CoverageUnit = when (this) {
    "lines" -> CoverageUnit.LINE
    "instructions" -> CoverageUnit.INSTRUCTION
    "branches" -> CoverageUnit.BRANCH
    else -> throw KoverCriticalException("Unknown JaCoCo metric type '$this' in verification error:\n$line")
}

private fun String.asAggType(line: String): AggregationType = when (this) {
    "covered ratio" -> AggregationType.COVERED_PERCENTAGE
    "missed ratio" -> AggregationType.MISSED_PERCENTAGE
    "covered count" -> AggregationType.COVERED_COUNT
    "missed count" -> AggregationType.MISSED_COUNT
    else -> throw KoverCriticalException("Unknown JaCoCo aggregation type '$this' in verification error:\n$line")
}

private fun String.asIsMax(line: String): Boolean = when (this) {
    "minimum" -> false
    "maximum" -> true
    else -> throw KoverCriticalException("Unknown JaCoCo direction '$this' in verification error:\n$line")
}

private fun String.asValue(line: String, aggregationType: AggregationType): BigDecimal {
    val value = toBigDecimalOrNull()
        ?: throw KoverCriticalException("Illegal JaCoCo metric value '$this' in verification error:\n$line")

    return if (aggregationType.isPercentage) {
        value * ONE_HUNDRED
    } else {
        value
    }
}



