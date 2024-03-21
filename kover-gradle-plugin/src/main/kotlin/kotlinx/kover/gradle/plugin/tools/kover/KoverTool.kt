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

    // since Kover Features is in compile dependency and there is no need in additional dependency to reporter
    // we can't just specify null dependency, so use agent as a mock
    override val jvmReporterDependency: String = jvmAgentDependency
    override val jvmReporterExtraDependency: String = jvmAgentDependency


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
