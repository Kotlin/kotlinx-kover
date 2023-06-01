/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.dsl

/**
 * Stable reference point for various versions that Kover leverages.
 */
public object KoverVersions {
    /**
     * Minimal supported Gradle version.
     */
    public const val MINIMUM_GRADLE_VERSION = "6.8"

    /**
     * Kover coverage tool version.
     */
    public const val KOVER_TOOL_VERSION = "1.0.721"

    /**
     * JaCoCo coverage tool version used by default.
     */
    public const val JACOCO_TOOL_DEFAULT_VERSION = "0.8.10"
}
