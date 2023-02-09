/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.commons

import kotlinx.kover.gradle.plugin.dsl.*


internal const val FIND_JAR_TASK = "koverFindJar"

internal fun setupGenerationTask(setupId: SetupId) = "koverGenerateSetupArtifact${setupId.capitalized}"

internal fun htmlReportTaskName(setupId: SetupId) = "$REGULAR_HTML_REPORT_NAME${setupId.capitalized}"

internal fun xmlReportTaskName(setupId: SetupId) = "$REGULAR_XML_REPORT_NAME${setupId.capitalized}"

internal fun verifyTaskName(setupId: SetupId) = "$REGULAR_VERIFY_REPORT_NAME${setupId.capitalized}"

/**
 * TODO
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

internal fun setupConfigurationName(setupId: SetupId): String = "koverSetup${setupId.capitalized}"

internal fun aggSetupConfigurationName(setupId: SetupId): String = "koverAggregatedSetup${setupId.capitalized}"

