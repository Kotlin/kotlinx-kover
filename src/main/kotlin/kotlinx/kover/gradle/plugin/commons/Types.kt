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
 * All used compilation kits for some Gradle Project.
 */
internal class ProjectCompilation(
    val kotlinPlugin: AppliedKotlinPlugin,
    val jvm: List<JvmCompilationKit> = emptyList(),
    val android: List<AndroidCompilationKit> = emptyList()
)

/**
 * Grouped named JVM compilation kits and tests running on them.
 */
internal class JvmCompilationKit(
    val targetName: String,
    val tests: TaskCollection<Test>,
    val compilations: Provider<Map<String, CompilationUnit>>,
)

/**
 * Grouped named Android compilation kits and tests running on them.
 *
 * Contains additional information about the build variant taken from the Android Gradle Plugin
 */
internal class AndroidCompilationKit(
    val buildVariant: String,
    val buildType: String,
    val flavors: List<AndroidFlavor>,

    val fallbacks: AndroidFallbacks,

    /**
     * The flavors used in case the dependency contains a dimension that is missing in the current project.
     * Specific only for this build variant.
     *
     * map of (dimension > flavor)
     */
    val missingDimensions: Map<String, String>,

    val tests: TaskCollection<Test>,
    val compilations: Provider<Map<String, CompilationUnit>>
)

internal class AndroidFallbacks(
    /**
     *  Specifies a sorted list of fallback build types that the
     *  Kover can try to use when a dependency does not include a
     *  key build type. Kover selects the first build type that's
     *  available in the dependency
     *
     *  map of (buildtype > fallbacks)
     *  */
    val buildTypes: Map<String, List<String>>,

    /**
     * first loop through all the flavors and collect for each dimension, and each value, its
     * fallbacks.
     *
     * map of (dimension > (requested > fallbacks))
     */
    val flavors: Map<String, Map<String, List<String>>>,
)

/**
 * Flavor in Android Project.
 */
internal class AndroidFlavor(
    val dimension: String,
    val name: String,
)

/**
 * Atomic portion of information about the building of part of the project.
 */
internal class CompilationUnit(
    /**
     * Directories of sources, used in [compileTasks].
     */
    val sources: Set<File> = emptySet(),

    /**
     * Directories with compiled classes, outputs of [compileTasks].
     */
    val outputs: Set<File> = emptySet(),

    /**
     * In case when no one compile tasks will be triggered,
     * output dirs will be empty and reporter can't determine project classes.
     *
     * So compile tasks must be triggered anyway.
     */
    val compileTasks: List<Task> = emptyList(),
)


internal class ReportContext(
    val files: ArtifactContent,
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
