/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

import kotlinx.kover.gradle.plugin.commons.KoverMigrations
import kotlinx.kover.gradle.plugin.dsl.KoverNames.REPORT_EXTENSION_NAME
import kotlinx.kover.gradle.plugin.dsl.KoverVersions.JACOCO_TOOL_DEFAULT_VERSION
import org.gradle.api.*

/**
 * Setting up how the coverage for the current project will be collected.
 *
 * Example of usage:
 * ```
 * kover {
 *      // disable measurement of the coverage for current project
 *      disable()
 *
 *      // skip classes defined in java source files
 *      excludeJavaCode()
 *
 *      excludeTests {
 *          // execution of the specified tests will not be taken into account in the coverage
 *      }
 *
 *      excludeInstrumentation {
 *          // bytecode of specified classes will not be modified when running tests - this solves some rare problems with instrumentation
 *      }
 * }
 * ```
 */
public interface KoverProjectExtension {
    /**
     * Disables instrumentation of all tests in the corresponding project, also excludes all sources of this project are excluded from the reports.
     *
     * When generating reports, if all projects are disabled, then no report will be generated.
     */
    public fun disable()

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
     * Coverage Tool by [JaCoCo](https://www.jacoco.org/jacoco/).
     */
    public fun useJacocoTool(version: String)

    /**
     * Excludes from report all classes, defined in Java source files.
     *
     * As a side effect, reports cease to depend on Java compilation task.
     */
    public fun excludeJavaCode()

    /**
     * Disables instrumentation of specified tests.
     *
     * This means that even if the excluded test is executed, the function calls that occurred in it will not be counted in the reports.
     *
     * As a side effect, Kover reports cease to depend on the specified test tasks.
     */
    public fun excludeTests(config: Action<KoverTestsExclusions>)

//    /**
//     * _Experimental_
//     *
//     * Exclude specified Kotlin compilations from report.
//     *
//     * The unit is typical for each type of project, for example, for Kotlin JVM it is Source Set, for Kotlin MPP it is target or Kotlin compilation.
//     *
//     * As a side effect, when any compilation is excluded, Kover reports cease to depend on the appropriate compilation task.
//     */
//     fun excludeCompilations(config: Action<KoverCompilationsExclusions>)

    /**
     * Exclude specified class from instrumentation.
     *
     * This means that even if these classes were actually called, their coverage will always be 0 in reports.
     *
     * This is necessary when there are errors in the instrumentation of classes from external dependencies, for example https://github.com/Kotlin/kotlinx-kover/issues/89
     */
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
        message = "Property was replaced with 'disable()' function. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("disable()"),
        level = DeprecationLevel.ERROR
    )
    public val isDisabled: Boolean
        get() = false

    @Deprecated(
        message = "Common filters were moved to '$REPORT_EXTENSION_NAME { filters { } }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
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
        message = "XML report setting was moved to '$REPORT_EXTENSION_NAME { xml { ... } }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public fun xmlReport(block: () -> Unit) {
    }

    @Deprecated(
        message = "HTML report setting was moved to '$REPORT_EXTENSION_NAME { html { ... } }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public fun htmlReport(block: () -> Unit) {
    }

    @Deprecated(
        message = "Verification report setting was moved to '$REPORT_EXTENSION_NAME { verify { ... } }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public fun verify(block: () -> Unit) {
    }
}

public interface KoverTestsExclusions {

    /**
     * Disables instrumentation of specified tests.
     *
     * This means that even if the excluded test is executed, the function calls that occurred in it will not be counted in the reports.
     *
     * As a side effect, reports cease to depend on the specified test tasks.
     */
    public fun tasks(vararg name: String)

    /**
     * Disables instrumentation of specified tests.
     *
     * This means that even if the excluded test is executed, the function calls that occurred in it will not be counted in the reports.
     *
     * As a side effect, reports cease to depend on the specified test tasks.
     */
    public fun tasks(names: Iterable<String>)

    /**
     * Disables instrumentation of test tasks owned by the specified MPP targets.
     *
     * This means that even if the excluded test is executed, the function calls that occurred in it will not be counted in the reports.
     *
     * As a side effect, reports cease to depend on the relevant test tasks.
     */
    public fun mppTargetName(vararg name: String)

    @Deprecated(
        message = "Use function `tasks(...)` instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("tasks"),
        level = DeprecationLevel.ERROR
    )
    public val excludeTasks: MutableList<String>
        get() = mutableListOf()
}


public interface KoverCompilationsExclusions {

    /**
     * Specify compilation unit exclusion for Kotlin JVM project.
     */
    public fun jvm(config: Action<KoverJvmSourceSet>)

    /**
     * Specify compilation unit exclusion for Kotlin MPP project.
     */
    public fun mpp(config: Action<KoverMppSourceSet>)
}

public interface KoverJvmSourceSet {
    /**
     * Exclude specified source sets from report.
     *
     * As a side effect, reports cease to depend on the task of compiling this source sets.
     */
    public fun sourceSetName(vararg name: String)

    /**
     * Exclude specified source sets from report.
     *
     * As a side effect, reports cease to depend on the task of compiling this source sets.
     */
    public fun sourceSetName(names: Iterable<String>)
}

public interface KoverMppSourceSet {
    /**
     * Exclude sources of specified targets from Kotlin MPP reports.
     *
     * As a side effect, reports cease to depend on the task of compiling specified targets.
     */
    public fun targetName(vararg name: String)

    /**
     * Exclude sources of specified Kotlin compilations from Kotlin MPP reports.
     *
     * As a side effect, reports cease to depend on the task of compiling specified compilations.
     */
    public fun compilation(targetName: String, compilationName: String)

    /**
     * Exclude sources of all Kotlin compilations with specified name from Kotlin MPP reports.
     *
     * As a side effect, reports cease to depend on the task of compiling specified compilations.
     */
    public fun compilation(compilationName: String)
}


public interface KoverInstrumentationExclusions {
    /**
     * Exclude specified classes from instrumentation.
     *
     * This means that even if these classes were actually called, their coverage will always be 0 in reports.
     *
     * This is necessary when there are errors in the instrumentation of classes from external dependencies, for example https://github.com/Kotlin/kotlinx-kover/issues/89
     */
    public fun classes(vararg names: String)

    /**
     * Exclude specified classes from instrumentation.
     *
     * This means that even if these classes were actually called, their coverage will always be 0 in reports.
     *
     * This is necessary when there are errors in the instrumentation of classes from external dependencies, for example https://github.com/Kotlin/kotlinx-kover/issues/89
     */
    public fun classes(names: Iterable<String>)

    /**
     * Exclude classes from specified packages and its subpackages from instrumentation.
     *
     * This means that even if these classes were actually called, their coverage will always be 0 in reports.
     *
     * This is necessary when there are errors in the instrumentation of classes from external dependencies, for example https://github.com/Kotlin/kotlinx-kover/issues/89
     */
    public fun packages(vararg names: String)

    /**
     * Exclude classes from specified packages and its subpackages from instrumentation.
     *
     * This means that even if these classes were actually called, their coverage will always be 0 in reports.
     *
     * This is necessary when there are errors in the instrumentation of classes from external dependencies, for example https://github.com/Kotlin/kotlinx-kover/issues/89
     */
    public fun packages(names: Iterable<String>)
}

