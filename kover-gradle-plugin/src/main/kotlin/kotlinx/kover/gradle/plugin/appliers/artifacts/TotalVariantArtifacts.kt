/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers.artifacts

import kotlinx.kover.gradle.plugin.commons.ReportVariantType
import kotlinx.kover.gradle.plugin.commons.TOTAL_VARIANT_NAME
import kotlinx.kover.gradle.plugin.commons.VariantNameAttr
import kotlinx.kover.gradle.plugin.commons.VariantOriginAttr
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import kotlinx.kover.gradle.plugin.dsl.internal.KoverVariantConfigImpl
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.named

internal class TotalVariantArtifacts(
    project: Project,
    toolProvider: Provider<CoverageTool>,
    koverBucketConfiguration: Configuration,
    variantConfig: KoverVariantConfigImpl,
    projectExtension: KoverProjectExtensionImpl
) : AbstractVariantArtifacts(
    project,
    TOTAL_VARIANT_NAME,
    toolProvider,
    koverBucketConfiguration,
    variantConfig,
    projectExtension
) {
    init {
        producerConfiguration.configure {
            attributes {
                attribute(VariantOriginAttr.ATTRIBUTE, project.objects.named(ReportVariantType.TOTAL.name))
            }
        }

        consumerConfiguration.configure {
            attributes {
                // depends on total-only variants
                attribute(VariantOriginAttr.ATTRIBUTE, project.objects.named(ReportVariantType.TOTAL.name))
                attribute(VariantNameAttr.ATTRIBUTE, project.objects.named(TOTAL_VARIANT_NAME))
            }
        }
    }

    fun mergeWith(otherVariant: AbstractVariantArtifacts) {
        artifactGenTask.configure {
            /*
            We take only `artifactGenTask` but not the `consumerConfiguration` because total task
            takes only dependencies with origin total.

            This will protect from the problem if there is no overlapping set of variants in the dependencies:
            e.g. current project have `custom` report variant, but it missed in dependency.
             */
            additionalArtifacts.from(otherVariant.artifactGenTask.map { task -> task.artifactFile })
            dependsOn(otherVariant.artifactGenTask)
        }
    }
}