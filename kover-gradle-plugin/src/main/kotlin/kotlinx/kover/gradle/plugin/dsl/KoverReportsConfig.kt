/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("UNUSED_PARAMETER")

package kotlinx.kover.gradle.plugin.dsl

import kotlinx.kover.gradle.plugin.commons.KoverDeprecationException
import kotlinx.kover.gradle.plugin.commons.KoverMigrations
import org.gradle.api.*
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

/**
 * Configuration of Kover reports.
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
 *              filters {
 *                  // override report filters for total reports
 *              }
 *
 *              html {
 *                  // configure total HTML report
 *              }
 *
 *              xml {
 *                  // configure total XML report
 *              }
 *
 *              verify {
 *                  // configure total coverage verification
 *              }
 *          }
 *
 *          variant("custom") {
 *              filters {
 *                  // override report filters for reports of 'custom' variant
 *              }
 *
 *              html {
 *                  // configure HTML report for reports of 'custom' variant
 *              }
 *
 *              xml {
 *                  // configure XML report for reports of 'custom' variant
 *              }
 *
 *              verify {
 *                  // configure coverage verification for reports of 'custom' variant
 *              }
 *          }
 *      }
 *  }
 * ```
 */
public interface KoverReportsConfig {
    /**
     * Specify common filters for all report variants, these filters will be inherited in HTML/XML/verification reports.
     * They can be redefined in the settings of a specific report variant.
     * ```
     *  filters {
     *      excludes {
     *          // ...
     *      }
     *
     *      includes {
     *          // ...
     *      }
     *  }
     * ```
     */
    public fun filters(config: Action<KoverReportFiltersConfig>)


    /**
     * Specify common verification rules for all report variants: JVM and Android build variants.
     * They can be overridden in the settings for a specific report set for particular variant.
     * ```
     *  verify {
     *      rule {
     *          // verification rule
     *      }
     *
     *      rule("custom rule name") {
     *          // named verification rule
     *      }
     *  }
     * ```
     */
    public fun verify(config: Action<KoverVerificationRulesConfig>)

