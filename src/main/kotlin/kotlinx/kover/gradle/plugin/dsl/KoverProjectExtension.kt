/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

import kotlinx.kover.gradle.plugin.commons.KoverMigrations
import org.gradle.api.*

public interface KoverProjectExtension {
    /**
     * Property is deprecated, please use `use...Tool...()` functions.
     */
    @Deprecated(
        message = "Property was removed. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public val engine: CoverageToolVariant
        get() = KoverToolDefault

    /**
     * Disable instrumentation of all tests of this project, also all kover tasks of the current projects are not
     * executed, even if whey called directly.
     */
    public var isDisabled: Boolean

    public fun useKoverToolDefault()

    public fun useJacocoToolDefault()

    public fun useKoverTool(version: String)

    public fun useJacocoTool(version: String)

    public fun excludeTests(config: Action<KoverTestsExclusions>)

    public fun excludeSources(config: Action<KoverSourcesExclusions>)

    public fun excludeInstrumentation(config: Action<KoverInstrumentationExclusions>)
}


public interface KoverTestsExclusions: KoverTaskDefinitions {
    public override fun taskName(vararg name: String)

    public override fun taskName(names: Iterable<String>)

    public fun kmpTargetName(vararg name: String)
}


public interface KoverSourcesExclusions: KoverKmpCompilationDefinitions {
    public var excludeJavaCode: Boolean

    public fun jvmSourceSetName(vararg name: String)

    public fun jvmSourceSetName(names: Iterable<String>)

    public override fun kmpTargetName(vararg name: String)

    public override fun kmpCompilation(targetName: String, compilationName: String)

    public override fun kmpCompilation(compilationName: String)
}

public interface KoverInstrumentationExclusions: KoverClassDefinitions {
    public override fun className(vararg name: String)

    public override fun className(names: Iterable<String>)

    public override fun packageName(vararg name: String)

    public override fun packageName(names: Iterable<String>)
}

