/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("RedundantVisibilityModifier")

package kotlinx.kover.api

import kotlinx.kover.api.KoverVersions.DEFAULT_INTELLIJ_VERSION
import kotlinx.kover.api.KoverVersions.DEFAULT_JACOCO_VERSION
import org.gradle.api.tasks.*

public interface CoverageEngineVariant {
    @get:Input
    public val vendor: CoverageEngineVendor
    @get:Input
    public val version: String
}

public enum class CoverageEngineVendor {
    INTELLIJ,
    JACOCO
}

public class IntellijEngine(override val version: String): CoverageEngineVariant {
    override val vendor: CoverageEngineVendor = CoverageEngineVendor.INTELLIJ
    override fun toString(): String = "IntelliJ Coverage Engine $version"
}
public object DefaultIntellijEngine: CoverageEngineVariant {
    override val vendor: CoverageEngineVendor = CoverageEngineVendor.INTELLIJ
    override val version: String = DEFAULT_INTELLIJ_VERSION
}

public class JacocoEngine(override val version: String): CoverageEngineVariant {
    override val vendor: CoverageEngineVendor = CoverageEngineVendor.JACOCO
    override fun toString(): String = "JaCoCo Coverage Engine $version"
}
public object DefaultJacocoEngine: CoverageEngineVariant {
    override val vendor: CoverageEngineVendor = CoverageEngineVendor.JACOCO
    override val version: String = DEFAULT_JACOCO_VERSION
}
