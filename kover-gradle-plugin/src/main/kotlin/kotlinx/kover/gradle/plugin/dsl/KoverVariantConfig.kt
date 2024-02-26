/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

import kotlinx.kover.gradle.plugin.commons.KoverIllegalConfigException
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty


/**
 * Type for customizing report variants shared by the current project.
 *
 * A report variant is a set of information used to generate a reports, namely:
 * project classes, a list of Gradle test tasks, classes that need to be excluded from instrumentation.
 *
 * ```
 *  currentProject {
 *      // create report variant with custom name,
 *      // in which it is acceptable to add information from other variants of the current project, as well as `kover` dependencies
 *      createVariant("custom") {
 *          // ...
 *      }
 *
 *      // Configure the variant that is automatically created in the current project
 *      // For example, "jvm" for JVM target or "debug" for Android build variant
 *      providedVariant("jvm") {
 *          // ...
 *      }
 *
 *      // Configure the variant for all the code that is available in the current project.
 *      // This variant always exists for any type of project.
 *      totalVariant {
 *          // ...
 *      }
 *  }
 * ```
 */
public interface KoverCurrentProjectVariantsConfig: KoverVariantConfig {
    /**
     * Create custom report variant with name [variantName].
     * In it is acceptable to add information from other variants of the current project, as well as `kover` dependencies.
     */
    public fun createVariant(variantName: String, block: Action<KoverVariantCreateConfig>)

    /**
     * Configure the variant with name [variantName] that is automatically created in the current project.
     * For example, `"jvm"` for JVM target or `"debug"` for Android build variant.
     */
    public fun providedVariant(variantName: String, block: Action<KoverVariantConfig>)

    /**
     * Configure the variant for all the code that is available in the current project.
     * This variant always exists for any type of project.
     */
    public fun totalVariant(block: Action<KoverVariantConfig>)

    /**
     * Instrumentation settings for the current Gradle project.
     *
     * Instrumentation is the modification of classes when they are loaded into the JVM, which helps to determine which code was called and which was not.
     * Instrumentation changes the bytecode of the class, so it may disable some JVM optimizations, slow down performance and concurrency tests, and may also be incompatible with other instrumentation libraries.
     *
     * For this reason, it may be necessary to fine-tune the instrumentation, for example, disabling instrumentation for problematic classes. Note that such classes would be marked as uncovered because of that.
     *
     * Example:
     * ```
     *  instrumentation {
     *      // disable instrumentation of test tasks of all classes
     *      disabledForAll = true
     *
     *      // disable instrumentation of test task `test2`
     *      disabledForTasks.add("test2")
     *
     *      // disable instrumentation of specified classes in test tasks
     *      excludedClasses.addAll("foo.bar.*Biz", "*\$Generated")
     *  }
     * ```
     */
    public fun instrumentation(block: Action<KoverProjectInstrumentation>)
}

/**
 * Common config for Kover report variants.
 */
public interface KoverVariantConfig {
    /**
     * Limit the classes that will be included in the reports.
     * These settings do not affect the instrumentation of classes.
     *
     * The settings specified here affect all reports in any projects that use the current project depending on.
     * However, these settings should be used to regulate classes specific only to the project in which this setting is specified.
     *
     * Example:
     * ```
     *  sources {
     *     // exclude classes compiled by Java compiler from all reports
     *     excludeJava = true
     *
     *     // exclude source classes of specified source sets from all reports
     *     excludedSourceSets.addAll(excludedSourceSet)
     * ```
     */
    public fun sources(block: Action<KoverVariantSources>)

    /**
     * Set up tests, the run of which is used to measure coverage.
     *
     * To measure coverage, Kover runs Gradle test tasks, instrumentation takes place before they are performed, and code coverage is measured during execution.
     *
     * By default, Kover use all [org.gradle.api.tasks.testing.Test] to measure coverage.
     *
     * Example:
     * ```
     *  testTasks {
     *      // The coverage of the test1 and test2 tasks will no longer be taken into account in the reports
     *      //  as well as these tasks will not be called when generating the report
     *      excluded.addAll("test1", "test2")
     *  }
     * ```
     */
    public fun testTasks(block: Action<KoverVariantTestTasks>)
}

