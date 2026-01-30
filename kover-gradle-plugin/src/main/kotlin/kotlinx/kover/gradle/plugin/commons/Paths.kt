/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.commons

import kotlinx.kover.features.jvm.KoverFeatures
import kotlinx.kover.gradle.plugin.tools.CoverageToolVariant
import java.io.File


internal fun agentFilePath(toolVariant: CoverageToolVariant): String {
    return if (toolVariant.vendor == CoverageToolVendor.KOVER) {
        "kover${separator}kover-jvm-agent-${KoverFeatures.version}.jar"
    } else {
        "kover${separator}jacoco-coverage-agent-${toolVariant.version}.jar"
    }
}

internal fun binReportsRootPath() = "kover${separator}bin-reports"

internal fun binReportPath(taskName: String, toolVendor: CoverageToolVendor): String {
    return "${binReportsRootPath()}${separator}${binReportName(taskName, toolVendor)}"
}

internal fun htmlReportPath(variant: String): String {
    return "reports${separator}kover${separator}html${variant.capitalized()}"
}

internal fun xmlReportPath(variant: String): String {
    return "reports${separator}kover${separator}report${variant.capitalized()}.xml"
}

internal fun binaryReportPath(variant: String): String {
    return "reports${separator}kover${separator}report${variant.capitalized()}.bin"
}

internal fun verificationErrorsPath(variant: String): String {
    return "reports${separator}kover${separator}verify${variant.capitalized()}.err"
}

internal fun coverageLogPath(variant: String): String {
    return "kover${separator}coverage${variant.capitalized()}.txt"
}

internal fun artifactFilePath(variant: String): String = "kover${separator}$variant.artifact"

private val separator = File.separatorChar

// assumption: Kotlin class-files are not placed in directories named 'classpath-snapshot' and 'cacheable'
internal fun File.isKotlinCompilerOutputDirectory(): Boolean {
    val fileName = name
    return fileName != "classpath-snapshot" && fileName != "cacheable"
}

// assumption: Java compiler places class-files in directories named 'classes'
internal fun File.isJavaCompilerOutputDirectory(): Boolean {
    return name == "classes"
}