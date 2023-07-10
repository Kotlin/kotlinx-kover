/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

import kotlinx.kover.gradle.plugin.commons.KoverMigrations
import org.gradle.api.*
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import java.io.*

/**
 * Configuration of Kover reports.
 *
 * Example of usage:
 * ```
 *  koverReport {
 *      filters {
 *          // common filters for all reports of all variants
 *      }
 *
 *      // default reports - special reports that are filled in by default with class measurements from Kotlin/JVM or Kotlin/MPP projects
 *      defaults {
 *          // add content of reports for specified variant to default reports
 *          mergeWith("buildVariant")
 *
 *          filters {
 *              // override report filters for default reports
 *          }
 *
 *          html {
 *              // configure default HTML report
 *          }
 *
 *          xml {
 *              // configure default XML report
 *          }
 *
 *          verify {
 *              // configure default coverage verification
 *          }
 *      }
 *
 *      androidReports("buildVariant") {
 *          filters {
 *              // override report filters for reports of specified Android build variant
 *          }
 *
 *          html {
 *              // configure HTML report for specified Android build variant
 *          }
 *
 *          xml {
 *              // configure XML report for specified Android build variant
 *          }
 *
 *          verify {
 *              // configure coverage verification for specified Android build variant
 *          }
 *      }
 *
 *  }
 * ```
 */
public interface KoverReportExtension {
    /**
     * Specify common filters for the current report variant, these filters will be inherited in HTML/XML/verification reports.
     * They can be redefined in the settings of a specific report.
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
    public fun filters(config: Action<KoverReportFilters>)

    /**
     * Configure reports for classes from Kotlin/JVM or Kotlin/MPP projects.
     * Also content from specified Android build variant can be added by calling `mergeWith`.
     *
     * example:
     * ```
     * koverReport {
     *      defaults {
     *          // add content of reports for specified variant to default reports
     *          mergeWith("buildVariant")
     *
     *          filters {
     *              // override report filters for default reports
     *          }
     *
     *          html {
     *              // configure default HTML report
     *          }
     *
     *          xml {
     *              // configure default XML report
     *          }
     *
     *          verify {
     *              // configure default coverage verification
     *          }
     *      }
     * }
     * ```
     */
    public fun defaults(config: Action<KoverDefaultReportsConfig>)

    /**
     * Configure reports for classes of specified Android build variant.
     *
     * example:
     * ```
     * koverReport {
     *      androidReports("debug") {
     *          filters {
     *              // override report filters for reports of 'debug' Android build variant
     *          }
     *
     *          html {
     *              // configure HTML report for 'debug' Android build variant
     *          }
     *
     *          xml {
     *              // configure XML report for 'debug' Android build variant
     *          }
     *
     *          verify {
     *              // configure coverage verification for 'debug' Android build variant
     *          }
     *      }
     * }
     * ```
     */
    public fun androidReports(variant: String, config: Action<KoverReportsConfig>)
}

/**
 *  Configuration for default variant reports
 */
public interface KoverDefaultReportsConfig: KoverReportsConfig {
    /**
     * Add the contents of the reports with specified variant to the default reports.
     *
     * Affects the default reports of this project and any project that specifies this project as a dependency.
     */
    public fun mergeWith(otherVariant: String)
}

public interface KoverReportsConfig {
    /**
     * Specify common filters for the current report variant, these filters will be inherited in HTML/XML/verification reports.
     * They can be redefined in the settings of a specific report.
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
    public fun filters(config: Action<KoverReportFilters>)

    /**
     * Configure HTML report for current report variant.
     * ```
     *  html {
     *      filters {
     *          // ...
     *      }
     *
     *      title = "My report title"
     *      onCheck = false
     *      setReportDir(layout.buildDirectory.dir("my-project-report/html-result"))
     *  }
     * ```
     */
    public fun html(config: Action<KoverHtmlReportConfig>)

    /**
     * Configure XML report for current report variant.
     * ```
     *  xml {
     *      filters {
     *          // ...
     *      }
     *
     *      onCheck = false
     *      setReportFile(layout.buildDirectory.file("my-project-report/result.xml"))
     *  }
     * ```
     */
    public fun xml(config: Action<KoverXmlReportConfig>)

    /**
     * Configure coverage verification for current report variant.
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
    public fun verify(config: Action<KoverVerifyReportConfig>)

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
    public fun log(config: Action<KoverLogReportConfig>)
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
public interface KoverLogReportConfig {
    /**
     * Override common filters only for logging report.
     */
    public fun filters(config: Action<KoverReportFilters>)

    /**
     * Print coverage when running the `check` task.
     *
     * `false` by default.
     */
    public var onCheck: Boolean

    /**
     * Add a header line to the output before the lines with coverage.
     *
     * If value is `null` then the header is absent.
     *
     * `null` by default.
     */
    public var header: String?

    /**
     * Format of the strings to print coverage for the specified in [groupBy] group.
     *
     * The following placeholders can be used:
     *  - `<value>` - coverage value
     *  - `<entity>` - name of the entity by which the grouping took place. `application` if [groupBy] is [GroupingEntityType.APPLICATION].
     *
     * `"<entity> line coverage: <value>%"` if value is `null`.
     *
     * `null` by default.
     */
    public var format: String?

