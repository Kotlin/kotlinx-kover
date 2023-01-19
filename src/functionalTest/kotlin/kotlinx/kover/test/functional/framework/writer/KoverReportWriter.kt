/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.writer

import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.*
import org.gradle.api.provider.*
import java.io.*

internal class KoverReportExtensionWriter(private val writer: FormattedWriter) : KoverReportExtension {
    override fun filters(config: Action<KoverReportFilters>) {
        writer.call("filters", config) { KoverReportFiltersWriter(it) }
    }

    override fun html(config: Action<KoverHtmlReportConfig>) {
        writer.call("html", config) { KoverHtmlReportConfigWriter(it) }
    }

    override fun xml(config: Action<KoverXmlReportConfig>) {
        writer.call("xml", config) { KoverXmlReportConfigWriter(it) }
    }

    override fun verify(config: Action<KoverVerifyReportConfig>) {
        writer.call("verify", config) { KoverVerifyReportConfigWriter(it) }
    }
}

internal class KoverReportFiltersWriter(private val writer: FormattedWriter) : KoverReportFilters {
    override fun excludes(config: Action<KoverReportFilter>) {
        writer.call("excludes", config) { KoverReportFilterWriter(it) }
    }

    override fun includes(config: Action<KoverReportFilter>) {
        writer.call("includes", config) { KoverReportFilterWriter(it) }
    }
}

internal class KoverReportFilterWriter(private val writer: FormattedWriter) : KoverReportFilter {
    override fun classes(config: Action<KoverReportClassFilter>) {
        writer.call("classes", config) { KoverReportClassFilterWriter(it) }
    }

    override fun annotated(config: Action<KoverReportAnnotatedFilter>) {
        writer.call("annotated", config) { KoverReportAnnotatedFilterWriter(it) }
    }

}

internal class KoverReportClassFilterWriter(private val writer: FormattedWriter) : KoverReportClassFilter {
    override fun className(vararg name: String) {
        className(name.asIterable())
    }

    override fun className(names: Iterable<String>) {
        writer.callStr("className", names)
    }

    override fun packageName(vararg name: String) {
        packageName(name.asIterable())
    }

    override fun packageName(names: Iterable<String>) {
        writer.callStr("packageName", names)
    }
}

internal class KoverReportAnnotatedFilterWriter(private val writer: FormattedWriter) :
    KoverReportAnnotatedFilter {
    override fun annotationName(vararg name: String) {
        writer.callStr("annotationName", name.asIterable())
    }
}

internal class KoverHtmlReportConfigWriter(private val writer: FormattedWriter) : KoverHtmlReportConfig {
    override var onCheck: Boolean = false
        set(value) {
            writer.assign("onCheck", value.toString())
            field = value
        }
    override var title: String = ""
        set(value) {
            writer.assign("title", "\"$value\"")
            field = value
        }

    override fun setReportDir(dir: File) {
        writer.assign("reportDir", dir.forScript())
    }

    override fun setReportDir(dir: Provider<File>) {
        error("Not supported!")
    }

    override fun filters(config: Action<KoverReportFilters>) {
        writer.call("filters", config) { KoverReportFiltersWriter(it) }
    }

}

internal class KoverXmlReportConfigWriter(private val writer: FormattedWriter) : KoverXmlReportConfig {
    override var onCheck: Boolean = false
        set(value) {
            writer.assign("onCheck", value.toString())
            field = value
        }

    override fun setReportFile(xmlFile: File) {
        writer.assign("reportFile", xmlFile.forScript())
    }

    override fun setReportFile(xmlFile: Provider<File>) {
        error("Not supported!")
    }

    override fun filters(config: Action<KoverReportFilters>) {
        writer.call("filters", config) { KoverReportFiltersWriter(it) }
    }

}

internal class KoverVerifyReportConfigWriter(private val writer: FormattedWriter) : KoverVerifyReportConfig {
    override var onCheck: Boolean = true
        set(value) {
            writer.assign("onCheck", value.toString())
            field = value
        }

    override fun rule(config: Action<KoverVerifyRule>) {
        writer.call("rule", config) { KoverVerifyRuleWriter(it) }
    }

    override fun rule(name: String, config: Action<KoverVerifyRule>) {
        writer.callStr("rule", listOf(name), config) { KoverVerifyRuleWriter(it) }
    }
}

internal class KoverVerifyRuleWriter(private val writer: FormattedWriter): KoverVerifyRule {
    override var isEnabled: Boolean = true
        set(value) {
            writer.assign("isEnabled", value.toString())
            field = value
        }
    override var name: String? = null
        set(value) {
            writer.assign("name", "\"$value\"")
            field = value
        }
    override var entity: GroupingEntityType = GroupingEntityType.APPLICATION
        set(value) {
            writer.assign("entity", value.forScript())
            field = value
        }

    override fun filters(config: Action<KoverReportFilters>) {
        writer.call("filters", config) { KoverReportFiltersWriter(it) }
    }

    override fun bound(config: Action<KoverVerifyBound>) {
        writer.call("bound", config) { KoverVerifyBoundWriter(it) }
    }

    override fun bound(minValue: Int, maxValue: Int, metric: MetricType, aggregation: AggregationType) {
        writer.call("bound", minValue.toString(), maxValue.toString(), metric.forScript(), aggregation.forScript())
    }

    override fun minBound(minValue: Int) {
        writer.call("minBound", minValue.toString())
    }

    override fun minBound(minValue: Int, metric: MetricType, aggregation: AggregationType) {
        writer.call("minBound", minValue.toString(), metric.forScript(), aggregation.forScript())
    }

    override fun maxBound(maxValue: Int) {
        writer.call("maxBound", maxValue.toString())
    }

    override fun maxBound(maxValue: Int, metric: MetricType, aggregation: AggregationType) {
        writer.call("maxBound", maxValue.toString(), metric.forScript(), aggregation.forScript())
    }

}

internal class KoverVerifyBoundWriter(private val writer: FormattedWriter): KoverVerifyBound {
    override var minValue: Int? = null
        set(value) {
            writer.assign("minValue", value.toString())
            field = value
        }
    override var maxValue: Int? = null
        set(value) {
            writer.assign("maxValue", value.toString())
            field = value
        }
    override var metric: MetricType = MetricType.LINE
        set(value) {
            writer.assign("metric", value.forScript())
            field = value
        }
    override var aggregation: AggregationType = AggregationType.COVERED_PERCENTAGE
        set(value) {
            writer.assign("aggregation", value.forScript())
            field = value
        }
}

