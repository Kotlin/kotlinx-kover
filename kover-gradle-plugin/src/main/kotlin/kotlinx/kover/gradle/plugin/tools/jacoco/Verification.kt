/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import kotlinx.kover.features.jvm.*
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import kotlinx.kover.gradle.plugin.tools.kover.convert
import kotlinx.kover.gradle.plugin.util.ONE_HUNDRED
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkQueue
import org.jacoco.core.analysis.ICounter.CounterValue
import org.jacoco.core.analysis.ICoverageNode
import org.jacoco.core.analysis.ICoverageNode.CounterEntity
import org.jacoco.report.check.IViolationsOutput
import org.jacoco.report.check.Limit
import org.jacoco.report.check.RulesChecker
import java.io.File
import java.math.BigDecimal

typealias JacocoRule = org.jacoco.report.check.Rule

internal fun ReportContext.doJacocoVerify(rules: List<VerificationRule>, output: File) {
    val workQueue: WorkQueue = services.workerExecutor.classLoaderIsolation {
        classpath.from(this@doJacocoVerify.classpath)
    }

    workQueue.submit(JacocoVerifyAction::class.java) {
        outputFile.set(output)
        rulesProperty.convention(rules)

        fillCommonParameters(this@doJacocoVerify)
    }

}

internal interface VerifyParameters: AbstractVerifyParameters {
    val outputFile: RegularFileProperty
}

internal abstract class JacocoVerifyAction : AbstractJacocoVerifyAction<VerifyParameters>() {
    override fun processResult(violations: List<RuleViolations>) {
        val errorMessage = KoverLegacyFeatures.violationMessage(violations)
        val outputFile = parameters.outputFile.get().asFile
        outputFile.writeText(errorMessage)
    }
}




internal abstract class AbstractJacocoVerifyAction<T: AbstractVerifyParameters>: WorkAction<T> {

    override fun execute() {
        val rulesPairs = parameters.rulesProperty.get().toJacoco()

        val violationListener = ViolationListener(rulesPairs)

        val formatter = RulesChecker()
        formatter.setRules(rulesPairs.map { it.origin })
        val visitor = formatter.createVisitor(violationListener)
        visitor.loadContent("application", parameters.files.get(), parameters.filters.get())
        visitor.visitEnd()

        processResult(violationListener.violations())
    }

    abstract fun processResult(violations: List<RuleViolations>)
}

internal interface AbstractVerifyParameters : CommonJacocoParameters {
    val rulesProperty: ListProperty<VerificationRule>
}

private class ViolationListener(rulesPairs: List<JacocoRuleWrapper>): IViolationsOutput {
    private val violations: Map<JacocoRuleWrapper, MutableList<BoundViolation>> =
        rulesPairs.associateWith { mutableListOf() }

    override fun onViolation(node: ICoverageNode, rule: JacocoRule, limit: Limit, message: String) {
        val bounds = violations.filterKeys { key -> key.isRule(rule) }.values.singleOrNull()
            ?: throw KoverCriticalException("Rules not mapped for JaCoCo")

        val match = errorMessageRegex.find(message)
            ?: throw KoverCriticalException("Can't parse JaCoCo verification error string:\n$message")

        val entityName = match.groupValues[2].run { if (this == ":") null else this }
        val coverageUnits = match.groupValues[3].asCoverageUnit(message)
        val agg = match.groupValues[4].asAggType(message)
        val value = match.groupValues[5].asValue(message, agg)
        val isMax = match.groupValues[6].asIsMax(message)
        val expected = match.groupValues[7].asValue(message, agg)

        val bound = Bound(if (!isMax) expected else null, if (isMax) expected else null, coverageUnits.convert(), agg.convert())
        bounds += BoundViolation(bound, isMax, value, entityName)
    }

    fun violations() : List<RuleViolations> {
        return violations.mapNotNull { v ->
            if (v.value.isEmpty()) return@mapNotNull null
            RuleViolations(v.key.koverRule, v.value)
        }
    }

}

private class JacocoRuleWrapper(val origin: JacocoRule, val koverRule: Rule) {
    fun isRule(rule: JacocoRule): Boolean = origin === rule
}

private fun List<VerificationRule>.toJacoco(): List<JacocoRuleWrapper> {
    return map { rule -> JacocoRuleWrapper(rule.toJacoco(), rule.convert()) }
}

private fun VerificationRule.toJacoco(): JacocoRule {
    val rule = JacocoRule()

    rule.element = when(this.entityType) {
        GroupingEntityType.APPLICATION -> ICoverageNode.ElementType.BUNDLE
        GroupingEntityType.CLASS -> ICoverageNode.ElementType.CLASS
        GroupingEntityType.PACKAGE -> ICoverageNode.ElementType.PACKAGE
    }

    rule.limits = bounds.map { bound -> bound.toJacoco() }

    return rule
}

private fun VerificationBound.toJacoco(): Limit {
    val limit = Limit()

    val entity = when (metric) {
        CoverageUnit.LINE -> CounterEntity.LINE
        CoverageUnit.INSTRUCTION -> CounterEntity.INSTRUCTION
        CoverageUnit.BRANCH -> CounterEntity.BRANCH
    }
    limit.setCounter(entity.name)
    var min: BigDecimal? = minValue
    var max: BigDecimal? = maxValue
    val value: CounterValue
    when (aggregation) {
        AggregationType.COVERED_COUNT -> {
            value = CounterValue.COVEREDCOUNT
        }

        AggregationType.MISSED_COUNT -> {
            value = CounterValue.MISSEDCOUNT
        }

        AggregationType.COVERED_PERCENTAGE -> {
            value = CounterValue.COVEREDRATIO
            min = min?.divide(ONE_HUNDRED)?.setScale(4)
            max = max?.divide(ONE_HUNDRED)?.setScale(4)
        }

        AggregationType.MISSED_PERCENTAGE -> {
            value = CounterValue.MISSEDRATIO
            min = min?.divide(ONE_HUNDRED)?.setScale(4)
            max = max?.divide(ONE_HUNDRED)?.setScale(4)
        }
    }
    limit.setValue(value.name)
    if (min != null) {
        limit.minimum = min.toPlainString()
    }
    if (max != null) {
        limit.maximum = max.toPlainString()
    }
    return limit
}


private val errorMessageRegex =
    "Rule violated for (\\w+) (.+): (\\w+) (.+) is ([\\d\\.]+), but expected (\\w+) is ([\\d\\.]+)".toRegex()


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
        (value * ONE_HUNDRED).stripTrailingZeros()
    } else {
        value
    }
}
