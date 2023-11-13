/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.AndroidBuildVariant
import kotlinx.kover.gradle.plugin.commons.AndroidFallbacks
import kotlinx.kover.gradle.plugin.commons.AndroidFlavor
import kotlinx.kover.gradle.plugin.commons.KoverIllegalConfigException
import kotlinx.kover.gradle.plugin.appliers.origin.AndroidVariantOrigin
import kotlinx.kover.gradle.plugin.appliers.origin.CompilationDetails
import kotlinx.kover.gradle.plugin.util.DynamicBean
import kotlinx.kover.gradle.plugin.util.bean
import kotlinx.kover.gradle.plugin.util.hasSuperclass
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType


/**
 * Locate Android compilation kits for the given Kotlin Target.
 */
internal fun Project.androidCompilationKits(
    androidExtension: DynamicBean,
    kotlinTarget: DynamicBean
): List<AndroidVariantOrigin> {
    val variants = if ("applicationVariants" in androidExtension) {
        androidExtension.beanCollection("applicationVariants")
    } else {
        androidExtension.beanCollection("libraryVariants")
    }

    val fallbacks = findFallbacks(androidExtension)

    return variants.map {
        extractAndroidKit(androidExtension, kotlinTarget, fallbacks, it)
    }
}

private fun Project.extractAndroidKit(
    androidExtension: DynamicBean,
    kotlinTarget: DynamicBean,
    fallbacks: AndroidFallbacks,
    variant: DynamicBean
): AndroidVariantOrigin {
    val variantName = variant.value<String>("name")
    val compilations = provider {
       extractCompilation(kotlinTarget, variantName)
    }

    // if `unitTestVariant` not specified for application/library variant (is null) then unit tests are disabled for it
    val unitTestVariantName = variant.beanOrNull("unitTestVariant")?.value<String>("name")
    val tests = tasks.withType<Test>().matching { test ->
        unitTestVariantName != null
                // use only Android unit tests (local tests)
                && test.hasSuperclass("AndroidUnitTest")
                // only tests of current application build variant
                && test.bean().value<String>("variantName") == unitTestVariantName
    }

    val buildTypeName = variant["buildType"].value<String>("name")
    val flavors = variant.beanCollection("productFlavors").map { flavor ->
        val flavorName = flavor.value<String>("name")
        val dimension = flavor.valueOrNull<String>("dimension")
            ?: throw KoverIllegalConfigException("Product flavor '$flavorName' must have at least one flavor dimension. Android Gradle Plugin with version < 3.0.0 not supported")
        AndroidFlavor(dimension, flavorName)
    }

    // merge flavors to get missing dimensions for variant
    val missingDimensions = findMissingDimensions(androidExtension, variant)

    val details = AndroidBuildVariant(variantName, buildTypeName, flavors, fallbacks, missingDimensions)
    return AndroidVariantOrigin(tests, compilations, details)
}

private fun findMissingDimensions(androidExtension: DynamicBean, variant: DynamicBean): Map<String, String> {
    val missingDimensionsForVariant = mutableMapOf<String, Any>()
    // default config has the lowest priority
    missingDimensionsForVariant +=
        androidExtension["defaultConfig"].value<Map<String, Any>>("missingDimensionStrategies")
    // take flavour in reverse order - first defined in the highest priority (taken last)
    variant.beanCollection("productFlavors").reversed().forEach { flavor ->
        missingDimensionsForVariant += flavor.value<Map<String, Any>>("missingDimensionStrategies")
    }

    return missingDimensionsForVariant.entries.associate { (dimension, request) ->
        dimension to request.bean().value("requested")
    }
}

private fun extractCompilation(
    kotlinTarget: DynamicBean,
    variantName: String
): Map<String, CompilationDetails> {
    val compilations = kotlinTarget.beanCollection("compilations").filter {
        it.value<String>("name") == variantName
    }

    return compilations.jvmCompilations {
        // exclude java classes from report. Expected java class files are placed in directories like
        //   build/intermediates/javac/debug/classes
        it.parentFile.parentFile.name == "javac"
    }
}


/// COPY FROM AGP

private fun findFallbacks(androidExtension: DynamicBean): AndroidFallbacks {
    val buildTypeFallbacks = androidExtension.beanCollection("buildTypes").associate {
        it.value<String>("name") to it.value<List<String>>("matchingFallbacks")
    }

    val flavors = androidExtension.beanCollection("productFlavors")

    // first loop through all the flavors and collect for each dimension, and each value, its
    // fallbacks
    // map of (dimension > (requested > fallbacks))
    val flavorAlternateMap: MutableMap<String, MutableMap<String, List<String>>> = mutableMapOf()
    for (flavor in flavors) {
        val matchingFallbacks = flavor.value<List<String>>("matchingFallbacks")

        if (matchingFallbacks.isNotEmpty()) {
            val name = flavor.value<String>("name")
            val dimension = flavor.value<String>("dimension")
            val dimensionMap = flavorAlternateMap.computeIfAbsent(dimension) { mutableMapOf() }
            dimensionMap[name] = matchingFallbacks.toList()
        }
        handleMissingDimensions(flavorAlternateMap, flavor)
    }
    // also handle missing dimensions on the default config.
    handleMissingDimensions(flavorAlternateMap, androidExtension["defaultConfig"])

    return AndroidFallbacks(buildTypeFallbacks, flavorAlternateMap)
}

private fun handleMissingDimensions(
    alternateMap: MutableMap<String, MutableMap<String, List<String>>>,
    flavor: DynamicBean
) {
    val missingStrategies = flavor.value<Map<String, Any>>("missingDimensionStrategies")
    if (missingStrategies.isNotEmpty()) {
        for ((dimension, dimensionRequest) in missingStrategies) {
            val requestBean = dimensionRequest.bean()
            val dimensionMap = alternateMap.computeIfAbsent(dimension) { mutableMapOf() }
            dimensionMap[requestBean.value("requested")] = requestBean.value("fallbacks")
        }
    }
}
