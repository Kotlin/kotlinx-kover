/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import kotlinx.kover.gradle.plugin.commons.ReportContext
import kotlinx.kover.gradle.plugin.commons.ReportFilters
import kotlinx.kover.gradle.plugin.commons.VerificationRule
import kotlinx.kover.gradle.plugin.tools.*
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import kotlinx.kover.gradle.plugin.tools.RuleViolations
import org.gradle.api.file.*
import java.io.*


internal class JacocoTool(override val variant: CoverageToolVariant) : CoverageTool {
    override val jvmAgentDependency: String = "org.jacoco:org.jacoco.agent:${variant.version}"

    override val jvmReporterDependencies: List<String> = listOf(
        "org.jacoco:org.jacoco.agent:${variant.version}",
        "org.jacoco:org.jacoco.ant:${variant.version}"
    )

    override fun findJvmAgentJar(classpath: FileCollection, archiveOperations: ArchiveOperations): File {
        val fatJar = classpath.filter { it.name.startsWith("org.jacoco.agent") }.singleFile
        return archiveOperations.zipTree(fatJar).filter { it.name == "jacocoagent.jar" }.singleFile
    }

    override fun jvmAgentArgs(
        jarFile: File,
        tempDir: File,
        rawReportFile: File,
        excludedClasses: Set<String>
    ): List<String> {
        return buildJvmAgentArgs(jarFile, rawReportFile, excludedClasses)
    }

    override fun xmlReport(xmlFile: File, filters: ReportFilters, context: ReportContext) {
        context.jacocoXmlReport(xmlFile, filters)
    }

    override fun htmlReport(htmlDir: File, title: String, filters: ReportFilters, context: ReportContext) {
        context.jacocoHtmlReport(htmlDir, title, filters)
    }

    override fun verify(rules: List<VerificationRule>, commonFilters: ReportFilters, context: ReportContext): List<RuleViolations> {
        return context.jacocoVerify(rules, commonFilters)
    }

}
