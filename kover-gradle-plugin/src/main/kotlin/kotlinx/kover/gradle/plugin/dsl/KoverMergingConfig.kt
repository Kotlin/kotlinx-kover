/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.specs.Spec

/**
 * Configuring a merged report.
 *
 * **Attention! Usage of this block breaks project isolation and is incompatible with the configuration cache!**
 * If you need configuration cache support, please explicitly configure Kover plugin in each project using [variants] blocks.
 *
 * Used as a shortcut for group configuration of the plugin in several projects and merging reports.
 * If you specify this block without additional commands
 * ```
 *  kover {
 *      merge {
 *      }
 *  }
 * ```
 * it will be equivalent to this code
 * ```
 *  val thisProject = project
 *  subprojects {
 *      apply("org.jetbrains.kotlinx.kover")
 *      thisProject.dependencies.add("kover", this)
 *      extensions.getByType(KoverExtension::class.java).useJacoco.set(thisProject.extensions.getByType(KoverExtension::class.java).useJacoco)
 *      extensions.getByType(KoverExtension::class.java).jacocoVersion.set(thisProject.extensions.getByType(KoverExtension::class.java).jacocoVersion)
 *  }
 * ```
 * As a result, a merged report will be created in the project in which this `merge` block was called (merging project).
 *
 * It is acceptable to limit the projects in which the configuration will take place by adding filters:
 * ```
 *  kover {
 *      merge {
 *          subprojects {
 *              it.name != "uncovered"
 *          }
 *      }
 *  }
 * ```
 * This way Kover plugin will not be applied in a project named `uncovered`.
 *
 * If you specify several filters, Kover plugin will be applied in the project if at least one of these filters will return `true`.
 *
 *
 * Full list of functions:
 * ```
 *  kover {
 *      merge {
 *          // include all subprojects
 *          subprojects()
 *
 *          // include subprojects that have passed the filter
 *          subprojects {
 *              // filter predicate
 *          }
 *
 *          // include all projects of the build
 *          allProjects()
 *
 *          // include all projects of the build that have passed the filter
 *          allProjects {
 *              // filter predicate
 *          }
 *
 *          // include projects by name or path
 *          projects("project-name", ":")
 *
 *          sources {
 *              // set up sources for all variants of all included projects
 *          }
 *
 *          instrumentation {
 *              // set up instrumentation for all variants of all included projects
 *          }
 *
 *          createVariant("variantName") {
 *              // create custom variant
 *          }
 *      }
 *  }
 * ```
 */
@KoverGradlePluginDsl
public interface KoverMergingConfig {

    /**
     * Include to the merged report all subprojects of the current project.
     *
     * Kover plugin will be automatically applied in all subprojects.
     */
    public fun subprojects()

    /**
     * Include to the merged report subprojects of the current project that have passed the filter.
     *
     * Kover plugin will be automatically applied in passed subprojects.
     *
     * **Important!**
     *
     * It is impossible to guarantee exactly at what point in time the filter will be executed, before evaluation the corresponding project or during the evaluation.
     * Therefore, only static values can be read in filters, for example, the name of the project or its path.
     */
    public fun subprojects(filter: Spec<Project>)

    /**
     * Include to the merged report all projects of the build.
     *
     * Kover plugin will be automatically applied in all projects.
     */
    public fun allProjects()

    /**
     * Include to the merged report all projects of the build that have passed the filter.
     *
     * Kover plugin will be automatically applied in passed subprojects.
     *
     * **Important!**
     *
     * It is impossible to guarantee exactly at what point in time the filter will be executed, before evaluation the corresponding project or during the evaluation.
     * Therefore, only static values can be read in filters, for example, the name of the project or its path.
     */
    public fun allProjects(filter: Spec<Project>)


    /**
     * Include to the merged report all specified projects.
     * You can specify both the project name and its path (starts with the `:` sign).
     *
     * Kover plugin will be automatically applied in these subprojects.
    */
    public fun projects(vararg projectNameOrPath: String)

    /**
     * Limit the classes that will be included in the reports for all included projects.
     *
     * For more information about the settings, see [KoverVariantConfig.sources].
     *
     * This action is executed delayed, just before all tasks are created, at the after evaluate stage.
     * A corresponding project is passed in the argument. Analyzing this project, you can make flexible configurations.
     * ```
     *  sources {
     *      if (project.name == "projectName") {
     *          excludedSourceSets.add("excluded")
     *      }
     *  }
     * ```
     */
    public fun sources(config: Action<KoverMergingVariantSources>)

    /**
     * Instrumentation settings for all included projects.
     *
     * For more information about the settings, see [KoverVariantConfig.instrumentation].
     *
     * This action is executed delayed, just before all tasks are created, at the after evaluate stage.
     * A corresponding project is passed in the argument. Analyzing this project, you can make flexible configurations.
     * ```
     *  instrumentation {
     *      if (project.name == "projectName") {
     *          excludedClasses.add("foo.bar.*")
     *      }
     *  }
     * ```
     */
    public fun instrumentation(config: Action<KoverMergingInstrumentation>)

    /**
     * Create custom report variant with name [variantName] in all included projects.
     *
     * For more information about the settings, see [KoverCurrentProjectVariantsConfig.createVariant].
     *
     * This action is executed delayed, just before all tasks are created, at the after evaluate stage.
     * A corresponding project is passed in the argument. Analyzing this project, you can make flexible configurations.
     * ```
     *  createVariant("custom") {
     *      if (project.plugins.hasPlugin("kotlin")) {
     *          add("jvm")
     *      }
     *  }
     * ```
     */
    public fun createVariant(variantName: String, config: Action<KoverMergingVariantCreate>)
}

/**
 * Configuration scope.
 */
@KoverGradlePluginDsl
public interface KoverProjectAware {
    /**
     * The current project that is being configured.
     */
    public val project: Project
}

/**
 * Limit the classes that will be included in the reports for all included projects.
 *
 * For more information about the settings, see [KoverVariantConfig.sources].
 *
 * This action is executed delayed, just before all tasks are created, at the after evaluate stage.
 * A corresponding project is passed in the argument. Analyzing this project, you can make flexible configurations.
 * ```
 *  sources {
 *      if (project.name == "projectName") {
 *          excludedSourceSets.add("excluded")
 *      }
 *  }
 * ```
 */
@KoverGradlePluginDsl
public interface KoverMergingVariantSources: KoverVariantSources, KoverProjectAware

/**
 * Instrumentation settings for all included projects.
 *
 * For more information about the settings, see [KoverVariantConfig.instrumentation].
 *
 * This action is executed delayed, just before all tasks are created, at the after evaluate stage.
 * A corresponding project is passed in the argument. Analyzing this project, you can make flexible configurations.
 * ```
 *  instrumentation {
 *      if (project.name == "projectName") {
 *          excludedClasses.add("foo.bar.*")
 *      }
 *  }
 * ```
 */
@KoverGradlePluginDsl
public interface KoverMergingInstrumentation: KoverProjectInstrumentation, KoverProjectAware

/**
 * Create custom report variant in all included projects.
 *
 * For more information about the settings, see [KoverCurrentProjectVariantsConfig.createVariant].
 *
 * This action is executed delayed, just before all tasks are created, at the after evaluate stage.
 * A corresponding project is passed in the argument. Analyzing this project, you can make flexible configurations.
 * ```
 *  createVariant("custom") {
 *      if (project.plugins.hasPlugin("kotlin")) {
 *          add("jvm")
 *      }
 *  }
 * ```
 */
@KoverGradlePluginDsl
public interface KoverMergingVariantCreate: KoverVariantCreateConfig, KoverProjectAware

