/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import kotlinx.kover.features.jvm.KoverFeatures
import kotlinx.kover.gradle.plugin.commons.ReportContext
import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.tools.CoverageRequest
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import kotlinx.kover.gradle.plugin.tools.CoverageToolVariant
import org.gradle.api.GradleException
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.FileCollection
import java.io.File


internal class KoverTool(override val variant: CoverageToolVariant) : CoverageTool {
    override val jvmAgentDependency: String = "org.jetbrains.kotlinx:kover-jvm-agent:${KoverFeatures.getVersion()}"

    override val jvmReporterDependency: String = "org.jetbrains.intellij.deps:intellij-coverage-reporter:${variant.version}"
    override val jvmReporterExtraDependency: String = "org.jetbrains.intellij.deps:intellij-coverage-reporter:${variant.version}"


    override fun findJvmAgentJar(classpath: FileCollection, archiveOperations: ArchiveOperations): File {
        return classpath.filter { it.name.startsWith("kover-jvm-agent") }.files.firstOrNull()
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

    override fun xmlReport(xmlFile: File, title: String, context: ReportContext) {
        context.koverXmlReport(xmlFile, title)
    }

    override fun htmlReport(htmlDir: File, title: String, charset: String?, context: ReportContext) {
        context.koverHtmlReport(htmlDir, title, charset)
    }

    override fun binaryReport(binary: File, context: ReportContext) {
        context.koverBinaryReport(binary)
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
