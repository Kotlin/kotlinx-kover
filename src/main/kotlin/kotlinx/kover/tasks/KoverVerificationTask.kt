package kotlinx.kover.tasks

import kotlinx.kover.api.KoverMigrations
import kotlinx.kover.api.VerificationRule
import kotlinx.kover.engines.commons.EngineManager
import kotlinx.kover.engines.commons.ReportVerificationBound
import kotlinx.kover.engines.commons.ReportVerificationRule
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.listProperty
import org.gradle.process.ExecOperations
import javax.inject.Inject

// TODO make internal in 0.7 version - for now it public to save access to deprecated fields to print deprecation message
@CacheableTask
public open class KoverVerificationTask @Inject constructor(
    private val objects: ObjectFactory,
    // exec operations to launch Java applications
    private val exec: ExecOperations,
) : KoverReportTask(objects) {
    @get:Nested
    internal val rules: ListProperty<VerificationRule> = project.objects.listProperty()

    @get:OutputFile
    internal val resultFile: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun verify() {
        val reportRules = convertRules()
        val errors = EngineManager.verify(
            engine.get(),
            this,
            exec,
            files,
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
        message = "Function was removed in Kover API version 2, move it in 'verify {  }' in Kover project extension instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}",
        level = DeprecationLevel.ERROR
    )
    public fun rule(configureRule: Action<VerificationRule>) {
        throw Exception("Function was removed in Kover API version 2. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}")
    }

    @get:Internal
    @Deprecated(
        message = "Property was removed in Kover API version 2. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}",
        level = DeprecationLevel.ERROR
    )
    public var includes: List<String> = emptyList()

    @get:Internal
    @Deprecated(
        message = "Property was removed in Kover API version 2. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}",
        level = DeprecationLevel.ERROR
    )
    public var excludes: List<String> = emptyList()
}

