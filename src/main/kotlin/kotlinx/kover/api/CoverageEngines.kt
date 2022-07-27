/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.api

import kotlinx.kover.api.KoverVersions.DEFAULT_INTELLIJ_VERSION
import kotlinx.kover.api.KoverVersions.DEFAULT_JACOCO_VERSION
import org.gradle.api.tasks.*

public sealed class CoverageEngineVariant(
    @get:Input
    internal val vendor: CoverageEngineVendor,
    @get:Input
    internal val version: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoverageEngineVariant

        if (vendor != other.vendor) return false
        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vendor.hashCode()
        result = 31 * result + version.hashCode()
        return result
    }

    override fun toString(): String {
        return "$vendor Coverage Engine $version"
    }
}

internal enum class CoverageEngineVendor {
    INTELLIJ,
    JACOCO
}

// TODO make internal in 0.7 version
@Deprecated(
    message = "Class was removed in Kover API version 2. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}",
    level = DeprecationLevel.WARNING
)
public enum class CoverageEngine {
    INTELLIJ,
    JACOCO
}

/**
 * Coverage Engine by IntelliJ.
 */
public class IntellijEngine(version: String): CoverageEngineVariant(CoverageEngineVendor.INTELLIJ, version)

/**
 * IntelliJ Coverage Engine with default version [DEFAULT_INTELLIJ_VERSION].
 */
public object DefaultIntellijEngine: CoverageEngineVariant(CoverageEngineVendor.INTELLIJ, DEFAULT_INTELLIJ_VERSION)

/**
 * Coverage Engine by [JaCoCo](https://www.jacoco.org/jacoco/).
 */
public class JacocoEngine(version: String): CoverageEngineVariant(CoverageEngineVendor.JACOCO, version)

/**
 * JaCoCo Coverage Engine with default version [DEFAULT_JACOCO_VERSION].
 */
public object DefaultJacocoEngine: CoverageEngineVariant(CoverageEngineVendor.JACOCO, DEFAULT_JACOCO_VERSION)
