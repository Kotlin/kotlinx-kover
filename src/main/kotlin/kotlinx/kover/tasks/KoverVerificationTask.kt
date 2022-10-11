package kotlinx.kover.tasks

import kotlinx.kover.api.*
import kotlinx.kover.tools.commons.*
import org.gradle.api.*
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*

@CacheableTask
internal open class KoverVerificationTask : KoverReportTask() {
    @get:Nested
    internal val rules: ListProperty<VerificationRule> = project.objects.listProperty()

    @get:OutputFile
    internal val resultFile: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun verify() {
        val reportRules = convertRules()
        val errors = ToolManager.verify(
            tool.get(),
            this,
            exec,
            files.get(),
            getReportFilters(),
            reportRules,
        )
        resultFile.get().asFile.writeText(errors ?: "")

        if (errors != null) {
            throw GradleException(errors)
        }
    }

    private fun convertRules(): List<ReportVerificationRule> {
        val result = mutableListOf<ReportVerificationRule>()

        rules.get().forEach { rule ->
            if (!rule.isEnabled) {
                return@forEach
            }

            val ruleBounds = mutableListOf<ReportVerificationBound>()
            rule.bounds.get().forEach { bound ->
                val min = bound.minValue?.toBigDecimal()
                val max = bound.maxValue?.toBigDecimal()
                ruleBounds += ReportVerificationBound(ruleBounds.size, min, max, bound.counter, bound.valueType)
            }

            result += ReportVerificationRule(
                result.size,
                rule.name,
                rule.target,
                rule.classFilter.orNull,
                rule.annotationFilter.orNull,
                ruleBounds
            )
        }

        return result
    }
}

