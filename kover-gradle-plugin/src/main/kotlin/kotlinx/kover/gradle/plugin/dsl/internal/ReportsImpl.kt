/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.internal

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject


internal abstract class KoverReportsConfigImpl @Inject constructor(
    private val objects: ObjectFactory,
    private val layout: ProjectLayout,
    private val projectPath: String
) : KoverReportsConfig {
    private val rootFilters: KoverReportFiltersConfigImpl = objects.newInstance()
    private val rootVerify: KoverVerificationRulesConfigImpl = objects.newInstance()

    internal val total: KoverReportSetConfigImpl = createReportSet(TOTAL_VARIANT_NAME, projectPath)

    internal val byName: MutableMap<String, KoverReportSetConfigImpl> = mutableMapOf()

    override fun filters(config: Action<KoverReportFiltersConfig>) {
        rootFilters.also { config(it) }
    }

    override fun verify(config: Action<KoverVerificationRulesConfig>) {
        rootVerify.also { config(it) }
    }

    override fun total(config: Action<KoverReportSetConfig>) {
        config(total)
    }

    override fun variant(variant: String, config: Action<KoverReportSetConfig>) {
        val report = byName.getOrPut(variant) {
            createReportSet(variant, projectPath)
        }
        config(report)
    }

    internal fun createReportSet(variantName: String, projectPath: String): KoverReportSetConfigImpl {
        val block =
            objects.newInstance<KoverReportSetConfigImpl>(objects, layout.buildDirectory, variantName, projectPath)

        block.filters.extendsFrom(rootFilters)
        block.verify.extendFrom(rootVerify)

        return block
    }
}

internal abstract class KoverReportSetConfigImpl @Inject constructor(
    objects: ObjectFactory,
    buildDir: DirectoryProperty,
    variantName: String,
    projectPath: String
) : KoverReportSetConfig {
    internal val filters: KoverReportFiltersConfigImpl = objects.newInstance()
    internal val verify: KoverVerifyTaskConfigImpl = objects.newInstance()

    internal val html: KoverHtmlTaskConfig = objects.newInstance()
    internal val xml: KoverXmlTaskConfig = objects.newInstance()
    internal val binary: KoverBinaryTaskConfig = objects.newInstance()
    internal val log: KoverLogTaskConfig = objects.newInstance()

    init {
        xml.xmlFile.convention(buildDir.file(xmlReportPath(variantName)))
        html.htmlDir.convention(buildDir.dir(htmlReportPath(variantName)))
        binary.file.convention(buildDir.file(binaryReportPath(variantName)))

        html.onCheck.convention(false)
        xml.onCheck.convention(false)
        binary.onCheck.convention(false)
        log.onCheck.convention(false)
        verify.onCheck.convention(variantName == TOTAL_VARIANT_NAME)

        xml.title.convention("Kover Gradle Plugin XML report for $projectPath")

        log.format.convention("<entity> line coverage: <value>%")
        log.groupBy.convention(GroupingEntityType.APPLICATION)
        log.coverageUnits.convention(CoverageUnit.LINE)
        log.aggregationForGroup.convention(AggregationType.COVERED_PERCENTAGE)
    }


    override fun filters(config: Action<KoverReportFiltersConfig>) {
        filters.clean()
        config(filters)
    }

    override fun filtersAppend(config: Action<KoverReportFiltersConfig>) {
        config(filters)
    }

    override fun html(config: Action<KoverHtmlTaskConfig>) {
        config(html)
    }

    override fun xml(config: Action<KoverXmlTaskConfig>) {
        config(xml)
    }

    override fun binary(config: Action<KoverBinaryTaskConfig>) {
        config(binary)
    }

    override fun verify(config: Action<KoverVerifyTaskConfig>) {
        verify.clean()
        config(verify)
    }

    override fun verifyAppend(config: Action<KoverVerifyTaskConfig>) {
        config(verify)
    }

    override fun log(config: Action<KoverLogTaskConfig>) {
        config(log)
    }
}

internal abstract class KoverVerifyTaskConfigImpl @Inject constructor(objects: ObjectFactory) :
    KoverVerificationRulesConfigImpl(objects), KoverVerifyTaskConfig

internal abstract class KoverVerificationRulesConfigImpl @Inject constructor(
    private val objects: ObjectFactory
) : KoverVerificationRulesConfig {
    internal abstract val rules: ListProperty<KoverVerifyRuleImpl>

    init {
        @Suppress("LeakingThis")
        warningInsteadOfFailure.convention(false)
    }

    override fun rule(config: Action<KoverVerifyRule>) {
        val newRule = objects.newInstance<KoverVerifyRuleImpl>(objects, "")
        config(newRule)

        rules.add(newRule)
    }

    override fun rule(name: String, config: Action<KoverVerifyRule>) {
        val newRule = objects.newInstance<KoverVerifyRuleImpl>(objects, name)
        config(newRule)
        rules.add(newRule)
    }

    internal fun extendFrom(other: KoverVerificationRulesConfigImpl) {
        rules.addAll(other.rules)
        warningInsteadOfFailure.convention(other.warningInsteadOfFailure)
    }

    internal fun clean() {
        rules.empty()
    }
}

internal abstract class KoverVerifyRuleImpl @Inject constructor(private val objects: ObjectFactory, val name: String) : KoverVerifyRule {