    @Deprecated(
        message = "Default reports was removed, the concepts of total and custom reports are now used. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        level = DeprecationLevel.ERROR
    )
    public fun defaults(config: Action<*>) {
        throw KoverDeprecationException("Default reports was removed, the concepts of total and custom reports are now used. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
    }

    @Deprecated(
        message = "Block was renamed to variant. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("variant"),
        level = DeprecationLevel.ERROR
    )
    public fun androidReports(variant: String, config: Action<KoverReportSetConfig>) {
        throw KoverDeprecationException("Block androidReports was renamed to variant. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
    }

    /**
     * Configure reports for all code of current project and `kover` dependencies.
     *
     * example:
     * ```
     * kover {
     *      reports {
     *          total {
     *              filters {
     *                  // override report filters for total reports
     *              }
     *              html {
     *                  // configure HTML report for all code of current project and `kover` dependencies.
     *              }
     *              xml {
     *                  // configure XML report for all code of current project and `kover` dependencies.
     *              }
     *              verify {
     *                  // configure coverage verification all code of current project and `kover` dependencies.
     *              }
     *          }
     *      }
     * }
     * ```
     */
    public fun total(config: Action<KoverReportSetConfig>)

    /**
     * Configure reports for classes of specified named Kover report variant.
     *
     * example:
     * ```
     * kover {
     *      reports {
     *          variant("debug") {
     *              filters {
     *                  // override report filters for reports of 'debug' variant
     *              }
     *
     *              html {
     *                  // configure HTML report for 'debug' variant
     *              }
     *
     *              xml {
     *                  // configure XML report for 'debug' variant
     *              }
     *
     *              verify {
     *                  // configure coverage verification for 'debug' variant
     *              }
     *          }
     *      }
     * }
     * ```
     */
    public fun variant(variant: String, config: Action<KoverReportSetConfig>)

}

/**
 * Type to configure report set for a specific variant
 *
 * example:
 * ```
 *  filters {
 *      // override report filters
 *  }
 *  html {
 *      // configure HTML report
 *  }
 *  xml {
 *      // configure XML report
 *  }
 *  verify {
 *      // configure coverage verification
 *  }
 * ```
 */
public interface KoverReportSetConfig {
    /**
     * Specify common report filters, these filters will be inherited in HTML/XML/verification and other reports.
     *
     * Using this block clears all the filters specified earlier.
     * In order not to clear the existing filters, but to add new ones, use [filtersAppend].
     *
     * ```
     *  filters {
     *      excludes {
     *          // ...
     *      }
     *
     *      includes {
     *          // ...
     *      }
     *  }
     * ```
     */
    public fun filters(config: Action<KoverReportFiltersConfig>)

    /**
     * Specify common report filters, these filters will be inherited in HTML/XML/verification and other reports.
     *
     * Using this block will add additional filters to those that were inherited and specified earlier.
     * In order to clear the existing filters, use [filters].
     *
     * ```
     *  filtersAppend {
     *      excludes {
     *          // ...
     *      }
     *
     *      includes {
     *          // ...
     *      }
     *  }
     * ```
     */
    public fun filtersAppend(config: Action<KoverReportFiltersConfig>)



    /**
     * Configure HTML report for current report variant.
     * ```
     * html {
     *     title = "Custom title"
     *
     *     // Generate an HTML report when running the `check` task
     *     onCheck = false
     *
     *     // Specify HTML report directory
     *     htmlDir = layout.buildDirectory.dir("my-html-report")
     * }
     * ```
     */
    public fun html(config: Action<KoverHtmlTaskConfig>)

    /**
     * Configure XML report for current report variant.
     * ```
     * xml {
     *     // Generate an XML report when running the `check` task
     *     onCheck = true
     *
     *     // XML report title (the location depends on the library)
     *     title = "Custom XML report title"
     *
     *     // Specify file to generate XML report
     *     xmlFile = layout.buildDirectory.file("my-xml-report.xml")
     * }
     * ```
     */
    public fun xml(config: Action<KoverXmlTaskConfig>)

    /**
     * Configure Kover binary report for current report variant.
     * ```
     * binary {
     *     // Generate binary report when running the `check` task
     *     onCheck = true
     *
     *     // Specify file to generate binary report
     *     file = layout.buildDirectory.file("my-project-report/report.bin")
     * }
     * ```
     *
     * Kover binary report is compatible with IntelliJ Coverage report (ic)
     */
    public fun binary(config: Action<KoverBinaryTaskConfig>)

    /**
     * Configure coverage verification for current report variant.
     *
     * Using this block clears all the bounds specified earlier.
     * In order not to clear the existing bounds, but to add new ones, use [verifyAppend].
     *
     * ```
     *  verify {
     *      onCheck = true
     *
     *      rule {
     *          // ...
     *      }
     *
     *      rule("Custom Name") {
     *          // ...
     *      }
     *  }
     * ```
     */
    public fun verify(config: Action<KoverVerifyTaskConfig>)

    /**
     * Configure coverage verification for current report variant.
     *
     * Using this block will add additional bounds to those that were inherited and specified earlier.
     * In order to clear the existing bounds, use [verify].
     *
     * ```
     *  verifyAppend {
     *      onCheck = true
     *
     *      rule {
     *          // ...
     *      }
     *
     *      rule("Custom Name") {
     *          // ...
     *      }
     *  }
     * ```
     */
    public fun verifyAppend(config: Action<KoverVerifyTaskConfig>)

    /**
     * Configure coverage printing to the log for current report variant.
     * ```
     *  log {
     *      onCheck = true
     *
     *      filters {
     *          // ...
     *      }
     *      header = null
     *      format = "<entity> line coverage: <value>%"
     *      groupBy = GroupingEntityType.APPLICATION
     *      coverageUnits = MetricType.LINE
     *      aggregationForGroup = AggregationType.COVERED_PERCENTAGE
     *  }
     * ```
     */
    public fun log(config: Action<KoverLogTaskConfig>)
}

/**
 * Configuration of coverage printing to the log for current report variant.
 * ```
 *  log {
 *      onCheck = true
 *
 *      filters {
 *          // ...
 *      }
 *      header = null
 *      format = "<entity> line coverage: <value>%"
 *      groupBy = GroupingEntityType.APPLICATION
 *      coverageUnits = MetricType.LINE
 *      aggregationForGroup = AggregationType.COVERED_PERCENTAGE
 *  }
 * ```
 */
public interface KoverLogTaskConfig {
    @Deprecated(
        message = "It is forbidden to override filters for a specific report, use custom report variants to create reports with a different set of filters. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        level = DeprecationLevel.ERROR
    )
    public fun filters(config: Action<KoverReportFiltersConfig>) {
        throw KoverDeprecationException("It is forbidden to override filters for the log report, use custom report variants to create reports with a different set of filters. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
    }

    /**
     * Print coverage when running the `check` task.
     *
     * `false` by default.
     */
    public val onCheck: Property<Boolean>

    /**
     * Add a header line to the output before the lines with coverage.
     *
     * Absent by default.
     */
    public val header: Property<String>

    /**
     * Format of the strings to print coverage for the specified in [groupBy] group.
     *
     * The following placeholders can be used:
     *  - `<value>` - coverage value
     *  - `<entity>` - name of the entity by which the grouping took place. `application` if [groupBy] is [GroupingEntityType.APPLICATION].
     *
     * `"<entity> line coverage: <value>%"` by default.
     */
    public val format: Property<String>

    /**
     * Specifies by which entity the code for separate coverage evaluation will be grouped.
     *
     *
     * [GroupingEntityType.APPLICATION] by default.
     */
    public val groupBy: Property<GroupingEntityType>

    /**
     * The type of application code division (unit type) whose unit coverage will be considered independently.
     *
     * [MetricType.LINE] by default.
     */
    public val coverageUnits: Property<MetricType>

    /**
     * Specifies aggregation function that will be calculated over all the units of the same group.
     *
     * This function used to calculate the aggregated coverage value, it uses the values of the covered and uncovered units of type [coverageUnits] as arguments.
     *
     * Result value will be printed.
     *
     * [AggregationType.COVERED_PERCENTAGE] by default.
     */
    public val aggregationForGroup: Property<AggregationType>
}

/**
 * Filters to exclude classes from reports
 */
public interface KoverReportFiltersConfig {
    /**
     * Configures class filter in order to exclude classes and functions.
     *
     * Example:
     *  ```
     *  excludes {
     *      classes("com.example.FooBar?", "com.example.*Bar")
     *      packages("com.example.subpackage")
     *      annotatedBy("*Generated*")
     *  }
     *  ```
     * Excludes have priority over includes.
     */
    public fun excludes(config: Action<KoverReportFilter>)

    /**
     * Configures class filter in order to include classes.
     *
     * Example:
     *  ```
     *  includes {
     *      classes("com.example.FooBar?", "com.example.*Bar")
     *      packages("com.example.subpackage")
     *  }
     *  ```
     * Excludes have priority over includes.
     */
    public fun includes(config: Action<KoverReportFilter>)
}

/**
 * Exclusion or inclusion class filter for Kover reports.
 *
 * Exclusions example for Kotlin:
 * ```
 *     excludes {
 *          classes("*.foo.Bar", "*.M?Class")
 *          classes(listOf("*.foo.Bar", "*.M?Class"))
 *          packages("foo.b?r", "com.*.example")
 *          val somePackages =
 *          packages(listOf("foo.b?r", "com.*.example"))
 *          annotatedBy("*Generated*", "com.example.KoverExclude")
 *      }
 * ```
 */
public interface KoverReportFilter {
    /**
     * Add specified classes to current filters.
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example:
     * ```
     *  classes("*.foo.Bar", "*.M?Class")
     * ```
     */
    public fun classes(vararg names: String)

    /**
     * Add specified classes to current filters.
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example for Groovy:
     * ```
     *  def someClasses = ["*.foo.Bar", "*.M?Class"]
     *  ...
     *  classes(someClasses)
     * ```
     *
     * Example for Kotlin:
     * ```
     *  val someClasses = listOf("*.foo.Bar", "*.M?Class")
     *  ...
     *  classes(someClasses)
     * ```
     */
    public fun classes(names: Iterable<String>)

    /**
     * Add specified classes to current filters.
     *
     * Used for lazy setup.
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example:
     * ```
     *  val excludedClass: Provider<String> = ...
     *  ...
     *  classes(excludedClass)
     * ```
     */
    public fun classes(vararg names: Provider<String>)

    /**
     * Add specified classes to current filters.
     *
     * Used for lazy setup.
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example:
     * ```
     *  val someClasses: Provider<List<String>> = ...
     *  ...
     *  classes(someClasses)
     * ```
     */
    public fun classes(names: Provider<Iterable<String>>)

    /**
     * Add all classes in specified package and its subpackages to current filters.
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example:
     * ```
     *  packages("foo.b?r", "com.*.example")
     * ```
     */
    public fun packages(vararg names: String)

    /**
     * Add all classes in specified package and its subpackages to current filters.
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example for Groovy:
     * ```
     *  def somePackages = ["foo.b?r", "com.*.example"]
     *
     *  packages(somePackages)
     * ```
     *
     * Example for Kotlin:
     * ```
     *  val somePackages = listOf("foo.b?r", "com.*.example")
     *  ...
     *  packages(somePackages)
     * ```
     */
    public fun packages(names: Iterable<String>)

    /**
     * Add all classes in specified package and its subpackages to current filters.
     *
     * Used for lazy setup.
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example:
     * ```
     *  val classA: Provider<String> = ...
     *  val classB: Provider<String> = ...
     *  packages(classA, classB)
     * ```
     */
    public fun packages(vararg names: Provider<String>)

    /**
     * Add all classes in specified package and its subpackages to current filters.
     *
     * Used for lazy setup.
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example:
     * ```
     *  val somePackages: Provider<List<String>> = ...
     *  ...
     *  packages(somePackages)
     * ```
     */
    public fun packages(names: Provider<Iterable<String>>)

    /**
     * Add to filters all classes and functions marked by specified annotations.
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example:
     * ```
     *  annotatedBy("*Generated*", "com.example.KoverExclude")
     * ```
     */
    public fun annotatedBy(vararg annotationName: String)

    /**
     * Add to filters all classes and functions marked by specified annotations.
     *
     * Used for lazy setup.
     *
     * It is acceptable to use `*` and `?` wildcards,
     * `*` means any number of arbitrary characters (including no chars), `?` means one arbitrary character.
     *
     * Example:
     * ```
     *  val annotation: Provider<String> = ...
     *  annotatedBy(annotation)
     * ```
     */
    public fun annotatedBy(vararg annotationName: Provider<String>)

    /**
     * Add all classes generated by Android plugin to filters.
     *
     * It is shortcut for:
     * ```
     * classes(
     *     "*Fragment",
     *     "*Fragment\$*",
     *     "*Activity",
     *     "*Activity\$*",
     *     "*.databinding.*",
     *     "*.BuildConfig"
     * )
     * ```
     */
    public fun androidGeneratedClasses() {
        classes(
            "*Fragment",
            "*Fragment\$*",
            "*Activity",
            "*Activity\$*",
            "*.databinding.*",
            "*.BuildConfig"
        )
    }
}

/**
 * Configure Kover HTML Report.
 *
 * Example:
 * ```
 * ...
 * html {
 *     title = "Custom title"
 *
 *     // Generate an HTML report when running the `check` task
 *     onCheck = false
 *
 *     // Specify HTML report directory
 *     htmlDir = layout.buildDirectory.dir("my-html-report")
 * }
 *  ...
 * ```
 */
public interface KoverHtmlTaskConfig {
    @Deprecated(
        message = "It is forbidden to override filters for a specific report, use custom report variants to create reports with a different set of filters. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        level = DeprecationLevel.ERROR
    )
    public fun filters(config: Action<KoverReportFiltersConfig>) {
        throw KoverDeprecationException("It is forbidden to override filters for the HTML report, use custom report variants to create reports with a different set of filters. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
    }

    /**
     * Specify header in HTML reports.
     *
     * If not specified, project path is used instead.
     */
    public val title: Property<String>

    /**
     * Specify charset in HTML reports.
     *
     * If not specified, used return value of `Charset.defaultCharset()` for Kover report generator and UTF-8 is used for JaCoCo.
     */
    public val charset: Property<String>

    /**
     * Generate an HTML report when running the `check` task.
     *
     * `false` by default.
     */
    public val onCheck: Property<Boolean>

    @Deprecated(
        message = "Function was removed, use htmlDir property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("htmlDir"),
        level = DeprecationLevel.ERROR
    )
    public fun setReportDir(dir: Any) {
        throw KoverDeprecationException("Function setReportDir(dir) was removed, use htmlDir property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
    }

    /**
     * HTML report directory.
     *
     * `"${buildDirectory}/reports/kover/html${variantName}"` by default.
     *
     * This value should not be hardcoded, it is always necessary to read the actual value from the property.
     */
    public val htmlDir: DirectoryProperty
}

/**
 * Configure Kover XML Report.
 *
 * Example:
 * ```
 * ...
 * xml {
 *     // Generate an XML report when running the `check` task
 *     onCheck = true
 *
 *     // XML report title (the location depends on the library)
 *     title = "Custom XML report title"
 *
 *     // Specify file to generate XML report
 *     xmlFile = layout.buildDirectory.file("my-xml-report.xml")
 * }
 *  ...
 * ```
 */
public interface KoverXmlTaskConfig {
    @Deprecated(
        message = "It is forbidden to override filters for a specific report, use custom report variants to create reports with a different set of filters. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        level = DeprecationLevel.ERROR
    )
    public fun filters(config: Action<KoverReportFiltersConfig>) {
        throw KoverDeprecationException("It is forbidden to override filters for the XML report, use custom report variants to create reports with a different set of filters. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
    }

    /**
     * Generate an XML report when running the `check` task.
     *
     * `false` by default.
     */
    public val onCheck: Property<Boolean>

    /**
     * Specify file to generate XML report.
     */
    @Deprecated(
        message = "Function was removed, use xmlFile property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("xmlFile"),
        level = DeprecationLevel.ERROR
    )
    public fun setReportFile(xmlFile: Any) {
        throw KoverDeprecationException("Function xmlFile was removed, use xmlFile property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
    }

    /**
     * File for saving generated XML report.
     *
     * `"${buildDirectory}/reports/kover/report${variantName}.xml"` by default.
     *
     * This value should not be hardcoded, it is always necessary to read the actual value from the property.
     */
    public val xmlFile: RegularFileProperty

    /**
     * Specify title in XML report.
     *
     * `"Kover Gradle Plugin XML report for $projectPath"` by default.
     */
    public val title: Property<String>
}

/**
 * Configure Kover binary Report.
 *
 * Example:
 * ```
 * ...
 * binary {
 *     // Generate binary report when running the `check` task
 *     onCheck = true
 *
 *     // Specify file to generate binary report
 *     file = layout.buildDirectory.file("my-project-report/report.bin")
 * }
 *  ...
 * ```
 */
public interface KoverBinaryTaskConfig {
    @Deprecated(
        message = "It is forbidden to override filters for a specific report, use custom report variants to create reports with a different set of filters. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        level = DeprecationLevel.ERROR
    )
    public fun filters(config: Action<KoverReportFiltersConfig>) {
        throw KoverDeprecationException("It is forbidden to override filters for the binary report, use custom report variants to create reports with a different set of filters. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
    }

    /**
     * Generate binary report when running the `check` task.
     *
     * `false` by default.
     */
    public val onCheck: Property<Boolean>

    /**
     * Specify file to generate binary report.
     *
     * `"${buildDirectory}/reports/kover/report${variantName}.bin"` by default.
     *
     * This value should not be hardcoded, it is always necessary to read the actual value from the property.
     */
    public val file: RegularFileProperty
}

/**
 * Configuration of the coverage's result verification with the specified rules.
 *
 * Example:
 * ```
 *  verify {
 *      onCheck = true
 *
 *      rule {
 *          // ...
 *      }
 *
 *      rule("Custom Name") {
 *          // ...
 *      }
 *  }
 * ```
 */
public interface KoverVerifyTaskConfig: KoverVerificationRulesConfig {
    /**
     * Verify coverage when running the `check` task.
     *
     * `true` for total verification of all code in the project, `false` otherwise.
     */
    public val onCheck: Property<Boolean>
}

/**
 * Configuration to specify verification rules.
 *
 * Example:
 * ```
 *  verify {
 *      rule {
 *          // verification rule
 *      }
 *
 *      rule("custom rule name") {
 *          // named verification rule
 *      }
 *  }
 * ```
 */
public interface KoverVerificationRulesConfig {
    /**
     * Add new coverage verification rule to check after test task execution.
     */
    public fun rule(config: Action<KoverVerifyRule>)

    /**
     * Add new named coverage verification rule to check after test task execution.
     *
     * The name will be displayed in case of a verification error if Kover Tool was used.
     */
    public fun rule(name: String, config: Action<KoverVerifyRule>)
}

/**
 * Describes a single Kover verification task rule (that is part of Gradle's verify),
 * with the following configurable parameters:
 *
 * - Which classes and packages are included or excluded into the current rule
 * - What coverage bounds are enforced by current rules
 * - What kind of bounds (branches, lines, bytecode instructions) are checked by bound rules.
 */
public interface KoverVerifyRule {
    /**
     * Specifies by which entity the code for separate coverage evaluation will be grouped.
     * [GroupingEntityType.APPLICATION] by default.
     */
    public val groupBy: Property<GroupingEntityType>

    /**
     * Specifies that the rule is checked during verification.
     *
     * `false` by default.
     */
    public val disabled: Property<Boolean>

    /**
     * Specifies that the rule is checked during verification.
     */
    @Deprecated(
        message = "Property was renamed to disabled and inverted. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        level = DeprecationLevel.ERROR
    )
    public var isEnabled: Boolean
        get() {
            throw KoverDeprecationException("Property isEnabled was renamed to disabled and inverted. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
        }
        set(value) {
            throw KoverDeprecationException("Property isEnabled was renamed to disabled and inverted. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
        }

    @Deprecated(
        message = "Property was renamed to groupBy. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("groupBy"),
        level = DeprecationLevel.ERROR
    )
    public var entity: GroupingEntityType
        get() {
            throw KoverDeprecationException("Property entity was renamed to groupBy. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
        }
        set(value) {
            throw KoverDeprecationException("Property entity was renamed to groupBy. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
        }

    @Deprecated(
        message = "It is forbidden to override filters for a specific report, use custom report variants to create reports with a different set of filters. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        level = DeprecationLevel.ERROR
    )
    public fun filters(config: Action<KoverReportFiltersConfig>) {
        throw KoverDeprecationException("It is forbidden to override filters for the verification report, use custom report variants to create reports with a different set of filters. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
    }

    /**
     * Specifies the set of verification rules that control the
     * coverage conditions required for the verification task to pass.
     *
     * An example of bound configuration:
     * ```
     * // At least 75% of lines should be covered in order for build to pass
     * bound {
     *     aggregationForGroup = AggregationType.COVERED_PERCENTAGE // Default aggregation
     *     metric = MetricType.LINE
     *     min = 75
     * }
     * ```
     *
     * @see KoverVerifyBound
     */
    public fun bound(config: Action<KoverVerifyBound>)

    /**
     * A shortcut for
     * ```
     * bound {
     *     min = min
     * }
     * ```
     *
     * @see bound
     */
    public fun minBound(min: Int)

    /**
     * A shortcut for
     * ```
     * bound {
     *     min = min
     * }
     * ```
     *
     * @see bound
     */
    public fun minBound(min: Provider<Int>)

    /**
     * A shortcut for
     * ```
     * bound {
     *     max = max
     * }
     * ```
     *
     * @see bound
     */
    public fun maxBound(max: Int)

    /**
     * A shortcut for
     * ```
     * bound {
     *     max = max
     * }
     * ```
     *
     * @see bound
     */
    public fun maxBound(max: Provider<Int>)

    // Default parameters values supported only in Kotlin.

    /**
     * A shortcut for
     * ```
     * bound {
     *     min = minValue
     *     coverageUnits = coverageUnits
     *     aggregationForGroup = aggregationForGroup
     * }
     * ```
     *
     * @see bound
     */
    public fun minBound(
        minValue: Int,
        coverageUnits: MetricType = MetricType.LINE,
        aggregationForGroup: AggregationType = AggregationType.COVERED_PERCENTAGE
    )

    /**
     * A shortcut for
     * ```
     * bound {
     *     max = maxValue
     *     coverageUnits = coverageUnits
     *     aggregation = aggregation
     * }
     * ```
     *
     * @see bound
     */
    public fun maxBound(
        maxValue: Int,
        coverageUnits: MetricType = MetricType.LINE,
        aggregation: AggregationType = AggregationType.COVERED_PERCENTAGE
    )

    /**
     * A shortcut for
     * ```
     * bound {
     *     max = max
     *     min = min
     *     coverageUnits = coverageUnits
     *     aggregation = aggregation
     * }
     * ```
     *
     * @see bound
     */
    public fun bound(
        min: Int,
        max: Int,
        coverageUnits: MetricType = MetricType.LINE,
        aggregation: AggregationType = AggregationType.COVERED_PERCENTAGE
    )

}

/**
 * Describes a single bound for the verification rule to enforce;
 * Bound specifies what type of coverage is enforced (branches, lines, instructions),
 * how coverage is aggregated (raw number or percents) and what numerical values of coverage
 * are acceptable.
 */
public interface KoverVerifyBound {
    /**
     * Specifies minimal value to compare with aggregated coverage value.
     * The comparison occurs only if the value is present.
     *
     * Absent by default.
     */
    public val min: Property<Int>

    /**
     * Specifies maximal value to compare with counter value.
     * The comparison occurs only if the value is present.
     *
     * Absent by default.
     */
    public val max: Property<Int>

    /**
     * The type of application code division (unit type) whose unit coverage will be considered independently.
     * It affects which blocks the value of the covered and missed units will be calculated for.
     *
     * [MetricType.LINE] by default.
     */
    public val coverageUnits: Property<MetricType>

    /**
     * Specifies aggregation function that will be calculated over all the units of the same group.
     *
     * This function used to calculate the aggregated coverage value, it uses the values of the covered and uncovered units of type [coverageUnits] as arguments.
     *
     * Result value will be compared with the bounds.
     *
     * [AggregationType.COVERED_PERCENTAGE] by default.
     */
    public val aggregationForGroup: Property<AggregationType>


    @Deprecated(
        message = "Property was renamed to min. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("min"),
        level = DeprecationLevel.ERROR
    )
    public var minValue: Int?
        get() {
            throw KoverDeprecationException("Property minValue was renamed to min. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
        }
        set(value) {
            throw KoverDeprecationException("Property minValue was renamed to min. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
        }


    @Deprecated(
        message = "Property was renamed to max. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("max"),
        level = DeprecationLevel.ERROR
    )
    public var maxValue: Int?
        get() {
            throw KoverDeprecationException("Property maxValue was renamed to max. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
        }
        set(value) {
            throw KoverDeprecationException("Property maxValue was renamed to max. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
        }

    @Deprecated(
        message = "Property was renamed to coverageUnits. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("coverageUnits"),
        level = DeprecationLevel.ERROR
    )
    public var metric: MetricType
        get() {
            throw KoverDeprecationException("Property metric was renamed to coverageUnits. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
        }
        set(value) {
            throw KoverDeprecationException("Property metric was renamed to coverageUnits. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
        }

    @Deprecated(
        message = "Property was renamed to aggregationForGroup. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("aggregationForGroup"),
        level = DeprecationLevel.ERROR
    )
    public var aggregation: AggregationType
        get() {
            throw KoverDeprecationException("Property aggregation was renamed to aggregationForGroup. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
        }
        set(value) {
            throw KoverDeprecationException("Property aggregation was renamed to aggregationForGroup. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
        }
}

/**
 * Type of the metric to evaluate code coverage.
 */
public enum class MetricType {
    /**
     * Number of lines.
     */
    LINE,

    /**
     * Number of JVM bytecode instructions.
     */
    INSTRUCTION,

    /**
     * Number of branches covered.
     */
    BRANCH
}

/**
 * Type of counter value to compare with minimal and maximal values if them defined.
 */
public enum class AggregationType(val isPercentage: Boolean) {
    COVERED_COUNT(false),
    MISSED_COUNT(false),
    COVERED_PERCENTAGE(true),
    MISSED_PERCENTAGE(true)
}

/**
 * Entity type for grouping code to coverage evaluation.
 */
public enum class GroupingEntityType {
    /**
     * Counts the coverage values for all code.
     */
    APPLICATION,

    /**
     * Counts the coverage values for each class separately.
     */
    CLASS,

    /**
     * Counts the coverage values for each package that has classes separately.
     */
    PACKAGE
}