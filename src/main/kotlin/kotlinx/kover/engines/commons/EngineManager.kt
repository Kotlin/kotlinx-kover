/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.commons

import kotlinx.kover.api.CoverageEngineVariant
import kotlinx.kover.api.CoverageEngineVendor
import kotlinx.kover.api.KoverClassFilter
import kotlinx.kover.engines.intellij.buildIntellijAgentJvmArgs
import kotlinx.kover.engines.intellij.getIntellijDependencies
import kotlinx.kover.engines.intellij.intellijReport
import kotlinx.kover.engines.intellij.intellijVerification
import kotlinx.kover.engines.jacoco.buildJacocoAgentJvmArgs
import kotlinx.kover.engines.jacoco.getJacocoDependencies
import kotlinx.kover.engines.jacoco.jacocoReport
import kotlinx.kover.engines.jacoco.jacocoVerification
import kotlinx.kover.tasks.EngineDetails
import kotlinx.kover.tasks.ProjectFiles
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ArchiveOperations
import org.gradle.process.ExecOperations
import java.io.File


@Suppress("UNUSED_PARAMETER")
internal object EngineManager {
    fun buildAgentArgs(
        details: EngineDetails,
        task: Task,
        reportFile: File,
        filters: AgentFilters
    ): MutableList<String> {
        return if (details.variant.vendor == CoverageEngineVendor.INTELLIJ) {
            task.buildIntellijAgentJvmArgs(details.jarFile, reportFile, filters)
        } else {
            reportFile.parentFile.mkdirs()
            task.buildJacocoAgentJvmArgs(details.jarFile, reportFile, filters)
        }
    }

    fun report(
        details: EngineDetails,
        task: Task,
        exec: ExecOperations,
        projectFiles: ProjectFiles,
        classFilter: KoverClassFilter,
        xmlFile: File?,
        htmlDir: File?
    ) {
        if (details.variant.vendor == CoverageEngineVendor.INTELLIJ) {
            task.intellijReport(exec, projectFiles, classFilter, xmlFile, htmlDir, details.classpath)
        } else {
            task.jacocoReport(projectFiles, xmlFile, htmlDir, details.classpath)
        }
    }

    fun verify(
        details: EngineDetails,
        task: Task,
        exec: ExecOperations,
        projectFiles: ProjectFiles,
        classFilter: KoverClassFilter,
        rules: List<ReportVerificationRule>,
    ): String? {
        return if (details.variant.vendor == CoverageEngineVendor.INTELLIJ) {
            task.intellijVerification(exec, projectFiles, classFilter, rules, details.classpath)
        } else {
            task.jacocoVerification(projectFiles, rules, details.classpath)
        }
    }


    fun findJarFile(variant: CoverageEngineVariant, config: Configuration, archiveOperations: ArchiveOperations): File {
        return if (variant.vendor == CoverageEngineVendor.INTELLIJ) {
            config.fileCollection { it.name == "intellij-coverage-agent" }.singleFile
        } else {
            val fatJar = config.fileCollection { it.name == "org.jacoco.agent" }.singleFile
            archiveOperations.zipTree(fatJar).filter { it.name == "jacocoagent.jar" }.singleFile
        }
    }

    fun dependencies(engineVariant: CoverageEngineVariant): List<String> {
        return if (engineVariant.vendor == CoverageEngineVendor.INTELLIJ) {
            getIntellijDependencies(engineVariant.version)
        } else {
            getJacocoDependencies(engineVariant.version)
        }
    }
}
