/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.commons

import java.util.*

/**
 * ID of Kover Gradle Plugin.
 */
internal const val KOVER_PLUGIN_ID = "org.jetbrains.kotlinx.kover"

/**
 * Name of the project extension to configure Kover plugin.
 */
public const val KOVER_PROJECT_EXTENSION_NAME = "kover"

/**
 * Name of the configuration to add dependency on Kover reports from another project.
 */
public const val KOVER_DEPENDENCY_NAME = "kover"

/**
 * Name of reports variant for total report.
 *
 * It is an empty string in order to accurately avoid clashes with custom names.
 */
internal const val TOTAL_VARIANT_NAME = ""

/**
 * Name of task to find online instrumentation agent jar file.
 */
internal const val FIND_JAR_TASK = "koverFindJar"

/**
 * Name of the XML report generation task for Kotlin JVM and Kotlin multiplatform projects.
 */
internal const val XML_REPORT_NAME = "koverXmlReport"

/**
 * Name of the binary report generation task for Kotlin JVM and Kotlin multiplatform projects.
 */
internal const val BINARY_REPORT_NAME = "koverBinaryReport"

/**
 * Name of the HTML report generation task for Kotlin JVM and Kotlin multiplatform projects.
 */
internal const val HTML_REPORT_NAME = "koverHtmlReport"

/**
 * Name of the verification task for Kotlin JVM and Kotlin multiplatform projects.
 */
internal const val VERIFY_REPORT_NAME = "koverVerify"

/**
 * Name of the coverage logging task for Kotlin JVM and Kotlin multiplatform projects.
 */
internal const val LOG_REPORT_NAME = "koverLog"

/**
 * Name of reports variant for JVM targets.
 * It includes all code from a project using the Kotlin/JVM plugin, or the code of the JVM target from a project using Kotlin/Multiplatform.
 */
internal const val JVM_VARIANT_NAME = "jvm"

/**
 * Name for task for generating Kover artifact.
 */
internal fun artifactGenerationTaskName(variant: String) = "koverGenerateArtifact${variant.capitalized()}"

/**
 * Name for HTML reporting task for specified report namespace.
 */
internal fun htmlReportTaskName(variant: String) = "$HTML_REPORT_NAME${variant.capitalized()}"

/**
 * Name for XML reporting task for specified report namespace.
 */
internal fun xmlReportTaskName(variant: String) = "$XML_REPORT_NAME${variant.capitalized()}"

/**
 * Name for binary reporting task for specified report namespace.
 */
internal fun binaryReportTaskName(variant: String) = "$BINARY_REPORT_NAME${variant.capitalized()}"

/**
 * Name for cached verifying task for specified report namespace.
 */
internal fun verifyCachedTaskName(variant: String) = "koverCachedVerify${variant.capitalized()}"
/**
 * Name for verifying task for specified report namespace.
 */
internal fun verifyTaskName(variant: String) = "$VERIFY_REPORT_NAME${variant.capitalized()}"

/**
 * Name for coverage logging task.
 */
internal fun logTaskName(variant: String) = "$LOG_REPORT_NAME${variant.capitalized()}"

/**
 * Name for task to print coverage to the log.
 */
internal fun printLogTaskName(variant: String) = "koverPrintCoverage${variant.capitalized()}"

/**
 * Name of binary report for specified test task name (without directory path).
 */
internal fun binReportName(taskName: String, toolVendor: CoverageToolVendor): String {
    return "${taskName}.${toolVendor.binReportExtension}"
}

/**
 * Name of transitive configuration to collect classpath of selected JVM runtime instrumentation agent.
 */
internal const val JVM_AGENT_CONFIGURATION_NAME = "koverJvmAgent"

/**
 * Name of transitive configuration to collect classpath of selected JVM report generator.
 */
internal const val JVM_REPORTER_CONFIGURATION_NAME = "koverJvmReporter"

/**
 * Name of the Gradle configuration for sharing Kover artifact.
 */
internal fun artifactConfigurationName(variantName: String): String = "koverArtifact${variantName.capitalized()}"

/**
 * Name of the Gradle configuration for collecting Kover artifacts from dependencies.
 */
internal fun externalArtifactConfigurationName(variantName: String): String = "koverExternalArtifacts${variantName.capitalized()}"

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
internal fun String.capitalized(): String {
    return when {
        isEmpty() -> this
        length == 1 -> (this as java.lang.String).toUpperCase(Locale.ROOT)
        else -> Character.toUpperCase(this[0]) + this.substring(1)
    }
}