    /**
     * Specifies by which entity the code for separate coverage evaluation will be grouped.
     *
     *
     * [GroupingEntityType.APPLICATION] if value is `null`.
     *
     * `null` by default.
     */
    public var groupBy: GroupingEntityType?

    /**
     * Specifies which metric is used for coverage evaluation.
     *
     * [MetricType.LINE] if value is `null`.
     *
     * `null` by default.
     */
    public var coverageUnits: MetricType?

    /**
     * Specifies aggregation function that will be calculated over all the elements of the same group. This function returns the printed value.
     *
     * [AggregationType.COVERED_PERCENTAGE] if value is `null`.
     *
     * `null` by default.
     */
    public var aggregationForGroup: AggregationType?
}

/**
 * Filters to excludes
 */
public interface KoverReportFilters {


    /**
     * Configures class filter in order to exclude declarations marked by specific annotations.
     *
     * Example:
     *  ```
     *  annotations {
     *      excludes += "com.example.Generated"
     *  }
     *  ```
     */

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

    @Deprecated(
        message = "Class filters was moved into 'excludes { classes(\"fq.name\") }' or 'includes { classes(\"fq.name\") }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public fun classes(block: () -> Unit) { }

    @Deprecated(
        message = "Class inclusion filters was moved into 'includes { classes(\"fq.name\") }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public val includes: MutableList<String>
        get() = mutableListOf()

    @Deprecated(
        message = "Class exclusion filters was moved into 'excludes { classes(\"fq.name\") }'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public val excludes: MutableList<String>
        get() = mutableListOf()
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
     * Add all classes in specified package and it subpackages to current filters.
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
     * Add all classes in specified package and it subpackages to current filters.
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
}

/**
 * Configure Kover HTML Report.
 *
 * Example:
 * ```
 * ...
 * html {
 *     // Filter the classes that will be included in the HTML report.
 *     // This filter does not affect the list of classes that will be instrumented and it is applied only to the report of the current project.
 *     filters {
 *         // ...
 *     }
 *
 *     title = "Custom title"
 *
 *     // Generate an HTML report when running the `check` task
 *     onCheck = false
 *
 *     // Specify HTML report directory
 *     setReportDir(layout.buildDirectory.file("my-html-report"))
 * }
 *  ...
 * ```
 */
public interface KoverHtmlReportConfig {
    /**
     * Override common filters only for HTML report.
     */
    public fun filters(config: Action<KoverReportFilters>)

    /**
     * Specify header in HTML reports.
     *
     * If not specified, project path is used instead.
     */
    public var title: String?

    /**
     * Specify charset in HTML reports.
     *
     * If not specified, used return value of `Charset.defaultCharset()` for Kover report generator and UTF-8 is used for JaCoCo.
     */
    public var charset: String?

    /**
     * Generate an HTML report when running the `check` task.
     */
    public var onCheck: Boolean

    /**
     * Specify HTML report directory.
     */
    public fun setReportDir(dir: File)

    /**
     * Specify HTML report directory.
     */
    public fun setReportDir(dir: Provider<Directory>)

    @Deprecated(
        message = "Property was removed. Use function 'setReportDir(file)'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("setReportDir"),
        level = DeprecationLevel.ERROR
    )
    public val reportDir: Nothing?
        get() = null

    @Deprecated(
        message = "Use function 'filters' instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("filters"),
        level = DeprecationLevel.ERROR
    )
    public fun overrideFilters(block: () -> Unit) { }
}

/**
 * Configure Kover XML Report.
 *
 * Example:
 * ```
 * ...
 * xml {
 *     // Filter the classes that will be included in the XML report.
 *     // This filter does not affect the list of classes that will be instrumented and it is applied only to the report of the current project.
 *     filters {
 *         // ...
 *     }
 *
 *     // Generate an XML report when running the `check` task
 *     onCheck = false
 *
 *     // Specify file to generate XML report
 *     setReportFile(layout.buildDirectory.file("my-xml-report.xml"))
 * }
 *  ...
 * ```
 */
public interface KoverXmlReportConfig {
    /**
     * Override common filters only for XML report.
     */
    public fun filters(config: Action<KoverReportFilters>)

    /**
     * Generate an XML report when running the `check` task.
     */
    public var onCheck: Boolean

    /**
     * Specify file to generate XML report.
     */
    public fun setReportFile(xmlFile: File)

    /**
     * Specify file to generate XML report.
     */
    public fun setReportFile(xmlFile: Provider<RegularFile>)

    @Deprecated(
        message = "Property was removed. Use function 'setReportFile(file)'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("setReportFile"),
        level = DeprecationLevel.ERROR
    )
    public val reportFile: Nothing?
        get() = null

    @Deprecated(
        message = "Use function 'filters' instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("filters"),
        level = DeprecationLevel.ERROR
    )
    public fun overrideFilters(block: () -> Unit) { }
}

/**
 * Configuration of the coverage's result verification with the specified rules.
 *
 * Example:
 * ```
 *  verify {
 *      onCheck = true
 *      rule {
 *          // verification rule
 *      }
 *  }
 * ```
 */
public interface KoverVerifyReportConfig {
    /**
     * Verify coverage when running the `check` task.
     * `null` by default, for Kotlin JVM and Kotlin MPP triggered on `check` task, but not for Android.
     */
    public var onCheck: Boolean

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
     * Specifies that the rule is checked during verification.
     */
    public var isEnabled: Boolean

    /**
     * Specifies by which entity the code for separate coverage evaluation will be grouped.
     */
    public var entity: GroupingEntityType

    /**
     * Specifies the set of Kover report filters that control
     * included or excluded classes and packages for verification.
     *
     * An example of filter configuration:
     * ```
     * filters {
     *    excludes {
     *        // Do not include deprecated package into verification coverage data
     *        className("org.jetbrains.deprecated.*")
     *     }
     * }
     * ```
     *
     * @see KoverReportFilter
     */
    public fun filters(config: Action<KoverReportFilters>)

    /**
     * Specifies the set of verification rules that control the
     * coverage conditions required for the verification task to pass.
     *
     * An example of bound configuration:
     * ```
     * // At least 75% of lines should be covered in order for build to pass
     * bound {
     *     aggregation = AggregationType.COVERED_PERCENTAGE // Default aggregation
     *     metric = MetricType.LINE
     *     minValue = 75
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
     *     minValue = minValue
     * }
     * ```
     *
     * @see bound
     */
    public fun minBound(minValue: Int)

    /**
     * A shortcut for
     * ```
     * bound {
     *     maxValue = maxValue
     * }
     * ```
     *
     * @see bound
     */
    public fun maxBound(maxValue: Int)

    // Default parameters values supported only in Kotlin.

    /**
     * A shortcut for
     * ```
     * bound {
     *     minValue = minValue
     *     metric = metric
     *     aggregation = aggregation
     * }
     * ```
     *
     * @see bound
     */
    public fun minBound(
        minValue: Int,
        metric: MetricType = MetricType.LINE,
        aggregation: AggregationType = AggregationType.COVERED_PERCENTAGE
    )

    /**
     * A shortcut for
     * ```
     * bound {
     *     maxValue = maxValue
     *     metric = metric
     *     aggregation = aggregation
     * }
     * ```
     *
     * @see bound
     */
    public fun maxBound(
        maxValue: Int,
        metric: MetricType = MetricType.LINE,
        aggregation: AggregationType = AggregationType.COVERED_PERCENTAGE
    )

    /**
     * A shortcut for
     * ```
     * bound {
     *     maxValue = maxValue
     *     minValue = minValue
     *     metric = metric
     *     aggregation = aggregation
     * }
     * ```
     *
     * @see bound
     */
    public fun bound(
        minValue: Int,
        maxValue: Int,
        metric: MetricType = MetricType.LINE,
        aggregation: AggregationType = AggregationType.COVERED_PERCENTAGE
    )

    @Deprecated(
        message = "Property was renamed to 'entity'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("entity"),
        level = DeprecationLevel.ERROR
    )
    public var target: GroupingEntityType
        get() = GroupingEntityType.APPLICATION
        set(@Suppress("UNUSED_PARAMETER") value) {}

    @Deprecated(
        message = "Property 'name' was removed, specify rule name in `rule(myName) { ... }` function. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public var name: String?

    @Deprecated(
        message = "Use function 'filters' instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("filters"),
        level = DeprecationLevel.ERROR
    )
    public fun overrideClassFilter(block: () -> Unit) {}
}

/**
 * Describes a single bound for the verification rule to enforce;
 * Bound specifies what type of coverage is enforced (branches, lines, instructions),
 * how coverage is aggerageted (raw number or percents) and what numerical values of coverage
 * are acceptable.
 */
public interface KoverVerifyBound {
    /**
     * Specifies minimal value to compare with counter value.
     */
    public var minValue: Int?

    /**
     * Specifies maximal value to compare with counter value.
     */
    public var maxValue: Int?

    /**
     * Specifies which metric is used for code coverage verification.
     */
    public var metric: MetricType

    /**
     * Specifies type of lines counter value to compare with minimal and maximal values if them defined.
     * Default is [AggregationType.COVERED_PERCENTAGE]
     */
    public var aggregation: AggregationType

    @Deprecated(
        message = "Property was renamed to 'metric'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("metric"),
        level = DeprecationLevel.ERROR
    )
    public var counter: MetricType
        get() = MetricType.LINE
        set(@Suppress("UNUSED_PARAMETER") value) {}

    @Deprecated(
        message = "Property was renamed to 'aggregation'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("aggregation"),
        level = DeprecationLevel.ERROR
    )
    public var valueType: AggregationType
        get() = AggregationType.COVERED_PERCENTAGE
        set(@Suppress("UNUSED_PARAMETER") value) {}


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
    PACKAGE;

    @Deprecated(
        message = "Entry was renamed to 'APPLICATION'. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("APPLICATION"),
        level = DeprecationLevel.ERROR
    )
    object ALL
}