    init {
        // Gradle is guaranteed to fill properties
        @Suppress("LeakingThis")
        disabled.set(false)
        @Suppress("LeakingThis")
        groupBy.set(GroupingEntityType.APPLICATION)
    }

    override fun minBound(minValue: Int, coverageUnits: CoverageUnit, aggregationForGroup: AggregationType) {
        val newBound = createBound()
        newBound.minValue.set(minValue)
        newBound.coverageUnits.set(coverageUnits)
        newBound.aggregationForGroup.set(aggregationForGroup)
        bounds += newBound
    }

    override fun minBound(minValue: Int) {
        val newBound = createBound()
        newBound.minValue.set(minValue)
        bounds += newBound
    }

    override fun minBound(minValue: Provider<Int>) {
        val newBound = createBound()
        newBound.minValue.set(minValue)
        bounds += newBound
    }

    override fun maxBound(maxValue: Int, coverageUnits: CoverageUnit, aggregationForGroup: AggregationType) {
        val newBound = createBound()
        newBound.maxValue.set(maxValue)
        newBound.coverageUnits.set(coverageUnits)
        newBound.aggregationForGroup.set(aggregationForGroup)
        bounds += newBound
    }

    override fun maxBound(maxValue: Int) {
        val newBound = createBound()
        newBound.maxValue.set(maxValue)
        bounds += newBound
    }

    override fun maxBound(maxValue: Provider<Int>) {
        val newBound = createBound()
        newBound.maxValue.set(maxValue)
        bounds += newBound
    }

    override fun bound(minValue: Int, maxValue: Int, coverageUnits: CoverageUnit, aggregationForGroup: AggregationType) {
        val newBound = createBound()
        newBound.minValue.set(minValue)
        newBound.maxValue.set(maxValue)
        newBound.coverageUnits.set(coverageUnits)
        newBound.aggregationForGroup.set(aggregationForGroup)
        bounds += newBound
    }

    override fun bound(config: Action<KoverVerifyBound>) {
        val newBound = createBound()
        config(newBound)
        bounds += newBound
    }

    internal val bounds: MutableList<KoverVerifyBound> = mutableListOf()

    private fun createBound(): KoverVerifyBound {
        val newBound = objects.newInstance<KoverVerifyBound>()
        newBound.coverageUnits.set(CoverageUnit.LINE)
        newBound.aggregationForGroup.set(AggregationType.COVERED_PERCENTAGE)
        return newBound
    }
}


internal open class KoverReportFiltersConfigImpl @Inject constructor(
    objects: ObjectFactory
) : KoverReportFiltersConfig {
    internal val excludesImpl: KoverReportFilterImpl = objects.newInstance()
    internal val includesImpl: KoverReportFilterImpl = objects.newInstance()

    override fun excludes(config: Action<KoverReportFilter>) {
        config(excludesImpl)
    }

    override fun includes(config: Action<KoverReportFilter>) {
        config(includesImpl)
    }

    internal fun clean() {
        excludesImpl.clean()
        includesImpl.clean()
    }

    internal fun extendsFrom(other: KoverReportFiltersConfigImpl) {
        excludesImpl.extendsFrom(other.excludesImpl)
        includesImpl.extendsFrom(other.includesImpl)
    }
}


internal abstract class KoverReportFilterImpl: KoverReportFilter {
    internal abstract val classes: SetProperty<String>
    internal abstract val annotations: SetProperty<String>
    internal abstract val inheritedFrom: SetProperty<String>

    override fun classes(vararg names: String) {
        classes.addAll(*names)
    }

    override fun classes(names: Iterable<String>) {
        classes.addAll(names)
    }

    override fun classes(vararg names: Provider<String>) {
        names.forEach { nameProvider ->
            classes.add(nameProvider)
        }
    }

    override fun classes(names: Provider<Iterable<String>>) {
        classes.addAll(names)
    }

    override fun packages(vararg names: String) {
        names.forEach { packageName ->
            classes.add(packageName.packageAsClass())
        }
    }

    override fun packages(names: Iterable<String>) {
        names.forEach { packageName ->
            classes.add(packageName.packageAsClass())
        }
    }

    override fun packages(vararg names: Provider<String>) {
        names.forEach { packageNameProvider ->
            classes.add(packageNameProvider.map { it.packageAsClass() })
        }
    }

    override fun packages(names: Provider<Iterable<String>>) {
        classes.addAll(names.map { packages ->
            packages.map { it.packageAsClass() }
        })
    }

    override fun annotatedBy(vararg annotationName: String) {
        annotations.addAll(*annotationName)
    }

    override fun annotatedBy(vararg annotationName: Provider<String>) {
        annotationName.forEach { nameProvider ->
            annotations.add(nameProvider)
        }
    }
    override fun inheritedFrom(vararg typeName: String) {
        inheritedFrom.addAll(*typeName)
    }

    override fun inheritedFrom(vararg typeName: Provider<String>) {
        typeName.forEach { nameProvider ->
            inheritedFrom.add(nameProvider)
        }
    }

    internal fun extendsFrom(other: KoverReportFilterImpl) {
        classes.addAll(other.classes)
        annotations.addAll(other.annotations)
        projects.addAll(other.projects)
        inheritedFrom.addAll(other.inheritedFrom)
    }

    internal fun clean() {
        classes.empty()
        annotations.empty()
        inheritedFrom.empty()
        projects.empty()
    }

    private fun String.packageAsClass(): String = "$this.*"
}
