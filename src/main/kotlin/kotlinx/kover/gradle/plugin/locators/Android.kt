/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.AndroidCompilationKit
import kotlinx.kover.gradle.plugin.commons.AndroidFallbacks
import kotlinx.kover.gradle.plugin.commons.AndroidFlavor
import kotlinx.kover.gradle.plugin.commons.CompilationUnit
import kotlinx.kover.gradle.plugin.commons.KoverIllegalConfigException
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
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
    koverExtension: KoverProjectExtensionImpl,
    kotlinTarget: DynamicBean
): List<AndroidCompilationKit> {
    val variants = if ("applicationVariants" in androidExtension) {
        androidExtension.propertyBeans("applicationVariants")
    } else {
        androidExtension.propertyBeans("libraryVariants")
    }

    val fallbacks = findFallbacks(androidExtension)

    return variants.map {
        extractArtifact(androidExtension, koverExtension, kotlinTarget, fallbacks, it)
    }
}

private fun Project.extractArtifact(
    androidExtension: DynamicBean,
    koverExtension: KoverProjectExtensionImpl,
    kotlinTarget: DynamicBean,
    fallbacks: AndroidFallbacks,
    variant: DynamicBean
): AndroidCompilationKit {
    val variantName = variant.property<String>("name")
    val compilations = provider {
        mapOf("main" to extractCompilationOrEmpty(koverExtension, kotlinTarget, variantName))
    }

    val tests = tasks.withType<Test>().matching {
        // use only Android unit tests (local tests)
        it.hasSuperclass("AndroidUnitTest")
                // skip all tests from instrumentation if Kover Plugin is disabled for the project
                && !koverExtension.disabledForProject
                // skip this test if it disabled by name
                && it.name !in koverExtension.tests.tasksNames
                // only tests of current application build variant
                && it.bean().property<String>("variantName") == variant["unitTestVariant"].property<String>("name")
    }

    val buildTypeName = variant["buildType"].property<String>("name")
    val flavors = variant.propertyBeans("productFlavors").map { flavor ->
        val flavorName = flavor.property<String>("name")
        val dimension = flavor.propertyOrNull<String>("dimension")
            ?: throw KoverIllegalConfigException("Product flavor '$flavorName' must have at least one flavor dimension. Android Gradle Plugin with version < 3.0.0 not supported")
        AndroidFlavor(dimension, flavorName)
    }

    // merge flavors to get missing dimensions for variant
    val missingDimensions = findMissingDimensions(androidExtension, variant)

    return AndroidCompilationKit(variantName, buildTypeName, flavors, fallbacks, missingDimensions, tests, compilations)
}

private fun findMissingDimensions(androidExtension: DynamicBean, variant: DynamicBean): Map<String, String> {
    val missingDimensionsForVariant = mutableMapOf<String, Any>()
    // default config has the lowest priority
    missingDimensionsForVariant +=
        androidExtension["defaultConfig"].property<Map<String, Any>>("missingDimensionStrategies")
    // take flavour in reverse order - first defined in the highest priority (taken last)
    variant.propertyBeans("productFlavors").reversed().forEach { flavor ->
        missingDimensionsForVariant += flavor.property<Map<String, Any>>("missingDimensionStrategies")
    }

    return missingDimensionsForVariant.entries.associate { (dimension, request) ->
        dimension to request.bean().property<String>("requested")
    }
}

private fun extractCompilationOrEmpty(
    koverExtension: KoverProjectExtensionImpl,
    kotlinTarget: DynamicBean,
    variantName: String
): CompilationUnit {
    if (koverExtension.disabledForProject) {
        // If the Kover plugin is disabled, then it does not provide any directories and compilation tasks to its artifacts.
        return CompilationUnit()
    }

    val compilation = kotlinTarget.propertyBeans("compilations").first {
        it.property<String>("name") == variantName
    }

    return compilation.asJvmCompilationUnit(koverExtension.sources.excludeJavaCode) {
        // exclude java classes from report. Expected java class files are placed in directories like
        //   build/intermediates/javac/debug/classes
        it.parentFile.parentFile.name == "javac"
    }
}


/// COPY FROM AGP

private fun findFallbacks(androidExtension: DynamicBean): AndroidFallbacks {
    val buildTypeFallbacks = androidExtension.propertyBeans("buildTypes").associate {
        it.property<String>("name") to it.property<List<String>>("matchingFallbacks")
    }

    val flavors = androidExtension.propertyBeans("productFlavors")

    // first loop through all the flavors and collect for each dimension, and each value, its
    // fallbacks
    // map of (dimension > (requested > fallbacks))
    val flavorAlternateMap: MutableMap<String, MutableMap<String, List<String>>> = mutableMapOf()
    for (flavor in flavors) {
        val matchingFallbacks = flavor.property<List<String>>("matchingFallbacks")

        if (matchingFallbacks.isNotEmpty()) {
            val name = flavor.property<String>("name")
            val dimension = flavor.property<String>("dimension")
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
    val missingStrategies = flavor.property<Map<String, Any>>("missingDimensionStrategies")
    if (missingStrategies.isNotEmpty()) {
        for ((dimension, dimensionRequest) in missingStrategies) {
            val requestBean = dimensionRequest.bean()
            val dimensionMap = alternateMap.computeIfAbsent(dimension) { mutableMapOf() }
            dimensionMap[requestBean.property("requested")] = requestBean.property("fallbacks")
        }
    }
}
