/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.dsl

import kotlinx.kover.features.jvm.KoverFeatures

/**
 * Stable reference point for various versions that Kover leverages.
 */
public object KoverVersions {
    /**
     * Minimal supported Gradle version.
     */
    public const val MINIMUM_GRADLE_VERSION = "6.8"

    /**
     * JaCoCo coverage tool version used by default.
     */
    public const val JACOCO_TOOL_DEFAULT_VERSION = "0.8.14"

    /**
     * JaCoCo coverage tool minimal supported version.
     */
    public const val JACOCO_TOOL_MINIMAL_VERSION = "0.8.7"

    /**
     * Current version of Kover Gradle Plugin
     */
    public val version: String
        get() = KoverFeatures.version
}
