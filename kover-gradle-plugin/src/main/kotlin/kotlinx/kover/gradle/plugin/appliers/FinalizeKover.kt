/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import kotlinx.kover.gradle.plugin.appliers.tasks.VariantReportsSet
import kotlinx.kover.gradle.plugin.appliers.artifacts.*
import kotlinx.kover.gradle.plugin.appliers.instrumentation.instrument
import kotlinx.kover.gradle.plugin.appliers.origin.AndroidVariantOrigin
import kotlinx.kover.gradle.plugin.appliers.origin.JvmVariantOrigin
import kotlinx.kover.gradle.plugin.appliers.origin.AllVariantOrigins
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.JVM_VARIANT_NAME
import kotlinx.kover.gradle.plugin.commons.KoverIllegalConfigException
import kotlinx.kover.gradle.plugin.commons.ReportVariantType
import kotlinx.kover.gradle.plugin.commons.TOTAL_VARIANT_NAME
import kotlinx.kover.gradle.plugin.dsl.internal.KoverReportSetConfigImpl
import kotlinx.kover.gradle.plugin.dsl.internal.KoverVariantCreateConfigImpl
import kotlinx.kover.gradle.plugin.util.bean
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.newInstance


/**
 * The second stage of applying the Kover plugin.
 *
 * Objects are created that depend on the full configuration of the project: the availability of Kotlin plugins,
 * the availability and settings of the Android plugin, the user settings of the Kover plugin itself.
 */
internal fun KoverContext.finalizing(origins: AllVariantOrigins) {
    validateKoverDependencies()

    projectExtension.finalizeActions.forEach { action ->
        try {
            action()
        } catch (e: Exception) {
            throw KoverCriticalException("An error occurred while executing before Kover finalize action", e)
        }
    }

    val jvmVariants = origins.jvm.map { providedDetails ->
        providedDetails.createVariant(providedDetails.targetName, this, variantConfig(providedDetails.targetName))
    }

    jvmVariants.forEach { variant ->
        VariantReportsSet(
            project,
            variant.variantName,
            ReportVariantType.JVM,
            toolProvider,
            reportsConfig(variant.variantName, project.path),
            reporterClasspath,
            projectExtension.koverDisabled
        ).assign(variant)
    }

    val androidVariants = origins.android.map { providedDetails ->
        providedDetails.createVariant(this, variantConfig(providedDetails.buildVariant.buildVariant))
    }

    val variantArtifacts = mutableMapOf<String, AbstractVariantArtifacts>()
    jvmVariants.forEach { variantArtifacts[it.variantName] = it }
    androidVariants.forEach { variantArtifacts[it.variantName] = it }

    val totalVariant =
        TotalVariantArtifacts(project, toolProvider, koverBucketConfiguration, variantConfig(TOTAL_VARIANT_NAME), projectExtension)
    variantArtifacts.values.forEach { totalVariant.mergeWith(it) }
    totalReports.assign(totalVariant)

    projectExtension.currentProject.providedVariants.forEach { (name, _) ->
        if (name !in variantArtifacts) {
            throw KoverIllegalConfigException("It is unacceptable to configure provided variant '$name', since there is no such variant in the project.\nAcceptable variants: ${variantArtifacts.keys}")
        }
    }

    projectExtension.currentProject.customVariants.forEach { (name, config) ->
        if (name == JVM_VARIANT_NAME) {
            throw KoverIllegalConfigException("It is unacceptable to create a custom reports variant '$JVM_VARIANT_NAME', because this name is reserved for JVM code")
        }
        if (name in variantArtifacts) {
            throw KoverIllegalConfigException("It is unacceptable to create a custom reports variant '$name', because this name is reserved for provided Android reports variant.")
        }

        val customVariant =
            CustomVariantArtifacts(project, name, toolProvider,  koverBucketConfiguration, config, projectExtension)

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

        variantArtifacts[name] = customVariant

        VariantReportsSet(
            project,
            name,
            ReportVariantType.CUSTOM,
            toolProvider,
            reportsConfig(name, project.path),
            reporterClasspath,
            projectExtension.koverDisabled
        ).assign(customVariant)
    }

    projectExtension.currentProject.variantsToCopy.forEach { (name, originVariantName) ->
        val originalVariant = variantArtifacts[originVariantName]
            ?: throw KoverIllegalConfigException("Cannot create a variant '$name': the original variant '$originVariantName' does not exist.")

        VariantReportsSet(
            project,
            name,
            ReportVariantType.CUSTOM,
            toolProvider,
            reportsConfig(name, project.path),
            reporterClasspath,
            projectExtension.koverDisabled
        ).assign(originalVariant)
    }

    androidVariants.forEach { androidVariant ->
        VariantReportsSet(
            project,
            androidVariant.variantName,
            ReportVariantType.ANDROID,
            toolProvider,
            reportsConfig(androidVariant.variantName, project.path),
            reporterClasspath,
            projectExtension.koverDisabled
        ).assign(androidVariant)
    }

    projectExtension.reports.byName.forEach { (requestedVariant, _) ->
        if (requestedVariant !in variantArtifacts && requestedVariant !in projectExtension.currentProject.variantsToCopy) {
            throw KoverIllegalConfigException("It is not possible to configure the '$requestedVariant' variant because it does not exist")
        }
    }
}

