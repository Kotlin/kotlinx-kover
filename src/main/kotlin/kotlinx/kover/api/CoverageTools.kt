/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.api

import kotlinx.kover.api.KoverVersions.DEFAULT_KOVER_TOOL_VERSION
import kotlinx.kover.api.KoverVersions.DEFAULT_JACOCO_TOOL_VERSION
import kotlinx.kover.tools.commons.*
import org.gradle.api.tasks.*

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

/**
 * Coverage Tool by Kover.
 */
public class KoverTool(version: String): CoverageToolVariant(CoverageToolVendor.KOVER, version)

/**
 * Kover Coverage Tool with default version [DEFAULT_KOVER_TOOL_VERSION].
 */
public object DefaultKoverTool: CoverageToolVariant(CoverageToolVendor.KOVER, DEFAULT_KOVER_TOOL_VERSION)

/**
 * Coverage Tool by [JaCoCo](https://www.jacoco.org/jacoco/).
 */
public class JacocoTool(version: String): CoverageToolVariant(CoverageToolVendor.JACOCO, version)

/**
 * JaCoCo Coverage Tool with default version [DEFAULT_JACOCO_TOOL_VERSION].
 */
public object DefaultJacocoTool: CoverageToolVariant(CoverageToolVendor.JACOCO, DEFAULT_JACOCO_TOOL_VERSION)




// DEPRECATIONS
// TODO delete deprecations in 0.8.x
@Deprecated(
    message = "Class was renamed to [CoverageToolVariant]. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
    replaceWith = ReplaceWith("CoverageToolVariant"),
    level = DeprecationLevel.ERROR
)
public sealed class CoverageEngineVariant

@Deprecated(
    message = "Class was renamed to [KoverTool]. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
    replaceWith = ReplaceWith("KoverTool"),
    level = DeprecationLevel.ERROR
)
public class IntellijEngine(@Suppress("UNUSED_PARAMETER") version: String)

@Deprecated(
    message = "Class was renamed to [DefaultKoverTool]. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
    replaceWith = ReplaceWith("DefaultKoverTool"),
    level = DeprecationLevel.ERROR
)
public object DefaultIntellijEngine

@Deprecated(
    message = "Class was renamed to [JacocoTool]. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
    replaceWith = ReplaceWith("JacocoTool"),
    level = DeprecationLevel.ERROR
)
public class JacocoEngine(@Suppress("UNUSED_PARAMETER") version: String)

@Deprecated(
    message = "Class was renamed to [DefaultJacocoTool]. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
    replaceWith = ReplaceWith("DefaultJacocoTool"),
    level = DeprecationLevel.ERROR
)
public object DefaultJacocoEngine
