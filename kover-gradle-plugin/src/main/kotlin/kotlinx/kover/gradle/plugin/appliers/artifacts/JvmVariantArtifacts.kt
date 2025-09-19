/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers.artifacts

import kotlinx.kover.gradle.plugin.commons.ReportVariantType
import kotlinx.kover.gradle.plugin.commons.VariantNameAttr
import kotlinx.kover.gradle.plugin.commons.VariantOriginAttr
import kotlinx.kover.gradle.plugin.dsl.internal.KoverVariantConfigImpl
import kotlinx.kover.gradle.plugin.appliers.origin.JvmVariantOrigin
import kotlinx.kover.gradle.plugin.commons.JVM_VARIANT_NAME
import kotlinx.kover.gradle.plugin.dsl.KoverVariantSources
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.named

internal class JvmVariantArtifacts(
    variantName: String,
    project: Project,
    toolProvider: Provider<CoverageTool>,
    koverBucketConfiguration: Configuration,
    variantOrigin: JvmVariantOrigin,
    variantConfig: KoverVariantConfigImpl,
    projectExtension: KoverProjectExtensionImpl
) : AbstractVariantArtifacts(
    project,
    variantName,
    toolProvider,
    koverBucketConfiguration,
    variantConfig,
    projectExtension
) {
    init {
        producerConfiguration.configure {
            attributes {
                attribute(VariantOriginAttr.ATTRIBUTE, project.objects.named(ReportVariantType.JVM.name))
            }
        }

        consumerConfiguration.configure {
            attributes {
                // depends on JVM-only variants
                attribute(VariantOriginAttr.ATTRIBUTE, project.objects.named(ReportVariantType.JVM.name))
                attribute(VariantNameAttr.ATTRIBUTE, project.objects.named(variantName))
            }
        }

        fromOrigin(variantOrigin) { compilationName ->
            !compilationIsExcluded(compilationName, variantConfig.sources)
        }
    }

    private fun compilationIsExcluded(compilationName: String, variant: KoverVariantSources): Boolean {
        if (compilationName in variant.excludedSourceSets.get()) {
            return true
        }

        val included = variant.includedSourceSets.get()

        if (included.isEmpty() && compilationName == "test") {
            return true
        }
        if (included.isNotEmpty() && compilationName !in included) {
            return true
        }

        return false
    }

}