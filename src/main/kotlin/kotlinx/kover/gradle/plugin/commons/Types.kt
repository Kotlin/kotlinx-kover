/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.commons

import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.api.tasks.testing.*
import org.gradle.process.*
import java.io.*
import java.math.BigDecimal
import javax.annotation.*
import javax.inject.*

/**
 * The name of the author or the brand of the instrument.
 *
 * @param[rawReportExtension] The coverage report file extension, without the first `.`
 */
internal enum class CoverageToolVendor(val rawReportExtension: String) {
    KOVER("ic"),
    JACOCO("exec"),
}

/**
 * Type of Kotlin plugin.
 */
internal enum class KotlinPluginType {
    JVM,
    MULTIPLATFORM,
    ANDROID
}

/**
 * Type of Kotlin plugin, applied in a specific project.
 *
 * If no Kotlin plugin is used in this project, then [type] is `null`.
 */
internal class AppliedKotlinPlugin(val type: KotlinPluginType?)

/**
 * Kover Setup - is a named set of exhaustive information, used in instrumentation and generation of reports.
 *
 * Includes of:
 *  - directories with source files and class-files
 *  - compile tasks used to compile all classes of the project
 *  - test tasks which need to run to measure the coverage
 */
internal class KoverSetup<T : Test>(
    // Provider is used because the list of directories and compilation tasks may change in the `afterEvaluate` block of another plugin later
    val lazyInfo: Provider<SetupLazyInfo>,

    val tests: TaskCollection<T>,

    val id: SetupId = SetupId.Regular
)

/**
 * Identifier of Kover setup.
 */
internal data class SetupId(val name: String) {
    companion object {
        /**
         * Used only if there is supposed to be only one setup in the project, e.g. in case of Kotlin/JVM or Kotlin/MP projects.
         */
        val Regular = SetupId(REGULAR_SETUP_NAME)
    }

    /**
     * It is used for convenient generation of the task name or other named objects in Gradle.
     */
    val capitalized: String = name.capitalize()
}

/**
 * Part of Kover setup information, received only during the execution of tasks (lazily).
 */
internal class SetupLazyInfo(
    val sources: Set<File> = emptySet(),
    val outputs: Set<File> = emptySet(),

    /**
     * In case when no one compile tasks will be triggered,
     * output dirs will be empty and reporter can't determine project classes.
     *
     * So compile tasks must be triggered anyway.
     */
    val compileTasks: List<Task> = emptyList()
)

internal class BuildFiles(
    val sources: Set<File>,
    val outputs: Set<File>,
    val reports: Set<File>
)

internal class ReportContext(
    val files: BuildFiles,
    val classpath: FileCollection,
    val tempDir: File,
    val projectPath: String,
    val services: GradleReportServices
)

internal class GradleReportServices(
    val exec: ExecOperations,
    val antBuilder: AntBuilder,
    val objects: ObjectFactory
)

internal data class ReportFilters(
    @get:Input
    val includesClasses: Set<String> = emptySet(),
    @get:Input
    val includesAnnotations: Set<String> = emptySet(),
    @get:Input
    val excludesClasses: Set<String> = emptySet(),
    @get:Input
    val excludesAnnotations: Set<String> = emptySet()
)

internal open class VerificationRule @Inject constructor(
    @get:Input
    val isEnabled: Boolean,

    @get:Nested
    @get:Nullable
    @get:Optional
    val filters: ReportFilters?,

    @get:Input
    @get:Nullable
    @get:Optional
    val name: String?,

    @get:Input
    val entityType: GroupingEntityType,

    @get:Nested
    internal val bounds: List<VerificationBound>
)

internal open class VerificationBound(
    @get:Input
    @get:Nullable
    @get:Optional
    val minValue: BigDecimal?,

    @get:Input
    @get:Nullable
    @get:Optional
    val maxValue: BigDecimal?,

    @get:Input
    val metric: MetricType,

    @get:Input
    val aggregation: AggregationType
)
