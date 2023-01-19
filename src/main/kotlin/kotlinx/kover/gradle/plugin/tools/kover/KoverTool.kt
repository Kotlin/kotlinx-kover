/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.tools.*
import org.gradle.api.*
import org.gradle.api.file.*
import java.io.*


internal class KoverTool(override val variant: CoverageToolVariant) : CoverageTool {
    override val jvmAgentDependency: String = "org.jetbrains.intellij.deps:intellij-coverage-agent:${variant.version}"

    override val jvmReporterDependencies: List<String> = listOf(
        "org.jetbrains.intellij.deps:intellij-coverage-reporter:${variant.version}"
    )

    override fun findJvmAgentJar(classpath: FileCollection, archiveOperations: ArchiveOperations): File {
        return classpath.filter { it.name.startsWith("intellij-coverage-agent") }.files.firstOrNull()
            ?: throw GradleException("JVM instrumentation agent not found for Kover Coverage Tool")
    }

    override fun jvmAgentArgs(
        jarFile: File,
        tempDir: File,
        rawReportFile: File,
        excludedClasses: Set<String>
    ): List<String> {
        return buildJvmAgentArgs(jarFile, tempDir, rawReportFile, excludedClasses)
    }

    override fun xmlReport(xmlFile: File, filters: ReportFilters, context: ReportContext) {
        context.koverXmlReport(xmlFile, filters)
    }

    override fun htmlReport(htmlDir: File, title: String, filters: ReportFilters, context: ReportContext) {
        context.koverHtmlReport(htmlDir, title, filters)
    }

    override fun verify(
        rules: List<VerificationRule>,
        commonFilters: ReportFilters,
        context: ReportContext
    ): List<RuleViolations> {
        return context.koverVerify(rules, commonFilters)
    }
}
