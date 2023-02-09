/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.internal

import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.model.*
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.*
import java.io.*
import javax.inject.*


internal open class KoverReportExtensionImpl @Inject constructor(private val objects: ObjectFactory) :
    KoverReportExtension {

    override fun filters(config: Action<KoverReportFilters>) {
        configured = true

        if (commonFilters == null) {
            commonFilters = objects.newInstance<KoverReportFiltersImpl>(objects)
        }
        commonFilters?.also { config(it) }
    }

    override fun html(config: Action<KoverHtmlReportConfig>) {
        configured = true

        config(html)
    }

    override fun xml(config: Action<KoverXmlReportConfig>) {
        configured = true

        config(xml)
    }

    override fun verify(config: Action<KoverVerifyReportConfig>) {
        configured = true

        config(verify)
    }

    internal val html: KoverHtmlReportConfigImpl = objects.newInstance(objects)
    internal val xml: KoverXmlReportConfigImpl = objects.newInstance(objects)
    internal val verify: KoverVerifyReportConfigImpl = objects.newInstance(objects)
    internal var commonFilters: KoverReportFiltersImpl? = null
    internal var configured = false
}

internal open class KoverGeneralReportExtensionImpl @Inject constructor(private val objects: ObjectFactory) :
    KoverGeneralReportExtension {

    override fun filters(config: Action<KoverReportFilters>) {
        if (commonFilters == null) {
            commonFilters = objects.newInstance<KoverReportFiltersImpl>(objects)
        }
        commonFilters?.also { config(it) }
    }

    override fun html(config: Action<KoverGeneralHtmlReportConfig>) {
        config(html)
    }

    override fun xml(config: Action<KoverGeneralXmlReportConfig>) {
        config(xml)
    }

    override fun verify(config: Action<KoverGeneralVerifyReportConfig>) {
        config(verify)
    }

    internal val html: KoverGeneralHtmlReportConfigImpl = objects.newInstance(objects)
    internal val xml: KoverGeneralXmlReportConfigImpl = objects.newInstance(objects)
    internal val verify: KoverGeneralVerifyReportConfigImpl = objects.newInstance(objects)
    internal var commonFilters: KoverReportFiltersImpl? = null
}

internal open class KoverHtmlReportConfigImpl @Inject constructor(objects: ObjectFactory) :
    KoverGeneralHtmlReportConfigImpl(objects), KoverHtmlReportConfig {

    override var onCheck: Boolean? = null

    override fun setReportDir(dir: File) {
        reportDir.set(dir)
    }

    override fun setReportDir(dir: Provider<Directory>) {
        reportDir.set(dir.map { it.asFile })
    }

    internal val reportDir: Property<File> = objects.property()
}

internal open class KoverGeneralHtmlReportConfigImpl @Inject constructor(
    private val objects: ObjectFactory,
) : KoverGeneralHtmlReportConfig {
    internal var filters: KoverReportFiltersImpl? = null

    override var title: String? = null

    override fun filters(config: Action<KoverReportFilters>) {
        val newFilters: KoverReportFiltersImpl = objects.newInstance(objects)
        config(newFilters)
        filters = newFilters
    }

}

internal open class KoverXmlReportConfigImpl @Inject constructor(
    objects: ObjectFactory
) : KoverGeneralXmlReportConfigImpl(objects), KoverXmlReportConfig {
    override var onCheck: Boolean? = false

    override fun setReportFile(xmlFile: File) {
        reportFile.set(xmlFile)
    }

    override fun setReportFile(xmlFile: Provider<RegularFile>) {
        reportFile.set(xmlFile.map { it.asFile })
    }

    internal val reportFile: Property<File> = objects.property()
}
internal open class KoverGeneralXmlReportConfigImpl @Inject constructor(
    private val objects: ObjectFactory
) : KoverGeneralXmlReportConfig {
    internal var filters: KoverReportFiltersImpl? = null

    override fun filters(config: Action<KoverReportFilters>) {
        val newFilters: KoverReportFiltersImpl = objects.newInstance(objects)
        config(newFilters)
        filters = newFilters
    }
}

