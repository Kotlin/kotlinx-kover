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
 * @param[rawReportExtension] The coverage report file extension, without the first `.`
 */
internal enum class CoverageToolVendor(val rawReportExtension: String) {
    KOVER("ic"),
    JACOCO("exec"),
}

internal enum class KotlinPluginType {
    JVM,
    MULTI_PLATFORM,
    ANDROID
}

internal class AppliedKotlinPlugin(val type: KotlinPluginType?)

internal class KoverSetup<T : Test>(
    // TODO docs why Provider is used
    val lazyInfo: Provider<SetupLazyInfo>,

    val tests: TaskCollection<T>,

    val id: SetupId = SetupId.Default
)

internal data class SetupId(val name: String) {
    companion object {
        val Default = SetupId(DEFAULT_PROJECT_SETUP_NAME)
    }

    val capitalized: String = name.capitalize()

    val isDefault get() = this == Default || name == Default.name
}

internal class SetupLazyInfo(
    val sources: Set<File> = emptySet(),
    val outputs: Set<File> = emptySet(),

    /**
     * TODO: All tests disabled - in this case no one compile tasks will be triggered, so output dirs will be empty and reporter can't determine project classes
     * TODO: Add tests
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
