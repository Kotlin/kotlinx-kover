/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.commons


internal const val FIND_JAR_TASK = "koverFindJar"

internal fun setupGenerationTask(setupId: SetupId) = "koverGenerateSetupArtifact${setupId.capitalized}"

internal fun htmlReportTaskName(setupId: SetupId) = "koverHtmlReport${setupId.capitalized}"

internal fun xmlReportTaskName(setupId: SetupId) = "koverXmlReport${setupId.capitalized}"

internal fun verifyTaskName(setupId: SetupId) = "koverVerify${setupId.capitalized}"

internal fun reportExtensionName(setupId: SetupId) = "kover${setupId.capitalized}Report"

internal fun rawReportName(taskName: String, toolVendor: CoverageToolVendor): String {
    return "${taskName}.${toolVendor.rawReportExtension}"
}

internal const val JVM_AGENT_CONFIGURATION_NAME = "koverJvmAgent"

internal const val JVM_REPORTER_CONFIGURATION_NAME = "koverJvmReporter"

internal fun setupConfigurationName(setupId: SetupId): String = "koverSetup${setupId.capitalized}"

internal fun aggSetupConfigurationName(setupId: SetupId): String = "koverAggregatedSetup${setupId.capitalized}"

