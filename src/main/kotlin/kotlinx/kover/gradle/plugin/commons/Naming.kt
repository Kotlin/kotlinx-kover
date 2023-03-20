/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.commons

import kotlinx.kover.gradle.plugin.dsl.KoverNames.REGULAR_HTML_REPORT_NAME
import kotlinx.kover.gradle.plugin.dsl.KoverNames.REGULAR_VERIFY_REPORT_NAME
import kotlinx.kover.gradle.plugin.dsl.KoverNames.REGULAR_XML_REPORT_NAME

/**
 * Name of task to find online instrumentation agent jar file.
 */
internal const val FIND_JAR_TASK = "koverFindJar"

/**
 * Name for task for generating Kover setup artifact.
 */
internal fun setupGenerationTask(setupId: SetupId) = "koverGenerateSetupArtifact${setupId.capitalized}"

/**
 * Name for HTML reporting task for specified Kover setup.
 */
internal fun htmlReportTaskName(setupId: SetupId) = "$REGULAR_HTML_REPORT_NAME${setupId.capitalized}"

/**
 * Name for XML reporting task for specified Kover setup.
 */
internal fun xmlReportTaskName(setupId: SetupId) = "$REGULAR_XML_REPORT_NAME${setupId.capitalized}"

/**
 * Name for verifying task for specified Kover setup.
 */
internal fun verifyTaskName(setupId: SetupId) = "$REGULAR_VERIFY_REPORT_NAME${setupId.capitalized}"

/**
 * Name of raw report for specified task name (without directory path).
 */
internal fun rawReportName(taskName: String, toolVendor: CoverageToolVendor): String {
    return "${taskName}.${toolVendor.rawReportExtension}"
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
 * Name of the Gradle configuration for sharing Kover setup artifact.
 */
internal fun setupConfigurationName(setupId: SetupId): String = "koverSetup${setupId.capitalized}"

/**
 * Name of the Gradle configuration for collecting Kover setup artifacts from another projects.
 */
internal fun aggSetupConfigurationName(setupId: SetupId): String = "koverAggregatedSetup${setupId.capitalized}"
