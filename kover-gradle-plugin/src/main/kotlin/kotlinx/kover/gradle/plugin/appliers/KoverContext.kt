/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import kotlinx.kover.gradle.plugin.appliers.tasks.VariantReportsSet
import kotlinx.kover.gradle.plugin.dsl.internal.KoverExtensionImpl
import kotlinx.kover.gradle.plugin.tasks.services.KoverAgentJarTask
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider

/**
 * Kover objects, created at the very beginning of configuration, immediately when applying the plugin.
 *
 * The presence of these objects does not depend on the user's settings, its will be used during further configuration.
 */
internal class KoverContext(
    val project: Project,
    val projectExtension: KoverExtensionImpl,
    val toolProvider: Provider<CoverageTool>,
    val findAgentJarTask: Provider<KoverAgentJarTask>,
    val koverBucketConfiguration: Configuration,
    val agentClasspath: Configuration,
    val reporterClasspath: Configuration,
    val totalReports: VariantReportsSet
)