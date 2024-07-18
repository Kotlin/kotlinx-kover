/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.mojo

import kotlinx.kover.features.jvm.ClassFilters
import kotlinx.kover.maven.plugin.mojo.abstracts.AbstractCoverageTaskMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import java.io.File
import java.lang.StringBuilder

/**
 * Mojo to print debug info about directories and files using in generating reports and verification.
 *
 * It is intended for debugging only.
 */
@Mojo(name = "print-artifact-info", defaultPhase = LifecyclePhase.VERIFY)
class ArtifactMojo: AbstractCoverageTaskMojo() {
    override fun processCoverage(
        binaryReports: List<File>,
        outputDirs: List<File>,
        sourceDirs: List<File>,
        filters: ClassFilters
    ) {
        val builder = StringBuilder()
        builder.appendLine("Binary reports")
        binaryReports.forEach { report ->
            builder.appendLine(report.toRelativeString(project.basedir))
        }
        builder.appendLine()

        builder.appendLine("Source root directories")
        sourceDirs.forEach { dir ->
            builder.appendLine(dir.toRelativeString(project.basedir))
        }
        builder.appendLine()

        builder.appendLine("Target root directories")
        outputDirs.forEach { dir ->
            builder.appendLine(dir.toRelativeString(project.basedir))
        }

        log.info("Kover artifact\n\n${builder}")
    }

}