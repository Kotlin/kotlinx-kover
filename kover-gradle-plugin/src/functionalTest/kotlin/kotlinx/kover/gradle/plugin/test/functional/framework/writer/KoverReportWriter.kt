/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.writer

import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.*
import java.io.*

internal class KoverReportExtensionWriter(private val writer: FormattedWriter) : KoverReportExtension {
    override fun filters(config: Action<KoverReportFilters>) {
        writer.call("filters", config) { KoverReportFiltersWriter(it) }
    }

    override fun verify(config: Action<KoverVerificationRulesConfig>) {
        writer.call("verify", config) { KoverVerificationRulesConfigWriter(it) }
    }

    override fun defaults(config: Action<KoverDefaultReportsConfig>) {
        writer.call("defaults", config) { KoverDefaultReportsWriter(it) }
    }

    override fun androidReports(variant: String, config: Action<KoverReportsConfig>) {
        writer.call("androidReports", config) { KoverReportsWriter(it) }
    }

}

internal class KoverDefaultReportsWriter(private val writer: FormattedWriter): KoverReportsWriter(writer), KoverDefaultReportsConfig {
    override fun mergeWith(otherVariant: String) {
        writer.call("mergeWith", otherVariant)
    }

}


internal open class KoverReportsWriter(private val writer: FormattedWriter): KoverReportsConfig {
    override fun filters(config: Action<KoverReportFilters>) {
        writer.call("filters", config) { KoverReportFiltersWriter(it) }
    }

    override fun html(config: Action<KoverHtmlReportConfig>) {
        writer.call("html", config) { KoverHtmlReportConfigWriter(it) }
    }

    override fun xml(config: Action<KoverXmlReportConfig>) {
        writer.call("xml", config) { KoverXmlReportConfigWriter(it) }
    }

    override fun binary(config: Action<KoverBinaryReportConfig>) {
        writer.call("binary", config) { KoverBinaryReportConfigWriter(it) }
    }

    override fun verify(config: Action<KoverVerifyReportConfig>) {
        writer.call("verify", config) { KoverVerifyReportConfigWriter(it) }
    }

    override fun log(config: Action<KoverLogReportConfig>) {
        writer.call("log", config) { KoverLogReportConfigWriter(it) }
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

    override fun classes(vararg names: String) {
        this.classes(names.asIterable())
    }

    override fun classes(names: Iterable<String>) {
        writer.callStr("classes", names)
    }

    override fun packages(vararg names: String) {
        packages(names.asIterable())
    }

    override fun packages(names: Iterable<String>) {
        writer.callStr("packages", names)
    }

    override fun annotatedBy(vararg annotationName: String) {
        writer.callStr("annotatedBy", annotationName.asIterable())
    }

}

internal class KoverHtmlReportConfigWriter(private val writer: FormattedWriter) : KoverHtmlReportConfig {
    override var onCheck: Boolean = false
        set(value) {
            writer.assign("onCheck", value.toString())
            field = value
        }
    override var title: String? = null
        set(value) {
            writer.assign("title", "\"$value\"")
            field = value
        }
    override var charset: String? = null
        set(value) {
            writer.assign("charset", "\"$value\"")
            field = value
        }

    override fun setReportDir(dir: File) {
        writer.assign("reportDir", dir.forScript())
    }

    override fun setReportDir(dir: Provider<Directory>) {
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

    override fun setReportFile(xmlFile: Provider<RegularFile>) {
        error("Not supported!")
    }

    override fun filters(config: Action<KoverReportFilters>) {
        writer.call("filters", config) { KoverReportFiltersWriter(it) }
    }

}

internal class KoverBinaryReportConfigWriter(private val writer: FormattedWriter) : KoverBinaryReportConfig {
    override val onCheck: Property<Boolean> = PropertyWriter("onCheck", writer)

    override val file: RegularFileProperty = FilePropertyWriter("file", writer)

    override fun filters(config: Action<KoverReportFilters>) {
        writer.call("filters", config) { KoverReportFiltersWriter(it) }
    }
}

internal class KoverVerifyReportConfigWriter(private val writer: FormattedWriter) :
    KoverVerificationRulesConfigWriter(writer), KoverVerifyReportConfig {

    override var onCheck: Boolean = true
        set(value) {
            writer.assign("onCheck", value.toString())
            field = value
        }
}

internal open class KoverVerificationRulesConfigWriter(private val writer: FormattedWriter) :
    KoverVerificationRulesConfig {

    override fun rule(config: Action<KoverVerifyRule>) {
        writer.call("rule", config) { KoverVerifyRuleWriter(it) }
    }

    override fun rule(name: String, config: Action<KoverVerifyRule>) {
        writer.callStr("rule", listOf(name), config) { KoverVerifyRuleWriter(it) }
    }
}

internal class KoverLogReportConfigWriter(private val writer: FormattedWriter) : KoverLogReportConfig {
    override var onCheck: Boolean = true
        set(value) {
            writer.assign("onCheck", value.toString())
            field = value
        }

    override fun filters(config: Action<KoverReportFilters>) {
        writer.call("filters", config) { KoverReportFiltersWriter(it) }
    }

    override var header: String? = null
        set(value) {
            if (value == null) {
                writer.assign("header", "null")
            } else {
                writer.assign("header", "\"$value\"")
            }
            field = value
        }

    override var format: String? = null
        set(value) {
            if (value == null) {
                writer.assign("format", "null")
            } else {
                writer.assign("format", "\"$value\"")
            }
            field = value
        }

    override var groupBy: GroupingEntityType? = null
        set(value) {
            if (value == null) {
                writer.assign("groupBy", "null")
            } else {
                writer.assign("groupBy", GroupingEntityType::class.qualifiedName + "." + value)
            }
            field = value
        }

    override var coverageUnits: MetricType? = null
        set(value) {
            if (value == null) {
                writer.assign("coverageUnits", "null")
            } else {
                writer.assign("coverageUnits", MetricType::class.qualifiedName + "." + value)
            }
            field = value
        }
    override var aggregationForGroup: AggregationType? = null
        set(value) {
            if (value == null) {
                writer.assign("aggregationForGroup", "null")
            } else {
                writer.assign("aggregationForGroup", AggregationType::class.qualifiedName + "." + value)
            }
            field = value
        }
}

internal class KoverVerifyRuleWriter(private val writer: FormattedWriter): KoverVerifyRule {
    override var isEnabled: Boolean = true
        set(value) {
            writer.assign("isEnabled", value.toString())
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

    @Deprecated(message = "Removed")
    override var name: String? = null
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

