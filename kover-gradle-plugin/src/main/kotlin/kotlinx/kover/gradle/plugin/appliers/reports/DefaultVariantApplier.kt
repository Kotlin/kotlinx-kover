/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers.reports

import kotlinx.kover.gradle.plugin.commons.DEFAULT_KOVER_VARIANT_NAME
import kotlinx.kover.gradle.plugin.commons.JvmCompilationKit
import kotlinx.kover.gradle.plugin.commons.ReportsVariantType
import kotlinx.kover.gradle.plugin.commons.VariantNameAttr
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.named

internal class DefaultVariantApplier(
    project: Project,
    koverDependencies: Configuration,
    reportClasspath: Configuration,
    toolProvider: Provider<CoverageTool>
) : ReportsVariantApplier(
    project,
    DEFAULT_KOVER_VARIANT_NAME,
    ReportsVariantType.DEFAULT,
    koverDependencies,
    reportClasspath,
    toolProvider
) {
    init {
        // always configure dependencies because variant name is constant for default reports
        dependencies.configure {
            attributes {
                attribute(VariantNameAttr.ATTRIBUTE, project.objects.named(DEFAULT_KOVER_VARIANT_NAME))
            }
        }
    }

    fun applyCompilationKit(kit: JvmCompilationKit) {
        applyCommonCompilationKit(kit)
    }
}