internal open class KoverVerifyReportConfigImpl @Inject constructor(
    objects: ObjectFactory,
) : KoverGeneralVerifyReportConfigImpl(objects), KoverVerifyReportConfig {
    override var onCheck: Boolean = false
}
internal open class KoverGeneralVerifyReportConfigImpl @Inject constructor(
    private val objects: ObjectFactory,
) : KoverGeneralVerifyReportConfig {
    internal var filters: KoverReportFiltersImpl? = null

    private val rules: MutableList<KoverVerifyRuleImpl> = mutableListOf()

    override fun rule(config: Action<KoverVerifyRule>) {
        val newRule = objects.newInstance<KoverVerifyRuleImpl>(objects)
        config(newRule)
        rules += newRule
    }

    override fun rule(name: String, config: Action<KoverVerifyRule>) {
        val newRule = objects.newInstance<KoverVerifyRuleImpl>(objects)
        newRule.name = name
        config(newRule)
        rules += newRule
    }

    internal fun definedRules(): List<KoverVerifyRuleImpl>? {
        return rules.takeIf { it.isNotEmpty() }
    }
}

internal open class KoverVerifyRuleImpl @Inject constructor(private val objects: ObjectFactory) : KoverVerifyRule {
    internal var filters: KoverReportFiltersImpl? = null

    override var isEnabled: Boolean = true

    override var name: String? = null

    override var entity: GroupingEntityType = GroupingEntityType.APPLICATION

    override fun filters(config: Action<KoverReportFilters>) {
        val newFilters: KoverReportFiltersImpl = objects.newInstance(objects)
        config(newFilters)
        filters = newFilters
    }

    override fun minBound(minValue: Int, metric: MetricType, aggregation: AggregationType) {
        val newBound = objects.newInstance<KoverVerifyBoundImpl>()
        newBound.minValue = minValue
        newBound.metric = metric
        newBound.aggregation = aggregation
        bounds += newBound
    }

    override fun maxBound(maxValue: Int, metric: MetricType, aggregation: AggregationType) {
        val newBound = objects.newInstance<KoverVerifyBoundImpl>()
        newBound.maxValue = maxValue
        newBound.metric = metric
        newBound.aggregation = aggregation
        bounds += newBound
    }

    override fun minBound(minValue: Int) {
        val newBound = objects.newInstance<KoverVerifyBoundImpl>()
        newBound.minValue = minValue
        bounds += newBound
    }

    override fun maxBound(maxValue: Int) {
        val newBound = objects.newInstance<KoverVerifyBoundImpl>()
        newBound.maxValue = maxValue
        bounds += newBound
    }

    override fun bound(minValue: Int, maxValue: Int, metric: MetricType, aggregation: AggregationType) {
        val newBound = objects.newInstance<KoverVerifyBoundImpl>()
        newBound.minValue = minValue
        newBound.maxValue = maxValue
        newBound.metric = metric
        newBound.aggregation = aggregation
        bounds += newBound
    }

    override fun bound(config: Action<KoverVerifyBound>) {
        val newBound = objects.newInstance<KoverVerifyBoundImpl>()
        config(newBound)
        bounds += newBound
    }

    internal val bounds: MutableList<KoverVerifyBoundImpl> = mutableListOf()
}

internal open class KoverVerifyBoundImpl : KoverVerifyBound {
    override var minValue: Int? = null
    override var maxValue: Int? = null
    override var metric: MetricType = MetricType.LINE
    override var aggregation: AggregationType = AggregationType.COVERED_PERCENTAGE
}

internal open class KoverReportFiltersImpl @Inject constructor(
    objects: ObjectFactory
) : KoverReportFilters {
    override fun excludes(config: Action<KoverReportFilter>) {
        config(excludes)
    }

    override fun includes(config: Action<KoverReportFilter>) {
        config(includes)
    }

    internal val excludes: KoverReportFilterImpl = objects.newInstance()
    internal val includes: KoverReportFilterImpl = objects.newInstance()
}

internal open class KoverReportFilterImpl : KoverReportFilter {
    internal val classes: MutableSet<String> = mutableSetOf()

    internal val annotations: MutableSet<String> = mutableSetOf()

    override fun className(vararg className: String) {
        classes += className
    }

    override fun className(classNames: Iterable<String>) {
        classes += classNames
    }

    override fun packageName(vararg className: String) {
        className.forEach {
            classes += "$it.*"
        }
    }

    override fun packageName(classNames: Iterable<String>) {
        classNames.forEach {
            classes += "$it.*"
        }
    }

    override fun annotatedBy(vararg annotationName: String) {
        annotations += annotationName
    }
}
