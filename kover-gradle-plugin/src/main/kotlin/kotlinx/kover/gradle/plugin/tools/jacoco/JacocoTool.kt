/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import kotlinx.kover.gradle.plugin.commons.ReportContext
import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import kotlinx.kover.gradle.plugin.tools.CoverageToolVariant
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.FileCollection
import java.io.File


internal class JacocoTool(override val variant: CoverageToolVariant) : CoverageTool {
    override val jvmAgentDependency: String = "org.jacoco:org.jacoco.agent:${variant.version}"

    override val jvmReporterDependency: String = "org.jacoco:org.jacoco.agent:${variant.version}"
    override val jvmReporterExtraDependency: String = "org.jacoco:org.jacoco.ant:${variant.version}"

    override fun findJvmAgentJar(classpath: FileCollection, archiveOperations: ArchiveOperations): File {
        val fatJar = classpath.filter { it.name.startsWith("org.jacoco.agent") }.singleFile
        return archiveOperations.zipTree(fatJar).filter { it.name == "jacocoagent.jar" }.singleFile
    }

    override fun jvmAgentArgs(
        jarFile: File,
        tempDir: File,
        binReportFile: File,
        excludedClasses: Set<String>
    ): List<String> {
        return buildJvmAgentArgs(jarFile, binReportFile, excludedClasses)
    }

    override fun xmlReport(xmlFile: File, context: ReportContext) {
        context.jacocoXmlReport(xmlFile)
    }

    override fun htmlReport(htmlDir: File, title: String, charset: String?, context: ReportContext) {
        context.jacocoHtmlReport(htmlDir, title, charset)
    }

    override fun verify(rules: List<VerificationRule>, outputFile: File, context: ReportContext) {
        context.jacocoVerify(rules, outputFile)
    }

}
