/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import groovy.lang.*
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.ReportFilters
import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.tools.*
import kotlinx.kover.gradle.plugin.tools.RuleViolations
import kotlinx.kover.gradle.plugin.util.*
import kotlinx.kover.gradle.plugin.util.ONE_HUNDRED
import org.gradle.internal.reflect.*
import java.math.*
import java.util.*


internal fun ReportContext.jacocoVerify(
    rules: List<VerificationRule>,
    filters: ReportFilters
): List<RuleViolations> {
    callAntReport(filters) {
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
                            MetricType.LINE -> "LINE"
                            MetricType.INSTRUCTION -> "INSTRUCTION"
                            MetricType.BRANCH -> "BRANCH"
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
                                min = min?.divide(ONE_HUNDRED)
                                max = max?.divide(ONE_HUNDRED)
                            }

                            AggregationType.MISSED_PERCENTAGE -> {
                                limitArgs["value"] = "MISSEDRATIO"
                                min = min?.divide(ONE_HUNDRED)
                                max = max?.divide(ONE_HUNDRED)
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

    return allErrorsString.lineSequence().map {
        val match = errorMessageRegex.find(it)
            ?: throw KoverCriticalException("Can't parse JaCoCo verification error string:\n$it")

        val entityType = match.groupValues[1].asEntityType(it)
        val entityName = match.groupValues[2]
        val metric = match.groupValues[3].asMetricType(it)
        val agg = match.groupValues[4].asAggType(it)
        val value = match.groupValues[5].asValue(it, agg)
        val isMax = match.groupValues[6].asIsMax(it)
        val expected = match.groupValues[7].asValue(it, agg)

        RuleViolations(entityType, listOf(BoundViolations(isMax, expected, value, metric, agg, entityName)))
    }.toList()
}

private fun String.asEntityType(line: String): GroupingEntityType = when (this) {
    "bundle" -> GroupingEntityType.APPLICATION
    "class" -> GroupingEntityType.CLASS
    "package" -> GroupingEntityType.PACKAGE
    else -> throw KoverCriticalException("Unknown JaCoCo entity type '$this' in verification error:\n$line")
}

private fun String.asMetricType(line: String): MetricType = when (this) {
    "lines" -> MetricType.LINE
    "instructions" -> MetricType.INSTRUCTION
    "branches" -> MetricType.BRANCH
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