private fun KoverContext.validateKoverDependencies() {
    val duplicateComponents = koverBucketConfiguration.dependencies
        .filterIsInstance<ProjectDependency>()
        .groupBy { dependency -> dependency.group to dependency.name }
        .mapValues { (_, dependencies) -> dependencies.map { dependency -> dependency.projectPath() }.distinct() }
        .filterValues { projectPaths -> projectPaths.size > 1 }

    if (duplicateComponents.isEmpty()) return

    val components = duplicateComponents.entries.joinToString("; ") { (identity, paths) ->
        val (group, name) = identity
        "'${group.orEmpty()}:$name' is used by ${paths.joinToString { path -> "'$path'" }}"
    }
    throw KoverIllegalConfigException(
        "Kover cannot resolve project dependencies with duplicate component identities: $components. " +
                "Assign a unique group or project name to each project."
    )
}

/**
 * Gets a project path dynamically, depending on a Gradle version
 */
private fun ProjectDependency.projectPath(): String {
    val dependency = bean()
    // Gradle since 8.11 has path property
    return dependency.valueOrNull<String>("path")
        // for Gradle < 8.11 dependencyProject was used
        ?: dependency.bean("dependencyProject").value("path")
}

private fun KoverContext.variantConfig(variantName: String): KoverVariantCreateConfigImpl {
    return projectExtension.currentProject.customVariants.getOrElse(variantName) {
        val variantConfig = projectExtension.currentProject.objects.newInstance<KoverVariantCreateConfigImpl>(variantName)
        variantConfig.deriveFrom(projectExtension.currentProject)
        variantConfig
    }
}

private fun KoverContext.reportsConfig(variantName: String, projectPath: String): KoverReportSetConfigImpl {
    return projectExtension.reports.byName.getOrElse(variantName) {
        projectExtension.reports.createReportSet(variantName, projectPath)
    }
}

private fun JvmVariantOrigin.createVariant(
    variantName: String,
    koverContext: KoverContext,
    config: KoverVariantCreateConfigImpl,
): JvmVariantArtifacts {
    tests.instrument(koverContext, koverContext.projectExtension.koverDisabled, koverContext.projectExtension.currentProject)
    return JvmVariantArtifacts(
        variantName,
        koverContext.project,
        koverContext.toolProvider,
        koverContext.koverBucketConfiguration,
        this,
        config,
        koverContext.projectExtension
    )
}

private fun AndroidVariantOrigin.createVariant(
    koverContext: KoverContext,
    config: KoverVariantCreateConfigImpl,
): AndroidVariantArtifacts {
    tests.instrument(koverContext, koverContext.projectExtension.koverDisabled, koverContext.projectExtension.currentProject)
    return AndroidVariantArtifacts(
        koverContext.project,
        buildVariant.buildVariant,
        koverContext.toolProvider,
        koverContext.koverBucketConfiguration,
        this,
        config,
        koverContext.projectExtension
    )
}
