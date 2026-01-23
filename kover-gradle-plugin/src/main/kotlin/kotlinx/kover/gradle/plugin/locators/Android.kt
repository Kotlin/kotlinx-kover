/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.appliers.origin.AndroidVariantOrigin
import kotlinx.kover.gradle.plugin.appliers.origin.CompilationDetails
import kotlinx.kover.gradle.plugin.appliers.origin.LanguageCompilation
import kotlinx.kover.gradle.plugin.commons.AndroidBuildVariant
import kotlinx.kover.gradle.plugin.commons.AndroidFallbacks
import kotlinx.kover.gradle.plugin.commons.AndroidFlavor
import kotlinx.kover.gradle.plugin.commons.KoverIllegalConfigException
import kotlinx.kover.gradle.plugin.util.DynamicBean
import kotlinx.kover.gradle.plugin.util.bean
import kotlinx.kover.gradle.plugin.util.hasSuperclass
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType
import java.io.File

/**
 * Locate Android compilation kits for the given Kotlin Target.
 *
 * Works for the AGP version < 9.0.0
 */
internal fun Project.androidCompilationKitsBefore9(
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
        extractAndroidKitBefore9(androidExtension, kotlinTarget, fallbacks, it)
    }
}

private fun Project.extractAndroidKitBefore9(
    androidExtension: DynamicBean,
    kotlinTarget: DynamicBean,
    fallbacks: AndroidFallbacks,
    variant: DynamicBean
): AndroidVariantOrigin {
    val variantName = variant.value<String>("name")
    val compilations = provider {
        extractCompilationBefore9(kotlinTarget, variantName)
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
    val missingDimensions = findMissingDimensionsBefore9(androidExtension, variant)

    val details = AndroidBuildVariant(variantName, buildTypeName, flavors, fallbacks, missingDimensions)
    return AndroidVariantOrigin(tests, compilations, details)
}

private fun findMissingDimensionsBefore9(androidExtension: DynamicBean, variant: DynamicBean): Map<String, String> {
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


/**
 * Locate Android compilation kits for the given Kotlin Target.
 */
internal fun Project.androidCompilationKits(
    androidExtension: DynamicBean,
    variants: List<AndroidVariantInfo>,
    kotlinTarget: DynamicBean,
): List<AndroidVariantOrigin> {
    val fallbacks = findFallbacks(androidExtension)
    // get missing dimensions from the default config
    val missingDimensions = findMissingDimensions(androidExtension)

    return variants.map {
        extractAndroidKit(missingDimensions, kotlinTarget, fallbacks, it)
    }
}

private fun Project.extractAndroidKit(
    missingDimensions: Map<String, String>,
    kotlinTarget: DynamicBean,
    fallbacks: AndroidFallbacks,
    variant: AndroidVariantInfo
): AndroidVariantOrigin {
    val compilations = provider {
        extractCompilation(kotlinTarget, variant)
    }

    val tests = tasks.withType<Test>().matching { test ->
        // use only Android unit tests (local tests)
        test.hasSuperclass("AndroidUnitTest")
                // An assumption: Android unit test always called <variantName>UnitTest
                && test.bean().value<String>("variantName") == "${variant.name}UnitTest"
    }

    val details =
        AndroidBuildVariant(variant.name, variant.buildTypeName, variant.flavors, fallbacks, missingDimensions)
    return AndroidVariantOrigin(tests, compilations, details)
}


internal fun DynamicBean.convertVariant(): AndroidVariantInfo {
    val variantName = value<String>("name")

    val buildType = value<Any>("buildType")
    val buildTypeName = buildType as? String ?: buildType.bean().value("name")

    val productFlavors = valueCollection<Pair<String, String>>("productFlavors").map { flavor ->
        // second flavor name
        val flavorName = flavor.second
        // first dimension name
        val dimension = flavor.first

        AndroidFlavor(flavorName, dimension)
    }

    // since AGP 9.0.0 doesn't fill allKotlinSourceSets.srcDirs we get source directories from Android build variant
    // it's ok to take sources not from a compilation but from build variant because each build variant corresponds to only one Kotlin compilation
    val sourceDirs = bean("sources").beanOrNull("kotlin")?.value<Provider<Collection<Directory>>>("all")?.get()?.map { it.asFile }?.toSet() ?: emptySet()

    return AndroidVariantInfo(variantName, buildTypeName, productFlavors, sourceDirs)
}

internal data class AndroidVariantInfo(
    val name: String,
    val buildTypeName: String,
    val flavors: List<AndroidFlavor>,
    val sourceDirs: Set<File>
)

private fun findMissingDimensions(androidExtension: DynamicBean): Map<String, String> {
    val missingDimensionsForVariant = mutableMapOf<String, String>()
    missingDimensionsForVariant +=
        androidExtension["defaultConfig"].value<Map<String, Any>>("missingDimensionStrategies")
            .mapValues { (_, request) ->
                request.bean().value("requested")
            }
    // no missingDimensionStrategies on product flavors in Variant API since 9.0.0

    return missingDimensionsForVariant
}

private fun extractCompilation(
    kotlinTarget: DynamicBean,
    variant: AndroidVariantInfo
): Map<String, CompilationDetails> {
    val compilations = kotlinTarget.beanCollection("compilations").filter {
        it.value<String>("name") == variant.name
    }

    // since AGP 9.0.0 doesn't fill allKotlinSourceSets.srcDirs and output.classesDirs in Kotlin compilations we use different logic
    return compilations.associate { compilation ->
        val name = compilation.value<String>("name")
        name to extractAndroidCompilation(compilation, variant)
    }
}

private fun extractCompilationBefore9(
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


private fun extractAndroidCompilation(
    compilation: DynamicBean,
    variant: AndroidVariantInfo
): CompilationDetails {
    val kotlinCompileTask = compilation.value<TaskProvider<Task>>("compileTaskProvider").get()
    val javaCompileTask = compilation.value<TaskProvider<Task>>("compileJavaTaskProvider").get()

    // assumption: compilers place class-files in directories named 'classes'
    val kotlinOutputs = kotlinCompileTask.outputs.files.filter { file -> file.name == "classes" }
    val javaOutputs = javaCompileTask.outputs.files.filter { file -> file.name == "classes" }

    val kotlin = LanguageCompilation(kotlinOutputs, kotlinCompileTask)
    val java = LanguageCompilation(javaOutputs, javaCompileTask)

    return CompilationDetails(variant.sourceDirs, kotlin, java)
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
