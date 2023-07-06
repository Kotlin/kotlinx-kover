/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import kotlinx.kover.gradle.plugin.commons.ArtifactContent
import kotlinx.kover.gradle.plugin.commons.ReportContext
import kotlinx.kover.gradle.plugin.commons.ReportFilters
import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.tools.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters
import java.io.*


internal class KoverTool(override val variant: CoverageToolVariant) : CoverageTool {
    override val jvmAgentDependency: String = "org.jetbrains.intellij.deps:intellij-coverage-agent:${variant.version}"

    override val jvmReporterDependency: String = "org.jetbrains.intellij.deps:intellij-coverage-reporter:${variant.version}"
    override val jvmReporterExtraDependency: String = "org.jetbrains.intellij.deps:intellij-coverage-reporter:${variant.version}"


    override fun findJvmAgentJar(classpath: FileCollection, archiveOperations: ArchiveOperations): File {
        return classpath.filter { it.name.startsWith("intellij-coverage-agent") }.files.firstOrNull()
            ?: throw GradleException("JVM instrumentation agent not found for Kover Coverage Tool")
    }

    override fun jvmAgentArgs(
        jarFile: File,
        tempDir: File,
        binReportFile: File,
        excludedClasses: Set<String>
    ): List<String> {
        return buildJvmAgentArgs(jarFile, tempDir, binReportFile, excludedClasses)
    }

    override fun xmlReport(xmlFile: File, context: ReportContext) {
        context.koverXmlReport(xmlFile)
    }

    override fun htmlReport(htmlDir: File, title: String, charset: String?, context: ReportContext) {
        context.koverHtmlReport(htmlDir, title, charset)
    }

    override fun icReport(icFile: File, context: ReportContext) {
        context.koverIcReport(icFile)
    }

    override fun verify(
        rules: List<VerificationRule>,
        outputFile: File,
        context: ReportContext
    ) {
        context.koverVerify(rules, outputFile)
    }

    override fun collectCoverage(request: CoverageRequest, outputFile: File, context: ReportContext) {
        context.printCoverage(request, outputFile)
    }
}


internal interface ReportParameters: WorkParameters {
    val filters: Property<ReportFilters>

    val files: Property<ArtifactContent>
    val tempDir: DirectoryProperty
    val projectPath: Property<String>
    val charset: Property<String>
}


internal interface VerifyReportParameters: ReportParameters {
    val outputFile: RegularFileProperty
    val rules: ListProperty<VerificationRule>
}