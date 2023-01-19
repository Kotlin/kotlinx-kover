/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

import kotlinx.kover.gradle.plugin.commons.CoverageToolVendor
import kotlinx.kover.gradle.plugin.dsl.KoverVersions.JACOCO_TOOL_DEFAULT_VERSION
import kotlinx.kover.gradle.plugin.dsl.KoverVersions.KOVER_TOOL_DEFAULT_VERSION
import org.gradle.api.tasks.*


/**
 * Coverage Tool by Kover.
 */
public fun koverTool(version: String): CoverageToolVariant = KoverTool(version)

/**
 * Kover Coverage Tool with default version [KOVER_TOOL_DEFAULT_VERSION].
 */
public fun koverToolDefault(): CoverageToolVariant = KoverToolDefault


/**
 * Coverage Tool by [JaCoCo](https://www.jacoco.org/jacoco/).
 */
public fun jacocoTool(version: String): CoverageToolVariant = JacocoTool(version)

/**
 * JaCoCo Coverage Tool with default version [JACOCO_TOOL_DEFAULT_VERSION].
 */
public fun jacocoToolDefault(): CoverageToolVariant = JacocoToolDefault

public sealed class CoverageToolVariant(
    @get:Input
    internal val vendor: CoverageToolVendor,
    @get:Input
    internal val version: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CoverageToolVariant

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
        return "$vendor Coverage Tool $version"
    }
}

internal class KoverTool(version: String): CoverageToolVariant(CoverageToolVendor.KOVER, version)
internal object KoverToolDefault: CoverageToolVariant(CoverageToolVendor.KOVER, KOVER_TOOL_DEFAULT_VERSION)
internal class JacocoTool(version: String): CoverageToolVariant(CoverageToolVendor.JACOCO, version)
internal object JacocoToolDefault: CoverageToolVariant(CoverageToolVendor.JACOCO, JACOCO_TOOL_DEFAULT_VERSION)
