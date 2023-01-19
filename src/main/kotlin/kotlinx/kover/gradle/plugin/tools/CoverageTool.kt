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
import java.io.*

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
