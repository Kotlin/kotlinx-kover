/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

import kotlinx.kover.gradle.plugin.dsl.KoverVersions.JACOCO_TOOL_DEFAULT_VERSION
import org.gradle.api.Action
import org.gradle.api.provider.Property

/**
 * Project extension for Kover Gradle Plugin.
 */
public interface KoverExtension {

    /**
     *
     */
    public fun disable()

    /**
     * Use [JaCoCo](https://www.jacoco.org/jacoco/) as coverage tool with version [JACOCO_TOOL_DEFAULT_VERSION] for measure coverage and generate reports.
     */
    public fun useJacoco()

    /**
     * Use [JaCoCo](https://www.jacoco.org/jacoco/) as coverage tool with version [version] for measure coverage and generate reports.
     */
    public fun useJacoco(version: String)

    /**
     * Specifies usage of [JaCoCo](https://www.jacoco.org/jacoco/) as coverage tool for measure coverage and generate reports.
     *
     * The version specified in the [jacocoVersion] will be used.
     */
    public val useJacoco: Property<Boolean>

    /**
     * Specifies version of [JaCoCo](https://www.jacoco.org/jacoco/) coverage tool.
     *
     * This property has an effect only if JaCoCo usage is enabled.
     *
     * [JACOCO_TOOL_DEFAULT_VERSION] by default.
     */
    public val jacocoVersion: Property<String>

    /**
     * Customize report variants shared by the current project.
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
    public fun currentProject(block: Action<KoverCurrentProjectVariantsConfig>)

    /**
     * Configuration of Kover reports.
     *
     * An individual set of reports is created for each Kover report variant.
     * All these sets can be configured independently of each other.
     *
     * The main difference between the reports sets and the report variants is that the reports are individual for each project, the settings of reports in different projects do not affect each other in any way.
     * At the same time, changing a report variant affects all reports that are based on it, for example, if several projects import a variant through a dependency `kover(project(":subproject"))`.
     *
     * Example of usage:
     * ```
     *  kover {
     *      reports {
     *          filters {
     *              // common filters for all reports of all variants
     *          }
     *          verify {
     *              // common verification rules for all variants
     *          }
     *
     *          /*
     *          Total reports set - special reports for all code of current project and it's kover dependencies.
     *          These are the reports for total variant of current project and it's kover dependencies.
     *          */
     *          total {
     *              // config
     *          }
     *
     *          /*
     *          Configure custom reports set with name "custom".
     *          These are the reports for variant "custom" of current project and it's kover dependencies.
     *          */
     *          variant("custom") {
     *          }
     *      }
     *  }
     * ```
     */
    public fun reports(block: Action<KoverReportsConfig>)

    /**
     * Configuring a merged report.
     *
     * **Attention! Using this block breaks the project isolation and is incompatible with the configuration cache!**
     * If you need configuration cache support, please explicitly configure Kover plugin in each project using [currentProject] blocks.
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
     *
     *      // apply values from `useJacoco` and `jacocoVersion`
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
    public fun merge(block: Action<KoverMergingConfig>)
}
