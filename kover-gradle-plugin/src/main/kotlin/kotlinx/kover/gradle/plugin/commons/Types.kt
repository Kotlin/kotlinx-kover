/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.commons

import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.*
import org.gradle.workers.WorkerExecutor
import java.io.*
import java.math.BigDecimal
import javax.annotation.*
import javax.inject.*

/**
 * The name of the author or the brand of the instrument.
 *
 * @param[binReportExtension] The coverage report file extension, without the first `.`
 */
internal enum class CoverageToolVendor(val binReportExtension: String) {
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
 * Type of report variant.
 *
 * A variant can be created based on a specific origin, total for all origins, or it can be declared by a user.
 */
internal enum class ReportVariantType {
    TOTAL,
    JVM,
    ANDROID,
    CUSTOM
}

/**
 * Type of Kotlin plugin, applied in a specific project.
 *
 * If no Kotlin plugin is used in this project, then [type] is `null`.
 */
internal class AppliedKotlinPlugin(val type: KotlinPluginType?)

internal class ReportContext(
    val files: ArtifactContent,
    val filters: ReportFilters,
    val classpath: FileCollection,
    val tempDir: File,
    val projectPath: String,
    val services: GradleReportServices
)

internal class GradleReportServices(
    val antBuilder: AntBuilder,
    val objects: ObjectFactory
)

internal data class ReportFilters(
    @get:Input
    val includesClasses: Set<String> = emptySet(),
    @get:Input
    val includesAnnotations: Set<String> = emptySet(),
    @get:Input
    val includeInheritedFrom: Set<String> = emptySet(),
    @get:Input
    val excludesClasses: Set<String> = emptySet(),
    @get:Input
    val excludesAnnotations: Set<String> = emptySet(),
    @get:Input
    val includeProjects: Set<String> = emptySet(),
    @get:Input
    val excludeProjects: Set<String> = emptySet(),
    @get:Input
    val excludeInheritedFrom: Set<String> = emptySet()
): Serializable

internal open class VerificationRule @Inject constructor(
    @get:Input
    val isEnabled: Boolean,

    @get:Input
    val name: String,

    @get:Input
    val entityType: GroupingEntityType,

    @get:Nested
    internal val bounds: List<VerificationBound>
): Serializable

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
    val metric: CoverageUnit,

    @get:Input
    val aggregation: AggregationType
): Serializable
