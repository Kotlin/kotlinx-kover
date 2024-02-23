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
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.named

internal class JvmVariantArtifacts(
    project: Project,
    toolProvider: Provider<CoverageTool>,
    koverBucketConfiguration: Configuration,
    variantOrigin: JvmVariantOrigin,
    variantConfig: KoverVariantConfigImpl
) : AbstractVariantArtifacts(project, JVM_VARIANT_NAME, toolProvider, koverBucketConfiguration, variantConfig) {
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
            compilationName !in variantConfig.sources.excludedSourceSets.get()
                    && compilationName != "test"
        }
    }

}