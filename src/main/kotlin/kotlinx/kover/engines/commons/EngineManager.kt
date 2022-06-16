/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.commons

import kotlinx.kover.api.*
import kotlinx.kover.engines.intellij.*
import kotlinx.kover.engines.jacoco.*
import kotlinx.kover.tasks.*
import org.gradle.api.*
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.*
import org.gradle.process.*
import java.io.*


@Suppress("UNUSED_PARAMETER")
internal object EngineManager {
    fun buildAgentArgs(
        details: EngineDetails,
        task: Task,
        reportFile: File,
        classFilter: KoverClassFilter
    ): MutableList<String> {
        return if (details.variant.vendor == CoverageEngineVendor.INTELLIJ) {
            task.buildIntellijAgentJvmArgs(details.jarFile, reportFile, classFilter)
        } else {
            reportFile.parentFile.mkdirs()
            task.buildJacocoAgentJvmArgs(details.jarFile, reportFile, classFilter)
        }
    }

    fun report(
        details: EngineDetails,
        task: Task,
        exec: ExecOperations,
        projectFiles: Map<String, ProjectFiles>,
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
        projectFiles: Map<String, ProjectFiles>,
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
