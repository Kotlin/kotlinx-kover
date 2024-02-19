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
     *  variants {
     *      // create report variant with custom name,
     *      // in which it is acceptable to add information from other variants of the current project, as well as `kover` dependencies
     *      create("custom") {
     *          // ...
     *      }
     *
     *      // Configure the variant that is automatically created in the current project
     *      // For example, "jvm" for JVM target or "debug" for Android build variant
     *      provided("jvm") {
     *          // ...
     *      }
     *
     *      // Configure the variant for all the code that is available in the current project.
     *      // This variant always exists for any type of project.
     *      total {
     *          // ...
     *      }
     *  }
     * ```
     */
    public fun variants(block: Action<KoverVariantsRootConfig>)


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
    public fun reports(block: Action<KoverReportConfig>)
}
