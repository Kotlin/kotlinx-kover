/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

import kotlinx.kover.gradle.plugin.commons.KoverMigrations
import kotlinx.kover.gradle.plugin.dsl.KoverVersions.JACOCO_TOOL_DEFAULT_VERSION
import kotlinx.kover.gradle.plugin.dsl.KoverVersions.KOVER_TOOL_DEFAULT_VERSION
import org.gradle.api.*

public interface KoverProjectExtension {

    /**
     * Disables instrumentation of all tests in the corresponding project, as well as execution
     * of all kover tasks of the current projects, including the direct calls to t
     */
    public var disabledForProject: Boolean

    /**
     * Configures plugin to use Kover coverage tool.
     * This option is enabled by default, unless [JaCoCo][useJacocoTool] is enabled.
     */
    public fun useKoverTool()

    /**
     * JaCoCo Coverage Tool with default version [JACOCO_TOOL_DEFAULT_VERSION].
     */
    public fun useJacocoTool()

    /**
     * Kover Coverage Tool with default version [KOVER_TOOL_DEFAULT_VERSION].
     */
    public fun useKoverTool(version: String)

    /**
     * Coverage Tool by [JaCoCo](https://www.jacoco.org/jacoco/).
     */
    public fun useJacocoTool(version: String)

    public fun excludeTests(config: Action<KoverTestsExclusions>)

    public fun excludeSources(config: Action<KoverSourcesExclusions>)

    public fun excludeInstrumentation(config: Action<KoverInstrumentationExclusions>)


    /*
     * Deprecations
     * TODO remove in 0.8.0
     */

    /**
     * Property is deprecated, please use `use...Tool()` functions.
     */
    @Deprecated(
        message = "Property was removed. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public var engine: Nothing?
        get() = null
        set(@Suppress("UNUSED_PARAMETER") value) {}

    @Deprecated(
        message = "Property was renamed to 'disabledForProject'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("disabledForProject"),
        level = DeprecationLevel.ERROR
    )
    public val isDisabled: Boolean
        get() = false

    @Deprecated(
        message = "Common filters was moved to '$REGULAR_REPORT_EXTENSION_NAME { filters { } }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public fun filters(block: () -> Unit) {
    }

    @Deprecated(
        message = "Tasks filters was renamed to 'excludeTests'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("excludeTests"),
        level = DeprecationLevel.ERROR
    )
    public fun instrumentation(block: KoverTestsExclusions.() -> Unit) {
    }

    @Deprecated(
        message = "XML report setting was moved to '$REGULAR_REPORT_EXTENSION_NAME { xml { ... } }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public fun xmlReport(block: () -> Unit) {}

    @Deprecated(
        message = "HTML report setting was moved to '$REGULAR_REPORT_EXTENSION_NAME { html { ... } }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public fun htmlReport(block: () -> Unit) {}

    @Deprecated(
        message = "Verification report setting was moved to '$REGULAR_REPORT_EXTENSION_NAME { verify { ... } }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public fun verify(block: () -> Unit) {}
}


public interface KoverTestsExclusions : KoverTaskDefinitions {

    public override fun tasks(vararg name: String)

    public override fun tasks(names: Iterable<String>)

    public fun mppTargetName(vararg name: String)

    @Deprecated(
        message = "Use function `tasks(...)` instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("tasks"),
        level = DeprecationLevel.ERROR
    )
    public val excludeTasks: MutableList<String>
        get() = mutableListOf()
}


public interface KoverSourcesExclusions {
    public var excludeJavaCode: Boolean

    public fun jvm(config: Action<KoverJvmSourceSet>)

    public fun mpp(config: Action<KoverMppSourceSet>)
}

public interface KoverJvmSourceSet {
    public fun sourceSetName(vararg name: String)

    public fun sourceSetName(names: Iterable<String>)
}

public interface KoverMppSourceSet {
    public fun targetName(vararg name: String)

    public fun compilation(targetName: String, compilationName: String)

    public fun compilation(compilationName: String)
}


public interface KoverInstrumentationExclusions : KoverClassDefinitions {
    public override fun classes(vararg names: String)

    public override fun classes(names: Iterable<String>)

    public override fun packages(vararg names: String)

    public override fun packages(names: Iterable<String>)
}