/**
 * Limit the classes that will be included in the reports.
 * These settings do not affect the instrumentation of classes.
 *
 * The settings specified here affect all reports in any projects that use the current project depending on.
 * However, these settings should be used to regulate classes specific only to the project in which this setting is specified.
 *
 * Example:
 * ```
 *  sources {
 *     // exclude classes compiled by Java compiler from all reports
 *     excludeJava = true
 *
 *     // exclude source classes of specified source sets from all reports
 *     excludedSourceSets.addAll(excludedSourceSet)
 * ```
 */
public interface KoverVariantSources {
    /**
     * Exclude classes compiled by Java compiler from all reports
     */
    public val excludeJava: Property<Boolean>

    /**
     * Exclude source classes of specified source sets from all reports
     */
    public val excludedSourceSets: SetProperty<String>
}

/**
 * Instrumentation settings for the current Gradle project.
 *
 * Instrumentation is the modification of classes when they are loaded into the JVM, which helps to determine which code was called and which was not.
 * Instrumentation changes the bytecode of the class, so it may disable some JVM optimizations, slow down performance and concurrency tests, and may also be incompatible with other instrumentation libraries.
 *
 * For this reason, it may be necessary to fine-tune the instrumentation, for example, disabling instrumentation for problematic classes.
 *
 * Example:
 * ```
 *  instrumentation {
 *      // disable instrumentation of test tasks of all classes
 *      disabledForAll = true
 *
 *      // disable instrumentation of test task `test2`
 *      disabledForTasks.add("test2")
 *
 *      // disable instrumentation of specified classes in test tasks
 *      excludedClasses.addAll("foo.bar.*Biz", "*\$Generated")
 *  }
 * ```
 */
public interface KoverProjectInstrumentation {
    /**
     * Disable instrumentation in all classes test tasks
     */
    public val disabledForAll: Property<Boolean>

    /**
     * Disable instrumentation in specified tasks
     */
    public val disabledForTasks: SetProperty<String>

    /**
     * Disable instrumentation in test tasks of specified classes
     */
    public val excludedClasses: SetProperty<String>
}

/**
 * Set up tests, the run of which is used to measure coverage.
 *
 * To measure coverage, Kover runs Gradle test tasks, instrumentation takes place before they are performed, and code coverage is measured during execution.
 *
 * By default, Kover use all [org.gradle.api.tasks.testing.Test] to measure coverage.
 *
 * Example:
 * ```
 *  testTasks {
 *      // The coverage of the test1 and test2 tasks will no longer be taken into account in the reports
 *      //  as well as these tasks will not be called when generating the report
 *      excluded.addAll("test1", "test2")
 *  }
 * ```
 */
public interface KoverVariantTestTasks {
    /**
     * Specifies not to use test task with passed names to measure coverage.
     * These tasks will also not be called when generating Kover reports.
     */
    public val excluded: SetProperty<String>
}

/**
 * The type for creating a custom report variant.
 *
 * Example:
 * ```
 *  // Add to created variant classes, tests and instrumented classes from "jvm" report variant of current project
 *  add("jvm")
 *
 *  // add an "nonexistent" option that may not exist in the current project
 *  add("nonexistent", true)
 *
 *  // Add to created variant classes, tests and instrumented classes from "jvm" report variant of current project, as well as `kover(project("name"))` dependencies
 *  addWithDependencies("custom")
 * ```
 */
public interface KoverVariantCreateConfig: KoverVariantConfig {
    /**
     * Add to created variant classes, tests and instrumented classes from report variant with name [variantNames].
     * This variant is taken only from the current project.
     *
     * If [optional] is `false` and a variant with given name is not found in the current project, an error [KoverIllegalConfigException] is thrown.
     */
    public fun add(vararg variantNames: String, optional: Boolean = false)

    /**
     * Add to created variant classes, tests and instrumented classes from report variant with name [variantNames].
     * This variant is taken from the current project and all `kover(project("name"))` dependency projects.
     *
     * If [optional] is `false` and a variant with given name is not found in the current project, an error [KoverIllegalConfigException] is thrown.
     *
     * If [optional] is `true` and a variant with given name is not found in the current project - in this case, the variant will not be searched even in dependencies.
     */
    public fun addWithDependencies(vararg variantNames: String, optional: Boolean = false)
}