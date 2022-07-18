package kotlinx.kover.tasks

import kotlinx.kover.api.*
import kotlinx.kover.engines.commons.*
import org.gradle.api.*
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*

// TODO make internal in 0.7 version
@CacheableTask
public open class KoverVerificationTask : KoverReportTask() {
    @get:Nested
    internal val rules: ListProperty<VerificationRule> = project.objects.listProperty(VerificationRule::class.java)

    @get:OutputFile
    internal val resultFile: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun verify() {
        val reportRules = convertRules()
        val errors = EngineManager.verify(
            engine.get(),
            this,
            exec,
            files.get(),
            classFilter.get(),
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

            result += ReportVerificationRule(result.size, rule.name, rule.target, rule.classFilter.orNull, ruleBounds)
        }

        return result
    }


    // DEPRECATIONS
    // TODO delete in 0.7 version
    @Suppress("UNUSED_PARAMETER")
    @Deprecated(
        message = "Function was removed in Kover API version 2. Please read migration to 0.6.0 guide to solve the issue",
        level = DeprecationLevel.ERROR
    )
    public fun rule(configureRule: Action<VerificationRule>) {
        throw Exception("Function was removed in Kover API version 2. Please read migration to 0.6.0 guide to solve the issue")
    }

    @get:Internal
    @Deprecated(
        message = "Property was removed in Kover API version 2. Please read migration to 0.6.0 guide to solve the issue",
        level = DeprecationLevel.ERROR
    )
    public var includes: List<String> = emptyList()

    @get:Internal
    @Deprecated(
        message = "Property was removed in Kover API version 2. Please read migration to 0.6.0 guide to solve the issue",
        level = DeprecationLevel.ERROR
    )
    public var excludes: List<String> = emptyList()
}

