/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.AndroidFallbacks
import kotlinx.kover.gradle.plugin.commons.AndroidFlavor
import kotlinx.kover.gradle.plugin.commons.AndroidVariantCompilationKit
import kotlinx.kover.gradle.plugin.commons.CompilationUnit
import kotlinx.kover.gradle.plugin.commons.KoverCriticalException
import kotlinx.kover.gradle.plugin.commons.KoverIllegalConfigException
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import kotlinx.kover.gradle.plugin.util.DynamicBean
import kotlinx.kover.gradle.plugin.util.bean
import kotlinx.kover.gradle.plugin.util.hasSuperclass
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

internal fun Project.afterAndroidPluginApplied(afterAndroid: () -> Unit) {
    val androidComponents = project.extensions.findByName("androidComponents")?.bean()
        ?: throw KoverCriticalException("Kover requires extension with name 'androidComponents' for project '${project.path}' since it is recognized as Kotlin+Android project")

    val callback = Action<Any> {
        project.afterEvaluate {
            afterAndroid()
        }
    }

    if (androidComponents.hasFunction("finalizeDsl", callback)) {
        /*
        Assumption: `finalizeDsl` is called in the `afterEvaluate` action, in which build variants are created.
        Therefore,  if an action is added to the queue inside it, it will be executed only after variants are created
         */
        androidComponents.call("finalizeDsl", callback)
    } else {
        // for old versions < 7.0 an action is added to the AAA queue.
        // Since this code is executed after the applying of AGP, there is a high probability that the action will fall into the `afterEvaluate` queue after the actions of the AGP
        project.afterEvaluate {
            afterAndroid()
        }
    }
}

/**
 * Locate Android compilation kits for the given Kotlin Target.
 */
internal fun Project.androidCompilationKits(
    androidExtension: DynamicBean,
    koverExtension: KoverProjectExtensionImpl,
    kotlinTarget: DynamicBean
): List<AndroidVariantCompilationKit> {
    val variants = if ("applicationVariants" in androidExtension) {
        androidExtension.getCollection("applicationVariants")
    } else {
        androidExtension.getCollection("libraryVariants")
    }

    val fallbacks = findFallbacks(androidExtension)

    return variants.map {
        extractAndroidKit(androidExtension, koverExtension, kotlinTarget, fallbacks, it)
    }
}

private fun Project.extractAndroidKit(
    androidExtension: DynamicBean,
    koverExtension: KoverProjectExtensionImpl,
    kotlinTarget: DynamicBean,
    fallbacks: AndroidFallbacks,
    variant: DynamicBean
): AndroidVariantCompilationKit {
    val variantName = variant.value<String>("name")
    val compilations = provider {
        mapOf("main" to extractCompilationOrEmpty(koverExtension, kotlinTarget, variantName))
    }

    val unitTestVariantName = variant.find("unitTestVariant")?.value<String>("name")
    val tests = tasks.withType<Test>().matching { test ->
        // if `unitTestVariant` not specified for application/library variant then unit tests are disabled for it
        unitTestVariantName != null
                // skip all tests from instrumentation if Kover Plugin is disabled for the project
                && !koverExtension.disabled
                // skip this test if it disabled by name
                && test.name !in koverExtension.tests.tasksNames
                // use only Android unit tests (local tests)
                && test.hasSuperclass("AndroidUnitTest")
                // only tests of current application build variant
                && test.bean().value<String>("variantName") == unitTestVariantName
    }

    val buildTypeName = variant["buildType"].value<String>("name")
    val flavors = variant.getCollection("productFlavors").map { flavor ->
        val flavorName = flavor.value<String>("name")
        val dimension = flavor.valueOrNull<String>("dimension")
            ?: throw KoverIllegalConfigException("Product flavor '$flavorName' must have at least one flavor dimension. Android Gradle Plugin with version < 3.0.0 not supported")
        AndroidFlavor(dimension, flavorName)
    }

    // merge flavors to get missing dimensions for variant
    val missingDimensions = findMissingDimensions(androidExtension, variant)

    return AndroidVariantCompilationKit(variantName, buildTypeName, flavors, fallbacks, missingDimensions, tests, compilations)
}

private fun findMissingDimensions(androidExtension: DynamicBean, variant: DynamicBean): Map<String, String> {
    val missingDimensionsForVariant = mutableMapOf<String, Any>()
    // default config has the lowest priority
    missingDimensionsForVariant +=
        androidExtension["defaultConfig"].value<Map<String, Any>>("missingDimensionStrategies")
    // take flavour in reverse order - first defined in the highest priority (taken last)
    variant.getCollection("productFlavors").reversed().forEach { flavor ->
        missingDimensionsForVariant += flavor.value<Map<String, Any>>("missingDimensionStrategies")
    }

    return missingDimensionsForVariant.entries.associate { (dimension, request) ->
        dimension to request.bean().value("requested")
    }
}

private fun extractCompilationOrEmpty(
    koverExtension: KoverProjectExtensionImpl,
    kotlinTarget: DynamicBean,
    variantName: String
): CompilationUnit {
    if (koverExtension.disabled) {
        // If the Kover plugin is disabled, then it does not provide any directories and compilation tasks to its artifacts.
        return CompilationUnit()
    }

    val compilation = kotlinTarget.getCollection("compilations").first {
        it.value<String>("name") == variantName
    }

    return compilation.asJvmCompilationUnit(koverExtension.excludeJava) {
        // exclude java classes from report. Expected java class files are placed in directories like
        //   build/intermediates/javac/debug/classes
        it.parentFile.parentFile.name == "javac"
    }
}


/// COPY FROM AGP

private fun findFallbacks(androidExtension: DynamicBean): AndroidFallbacks {
    val buildTypeFallbacks = androidExtension.getCollection("buildTypes").associate {
        it.value<String>("name") to it.value<List<String>>("matchingFallbacks")
    }

    val flavors = androidExtension.getCollection("productFlavors")

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
