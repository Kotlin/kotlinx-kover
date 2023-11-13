/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import kotlinx.kover.gradle.plugin.appliers.tasks.VariantTasks
import kotlinx.kover.gradle.plugin.appliers.artifacts.*
import kotlinx.kover.gradle.plugin.appliers.instrumentation.instrument
import kotlinx.kover.gradle.plugin.appliers.origin.AndroidVariantOrigin
import kotlinx.kover.gradle.plugin.appliers.origin.JvmVariantOrigin
import kotlinx.kover.gradle.plugin.appliers.origin.AllVariantOrigins
import kotlinx.kover.gradle.plugin.commons.JVM_VARIANT_NAME
import kotlinx.kover.gradle.plugin.commons.KoverIllegalConfigException
import kotlinx.kover.gradle.plugin.commons.ReportVariantType
import kotlinx.kover.gradle.plugin.commons.TOTAL_VARIANT_NAME
import kotlinx.kover.gradle.plugin.dsl.internal.KoverReportSetConfigImpl
import kotlinx.kover.gradle.plugin.dsl.internal.KoverVariantCreateConfigImpl
import org.gradle.kotlin.dsl.newInstance


/**
 * The second stage of applying the Kover plugin.
 *
 * Objects are created that depend on the full configuration of the project: the availability of Kotlin plugins,
 * the availability and settings of the Android plugin, the user settings of the Kover plugin itself.
 */
internal fun KoverContext.finalizing(origins: AllVariantOrigins) {
    val jvmVariant =
        origins.jvm?.createVariant(this, variantConfig(JVM_VARIANT_NAME))

    if (jvmVariant != null) {
        VariantTasks(
            project,
            JVM_VARIANT_NAME,
            ReportVariantType.JVM,
            toolProvider,
            reportsConfig(JVM_VARIANT_NAME, project.path),
            reporterClasspath
        ).assign(jvmVariant)
    }

    val androidVariants = origins.android.map { providedDetails ->
        providedDetails.createVariant(this, variantConfig(providedDetails.buildVariant.buildVariant))
    }

    val variantArtifacts = mutableMapOf<String, AbstractVariantArtifacts>()
    jvmVariant?.let { variantArtifacts[JVM_VARIANT_NAME] = it }
    androidVariants.forEach { variantArtifacts[it.variantName] = it }

    val availableVariants = variantArtifacts.keys + projectExtension.variants.customVariants.keys
    projectExtension.reports.byName.forEach { (requestedVariant, _) ->
        if (requestedVariant !in availableVariants) {
            throw KoverIllegalConfigException("It is not possible to configure the '$requestedVariant' variant because it does not exist")
        }
    }

    val totalVariant =
        TotalVariantArtifacts(project, toolProvider, koverBucketConfiguration, variantConfig(TOTAL_VARIANT_NAME))
    variantArtifacts.values.forEach { totalVariant.mergeWith(it) }
    totalVariantTasks.assign(totalVariant)

    projectExtension.variants.providedVariants.forEach { (name, _) ->
        if (name !in variantArtifacts) {
            throw KoverIllegalConfigException("It is unacceptable to configure provided variant '$name', since there is no such variant in the project.\nAcceptable variants: ${variantArtifacts.keys}")
        }
    }

    projectExtension.variants.customVariants.forEach { (name, config) ->
        if (name == JVM_VARIANT_NAME) {
            throw KoverIllegalConfigException("It is unacceptable to create a custom reports variant '$JVM_VARIANT_NAME', because this name is reserved for JVM code")
        }
        if (name in variantArtifacts) {
            throw KoverIllegalConfigException("It is unacceptable to create a custom reports variant '$name', because this name is reserved for provided Android reports variant.")
        }

        val customVariant =
            CustomVariantArtifacts(project, name, toolProvider,  koverBucketConfiguration, config)

        config.variantsByName.forEach { (mergedName, optionality) ->
            val mergedVariant = variantArtifacts[mergedName]
            if (mergedVariant != null) {
                if (optionality.withDependencies) {
                    customVariant.mergeWithDependencies(mergedVariant)
                } else {
                    customVariant.mergeWith(mergedVariant)
                }
            } else {
                if (!optionality.optional) {
                    throw KoverIllegalConfigException("Could not find the provided variant '$mergedName' to create a custom variant '$name'.\nSpecify an existing 'jvm' variant or Android build variant name, or delete the merge.")
                }
            }
        }

        VariantTasks(
            project,
            name,
            ReportVariantType.CUSTOM,
            toolProvider,
            reportsConfig(name, project.path),
            reporterClasspath
        ).assign(customVariant)

    }

    androidVariants.forEach { androidVariant ->
        VariantTasks(
            project,
            androidVariant.variantName,
            ReportVariantType.ANDROID,
            toolProvider,
            reportsConfig(androidVariant.variantName, project.path),
            reporterClasspath
        ).assign(androidVariant)
    }
}

private fun KoverContext.variantConfig(variantName: String): KoverVariantCreateConfigImpl {
    return projectExtension.variants.customVariants.getOrElse(variantName) {
        val variantConfig = projectExtension.variants.objects.newInstance<KoverVariantCreateConfigImpl>(variantName)
        variantConfig.deriveFrom(projectExtension.variants)
        variantConfig
    }
}

private fun KoverContext.reportsConfig(variantName: String, projectPath: String): KoverReportSetConfigImpl {
    return projectExtension.reports.byName.getOrElse(variantName) {
        projectExtension.reports.createVariant(variantName, projectPath)
    }
}

private fun JvmVariantOrigin.createVariant(
    koverContext: KoverContext,
    config: KoverVariantCreateConfigImpl,
): JvmVariantArtifacts {
    tests.instrument(koverContext, config.instrumentation.excludedClasses)
    return JvmVariantArtifacts(
        koverContext.project,
        koverContext.toolProvider,
        koverContext.koverBucketConfiguration,
        this,
        config
    )
}

private fun AndroidVariantOrigin.createVariant(
    koverContext: KoverContext,
    config: KoverVariantCreateConfigImpl,
): AndroidVariantArtifacts {
    tests.instrument(koverContext, config.instrumentation.excludedClasses)
    return AndroidVariantArtifacts(
        koverContext.project,
        buildVariant.buildVariant,
        koverContext.toolProvider,
        koverContext.koverBucketConfiguration,
        this,
        config
    )
}
