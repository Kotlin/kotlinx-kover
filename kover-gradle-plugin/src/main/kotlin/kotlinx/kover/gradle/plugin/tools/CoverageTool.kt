/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.dsl.KoverVersions.KOVER_TOOL_VERSION
import kotlinx.kover.gradle.plugin.dsl.internal.*
import kotlinx.kover.gradle.plugin.tools.jacoco.JacocoTool
import kotlinx.kover.gradle.plugin.tools.kover.KoverTool
import org.gradle.api.Project
import org.gradle.api.file.*
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import java.io.*

/**
 * Variant of coverage tool, characterized by the vendor and its version.
 */
internal sealed class CoverageToolVariant(
    @get:Input
    val vendor: CoverageToolVendor,
    @get:Input
    val version: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoverageToolVariant

        if (vendor != other.vendor) return false
        return version == other.version
    }

    override fun hashCode(): Int {
        var result = vendor.hashCode()
        result = 31 * result + version.hashCode()
        return result
    }

    override fun toString(): String {
        return "$vendor Coverage Tool $version"
    }
}

internal class KoverToolVariant(version: String): CoverageToolVariant(CoverageToolVendor.KOVER, version)
internal object KoverToolDefaultVariant: CoverageToolVariant(CoverageToolVendor.KOVER, KOVER_TOOL_VERSION)

internal class JacocoToolVariant(version: String): CoverageToolVariant(CoverageToolVendor.JACOCO, version)

internal object JacocoToolDefaultVariant: CoverageToolVariant(CoverageToolVendor.JACOCO,
    KoverVersions.JACOCO_TOOL_DEFAULT_VERSION
)

/**
 * Common interface for different implementations of coverage tools.
 *
 * Provides all the functionality for instrumentation and reporting.
 */
internal interface CoverageTool {
    val variant: CoverageToolVariant

    /**
     * Dependency on JVM online instrumentation agent.
     *
     * Written as Gradle dependency notation (like 'group.name:artifact.name:version').
     */
    val jvmAgentDependency: String

    /**
     * Dependencies on coverage report generator.
     *
     *  Written as Gradle dependency notation (like 'group.name:artifact.name:version').
     */
    val jvmReporterDependency: String

    /**
     * Dependencies on coverage report generator.
     *
     *  Written as Gradle dependency notation (like 'group.name:artifact.name:version').
     */
    val jvmReporterExtraDependency: String

    /**
     * Find jar-file with JVM online instrumentation agent in classpath, loaded from [jvmAgentDependency].
     */
    fun findJvmAgentJar(classpath: FileCollection, archiveOperations: ArchiveOperations): File

    /**
     * Generate additional JVM argument for test task.
     */
    fun jvmAgentArgs(jarFile: File, tempDir: File, binReportFile: File, excludedClasses: Set<String>): List<String>

    /**
     * Generate XML report.
     */
    fun xmlReport(xmlFile: File, context: ReportContext)

    /**
     * Generate HTML report.
     */
    fun htmlReport(htmlDir: File, title: String, charset: String?, context: ReportContext)

    /**
     * Perform verification.
     */
    fun verify(rules: List<VerificationRule>, outputFile: File, context: ReportContext)
}

/**
 * Factory to create instance of coverage tool according project settings from Kover project extension.
 */
internal object CoverageToolFactory {
    fun get(project: Project, projectExtension: KoverProjectExtensionImpl): Provider<CoverageTool> {
        return project.provider {
            // Kover Tool Default by default
            val variant = projectExtension.toolVariant

            when (variant?.vendor) {
                CoverageToolVendor.KOVER -> KoverTool(variant)
                CoverageToolVendor.JACOCO -> JacocoTool(variant)
                null -> KoverTool(KoverToolDefaultVariant)
            }
        }
    }

}
