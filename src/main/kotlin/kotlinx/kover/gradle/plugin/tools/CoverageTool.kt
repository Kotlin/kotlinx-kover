/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.dsl.internal.*
import kotlinx.kover.gradle.plugin.tools.jacoco.JacocoTool
import kotlinx.kover.gradle.plugin.tools.kover.KoverTool
import org.gradle.api.file.*
import org.gradle.api.tasks.*
import java.io.*


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
        if (version != other.version) return false

        return true
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
internal object KoverToolDefaultVariant: CoverageToolVariant(CoverageToolVendor.KOVER,
    KoverVersions.KOVER_TOOL_DEFAULT_VERSION
)

internal class JacocoToolVariant(version: String): CoverageToolVariant(CoverageToolVendor.JACOCO, version)

internal object JacocoToolDefaultVariant: CoverageToolVariant(CoverageToolVendor.JACOCO,
    KoverVersions.JACOCO_TOOL_DEFAULT_VERSION
)

internal interface CoverageTool {
    val variant: CoverageToolVariant

    val jvmAgentDependency: String
    val jvmReporterDependencies: List<String>

    fun findJvmAgentJar(classpath: FileCollection, archiveOperations: ArchiveOperations): File

    fun jvmAgentArgs(jarFile: File, tempDir: File, rawReportFile: File, excludedClasses: Set<String>): List<String>

    fun xmlReport(xmlFile: File, filters: ReportFilters, context: ReportContext)

    fun htmlReport(htmlDir: File, title: String, filters: ReportFilters, context: ReportContext)

    fun verify(rules: List<VerificationRule>, commonFilters: ReportFilters, context: ReportContext): List<RuleViolations>
}

internal object CoverageToolFactory {
    fun get(projectExtension: KoverProjectExtensionImpl): CoverageTool {
        val variant = projectExtension.toolVariant
        return when (variant.vendor) {
            CoverageToolVendor.KOVER -> KoverTool(variant)
            CoverageToolVendor.JACOCO -> JacocoTool(variant)
        }
    }

}
