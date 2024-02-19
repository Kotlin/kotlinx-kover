/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.commons

internal class AndroidBuildVariant(
    val buildVariant: String,
    val buildType: String,
    val flavors: List<AndroidFlavor>,

    val fallbacks: AndroidFallbacks,

    /**
     * The flavors used in case the dependency contains a dimension that is missing in the current project.
     * Specific only for this build variant.
     *
     * map of (dimension > flavor)
     */
    val missingDimensions: Map<String, String>
)

internal class AndroidFallbacks(
    /**
     *  Specifies a sorted list of fallback build types that the
     *  Kover can try to use when a dependency does not include a
     *  key build type. Kover selects the first build type that's
     *  available in the dependency
     *
     *  map of (buildtype > fallbacks)
     *  */
    val buildTypes: Map<String, List<String>>,

    /**
     * first loop through all the flavors and collect for each dimension, and each value, its
     * fallbacks.
     *
     * map of (dimension > (requested > fallbacks))
     */
    val flavors: Map<String, Map<String, List<String>>>,
)

/**
 * Flavor in Android Project.
 */
internal class AndroidFlavor(
    val dimension: String,
    val name: String,
)