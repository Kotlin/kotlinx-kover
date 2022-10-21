/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.tools.commons

import kotlinx.kover.api.*
import kotlinx.kover.tools.kover.*
import kotlinx.kover.tools.jacoco.*
import kotlinx.kover.tasks.*
import org.gradle.api.*
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.*
import org.gradle.process.*
import java.io.*


@Suppress("UNUSED_PARAMETER")
internal object ToolManager {
    fun buildAgentArgs(
        details: ToolDetails,
        task: Task,
        reportFile: File,
        filters: AgentFilters
    ): MutableList<String> {
        return if (details.variant.vendor == CoverageToolVendor.KOVER) {
            task.buildKoverAgentJvmArgs(details.jarFile, reportFile, filters)
        } else {
            reportFile.parentFile.mkdirs()
            task.buildJacocoAgentJvmArgs(details.jarFile, reportFile, filters)
        }
    }

    fun report(
        details: ToolDetails,
        task: Task,
        exec: ExecOperations,
        projectFiles: Map<String, ProjectFiles>,
        filters: ReportFilters,
        xmlFile: File?,
        htmlDir: File?
    ) {
        if (details.variant.vendor == CoverageToolVendor.KOVER) {
            task.koverReport(exec, projectFiles, filters, xmlFile, htmlDir, details.classpath)
        } else {
            task.jacocoReport(projectFiles, filters, xmlFile, htmlDir, details.classpath)
        }
    }

    fun verify(
        details: ToolDetails,
        task: Task,
        exec: ExecOperations,
        projectFiles: Map<String, ProjectFiles>,
        filters: ReportFilters,
        rules: List<ReportVerificationRule>,
    ): String? {
        return if (details.variant.vendor == CoverageToolVendor.KOVER) {
            task.koverVerification(exec, projectFiles, filters, rules, details.classpath)
        } else {
            task.jacocoVerification(projectFiles, filters, rules, details.classpath)
        }
    }


    fun findJarFile(variant: CoverageToolVariant, config: Configuration, archiveOperations: ArchiveOperations): File {
        return if (variant.vendor == CoverageToolVendor.KOVER) {
            config.fileCollection { it.name == "intellij-coverage-agent" }.singleFile
        } else {
            val fatJar = config.fileCollection { it.name == "org.jacoco.agent" }.singleFile
            archiveOperations.zipTree(fatJar).filter { it.name == "jacocoagent.jar" }.singleFile
        }
    }

    fun dependencies(toolVariant: CoverageToolVariant): List<String> {
        return if (toolVariant.vendor == CoverageToolVendor.KOVER) {
            getKoverDependencies(toolVariant.version)
        } else {
            getJacocoDependencies(toolVariant.version)
        }
    }
}
