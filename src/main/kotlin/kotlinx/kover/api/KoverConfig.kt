/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.api

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.model.*
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import javax.annotation.*
import javax.inject.*

public open class KoverProjectConfig @Inject constructor(objects: ObjectFactory) {
    internal val filters: KoverProjectFilters = objects.newInstance()

    internal val instrumentation: KoverProjectInstrumentation = objects.newInstance()

    internal val xmlReport: KoverProjectXmlConfig = objects.newInstance()

    internal val htmlReport: KoverProjectHtmlConfig = objects.newInstance()

    internal val verify: KoverVerifyConfig = objects.newInstance(objects)

    /**
     * Specifies whether instrumentation is disabled for all test tasks of current project.
     *
     * `false` by default.
     */
    public val isDisabled: Property<Boolean> = objects.property()

    /**
     * Specifies the coverage engine variant to be used to collect execution data.
     */
    public val engine: Property<CoverageEngineVariant> = objects.property()

    /**
     * Configures filtering for all Kover's tasks of current project by class names and source sets.
     */
    public fun filters(config: Action<KoverProjectFilters>) {
        config.execute(filters)
    }

    /**
     * Configures a list of tasks, the execution of tests from which is registered in the coverage counters.
     *
     */
    public fun instrumentation(config: Action<KoverProjectInstrumentation>) {
        config.execute(instrumentation)
    }

    /**
     * Configures the task of generating an XML report, including XML report location and whether it should be
     * generated during the 'check' task.
     *
     * By default, [KoverPaths.PROJECT_XML_REPORT_DEFAULT_PATH] location in build directory is used.
     */
    public fun xmlReport(config: Action<KoverProjectXmlConfig>) {
        config.execute(xmlReport)
    }

    /**
     * Configures the task of generating an HTML report, including HTML report location and whether it should be
     * generated during the 'check' task.
     *
     * By default, [KoverPaths.PROJECT_HTML_REPORT_DEFAULT_PATH] location in build directory is used.
     */
    public fun htmlReport(config: Action<KoverProjectHtmlConfig>) {
        config.execute(htmlReport)
    }

    /**
     * Configures the verification task, including adding verification rules and whether it should be
     * verified during the 'check' task.
     */
    public fun verify(config: Action<KoverVerifyConfig>) {
        config.execute(verify)
    }


    // DEPRECATIONS
    // TODO delete in 0.7 version
    @Suppress("DEPRECATION")
    @get:Internal
    @Deprecated(
        message = "Property was removed in Kover API version 2, use `engine` property instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}",
        replaceWith = ReplaceWith("engine"),
        level = DeprecationLevel.ERROR
    )
    public val coverageEngine: Property<CoverageEngine> = objects.property()

    @get:Internal
    @Deprecated(
        message = "Property was removed in Kover API version 2, use `engine.set(kotlinx.kover.api.IntellijEngine(\"version\"))` instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}",
        replaceWith = ReplaceWith("engine"),
        level = DeprecationLevel.ERROR
    )
    public val intellijEngineVersion: Property<String> = objects.property()

    @get:Internal
    @Deprecated(
        message = "Property was removed in Kover API version 2, use `engine.set(kotlinx.kover.api.JacocoEngine(\"version\"))` instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}",
        replaceWith = ReplaceWith("engine"),
        level = DeprecationLevel.ERROR
    )
    public val jacocoEngineVersion: Property<String> = objects.property()

    @get:Internal
    @Deprecated(
        message = "Property was removed in Kover API version 2. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}",
        level = DeprecationLevel.ERROR
    )
    public var generateReportOnCheck: Boolean = true

    @get:Internal
    @Deprecated(
        message = "Property was removed in Kover API version 2. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}",
        level = DeprecationLevel.ERROR
    )
    public var disabledProjects: Set<String> = emptySet()

    @Deprecated(
        message = "Property was removed in Kover API version 2. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}",
        level = DeprecationLevel.ERROR
    )
    @get:Internal
    public var instrumentAndroidPackage: Boolean = false

    @Deprecated(
        message = "Property was removed in Kover API version 2. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}",
        level = DeprecationLevel.ERROR
    )
    @get:Internal
    public var runAllTestsForProjectTask: Boolean = false
}

// DEPRECATIONS
// TODO delete in 0.7 version
@Deprecated(
    message = "Class KoverExtension was renamed to KoverProjectConfig in Kover API version 2",
    replaceWith = ReplaceWith("KoverProjectConfig"),
    level = DeprecationLevel.ERROR
)
public open class KoverExtension

public open class KoverProjectFilters @Inject constructor(private val objects: ObjectFactory) {
    internal val classes: Property<KoverClassFilter> = objects.property()
    internal val annotations: Property<KoverAnnotationFilter> = objects.property()
    internal val sourceSets: Property<KoverSourceSetFilter> = objects.property()

    /**
     * Configures class filter in order to include and exclude specific classes.
     *
     * Example:
     *  ```
     *  classes {
     *      excludes += "com.example.FooBar"
     *      includes += "com.example.*Bar"
     *  }
     *  ```
     * Excludes have priority over includes.
     */
    public fun classes(config: Action<KoverClassFilter>) {
        val classFilter = objects.newInstance<KoverClassFilter>()
        config.execute(classFilter)
        classes.set(classFilter)
    }

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
    public fun annotations(config: Action<KoverAnnotationFilter>) {
        val annotationFilter = objects.newInstance<KoverAnnotationFilter>()
        config.execute(annotationFilter)
        annotations.set(annotationFilter)
    }

    /**
     * Configures source set filter.
     */
    public fun sourceSets(config: Action<KoverSourceSetFilter>) {
        val sourceSetFilters = objects.newInstance<KoverSourceSetFilter>()
        config.execute(sourceSetFilters)
        sourceSets.set(sourceSetFilters)
    }
}

public open class KoverProjectInstrumentation {
    /**
     * Specifies the names of test tasks for which instrumentation will be disabled.
     */
    public val excludeTasks: MutableSet<String> = mutableSetOf()
}

public open class KoverProjectXmlConfig @Inject constructor(objects: ObjectFactory) {
    internal val filters: KoverProjectFilters = objects.newInstance()

    /**
     * Specifies whether the XML report generation task should be executed before the `check` task (if it exists) of the current project.
     *
     * `false` by default.
     */
    public val onCheck: Property<Boolean> = objects.property()

    /**
     * Specifies file path of generated XML report file with coverage data.
     *
     * By default, is a value of [KoverPaths.PROJECT_XML_REPORT_DEFAULT_PATH] in the build directory.
     */
    public val reportFile: RegularFileProperty = objects.fileProperty()

    /**
     * Override filters for the XML report generation task.
     * Only the explicitly specified filters will be overridden, the rest will be inherited from the common filters (see [KoverProjectConfig.filters]).
     */
    public fun overrideFilters(config: Action<KoverProjectFilters>) {
        config.execute(filters)
    }
}

public open class KoverProjectHtmlConfig @Inject constructor(private val objects: ObjectFactory) {
    internal val taskFilters: KoverProjectFilters = objects.newInstance()

    /**
     * Specifies whether the HTML report generation task should be executed before the `check` task (if it exists) of the current project.
     *
     * `false` by default.
     */
    public val onCheck: Property<Boolean> = objects.property()

    /**
     * Specifies directory path of generated HTML report.
     *
     * By default, is a value of [KoverPaths.PROJECT_HTML_REPORT_DEFAULT_PATH] in the build directory.
     */
    public val reportDir: DirectoryProperty = objects.directoryProperty()

    /**
     * Override filters for the HTML report generation task.
     * Only the explicitly specified filters will be overrided, the rest will be inherited from the common filters (see [KoverProjectConfig.filters]).
     */
    public fun overrideFilters(config: Action<KoverProjectFilters>) {
        config.execute(taskFilters)
    }
}


public open class KoverMergedConfig @Inject constructor(objects: ObjectFactory) {
    internal val isEnabled: Property<Boolean> = objects.property()
    internal val filters: KoverMergedFilters = objects.newInstance()
    internal val xmlReport: KoverMergedXmlConfig = objects.newInstance()
    internal val htmlReport: KoverMergedHtmlConfig = objects.newInstance()
    internal val verify: KoverVerifyConfig = objects.newInstance()

    /**
     * Create Kover tasks for generating merged reports.
     */
    public fun enable() {
        isEnabled.set(true)
    }

    /**
     * Configures filters for all Kover merged tasks in current project.
     */
    public fun filters(config: Action<KoverMergedFilters>) {
        config.execute(filters)
    }

    /**
     * Configures the task of generating a merged XML report.
     */
    public fun xmlReport(config: Action<KoverMergedXmlConfig>) {
        config.execute(xmlReport)
    }

    /**
     * Configures the task of generating a merged HTML report.
     */
    public fun htmlReport(config: Action<KoverMergedHtmlConfig>) {
        config.execute(htmlReport)
    }

    /**
     * Configures the merged verification task.
     */
    public fun verify(config: Action<KoverVerifyConfig>) {
        config.execute(verify)
    }
}

public open class KoverMergedFilters @Inject constructor(private val objects: ObjectFactory) {
    internal val classes: Property<KoverClassFilter> = objects.property()
    internal val annotations: Property<KoverAnnotationFilter> = objects.property()
    internal val projects: Property<KoverProjectsFilter> = objects.property()

    /**
     * Configures class filter.
     */
    public fun classes(config: Action<KoverClassFilter>) {
        val classFilter = objects.newInstance<KoverClassFilter>()
        config.execute(classFilter)
        classes.set(classFilter)
    }

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
    public fun annotations(config: Action<KoverAnnotationFilter>) {
        val newAnnotationFilter = objects.newInstance<KoverAnnotationFilter>()
        config.execute(newAnnotationFilter)
        annotations.set(newAnnotationFilter)
    }

    /**
     * Configures projects filter.
     */
    public fun projects(config: Action<KoverProjectsFilter>) {
        val projectsFilters = objects.newInstance<KoverProjectsFilter>()
        config.execute(projectsFilters)
        projects.set(projectsFilters)
    }
}


public open class KoverMergedXmlConfig @Inject constructor(private val objects: ObjectFactory) {
    internal val classFilter: Property<KoverClassFilter> = objects.property()
    internal val annotationFilter: Property<KoverAnnotationFilter> = objects.property()

    /**
     * Specifies whether the merged XML report generation task should be executed before the `check` task (if it exists) of the current project.
     */
    public val onCheck: Property<Boolean> = objects.property()

    /**
     * Specifies file path of generated XML report file with coverage data.
     */
    public val reportFile: RegularFileProperty = objects.fileProperty()

    /**
     * Override class filter for the merged XML report generation task.
     */
    public fun overrideClassFilter(config: Action<KoverClassFilter>) {
        val newClassFilter = objects.newInstance<KoverClassFilter>()
        config.execute(newClassFilter)
        classFilter.set(newClassFilter)
    }

    /**
     * Override annotation filter for the merged XML report generation task.
     *
     * To filter the merged XML report, only the annotations specified in this block will be used, the common annotation filter will be removed.
     *
     * Example:
     *  ```
     *  overrideAnnotationFilter {
     *      excludes += "com.example.Generated"
     *  }
     *  ```
     */
    public fun overrideAnnotationFilter(config: Action<KoverAnnotationFilter>) {
        val newAnnotationFilter = objects.newInstance<KoverAnnotationFilter>()
        config.execute(newAnnotationFilter)
        annotationFilter.set(newAnnotationFilter)
    }
}

public open class KoverMergedHtmlConfig @Inject constructor(private val objects: ObjectFactory) {
    internal val classFilter: Property<KoverClassFilter> = objects.property()
    internal val annotationFilter: Property<KoverAnnotationFilter> = objects.property()

    /**
     * Specifies whether the merged HTML report generation task should be executed before the `check` task (if it exists) of the current project.
     */
    public val onCheck: Property<Boolean> = objects.property()

    /**
     * Specifies directory path of generated HTML report.
     */
    public val reportDir: DirectoryProperty = objects.directoryProperty()

    /**
     * Override class filter for the merged HTML report generation task.
     */
    public fun overrideClassFilter(config: Action<KoverClassFilter>) {
        val newClassFilter = objects.newInstance<KoverClassFilter>()
        config.execute(newClassFilter)
        classFilter.set(newClassFilter)
    }

    /**
     * Override annotation filter for the merged HTML report generation task.
     *
     * To filter the merged HTML report, only the annotations specified in this block will be used, the common annotation filter will be removed.
     *
     * Example:
     *  ```
     *  overrideAnnotationFilter {
     *      excludes += "com.example.Generated"
     *  }
     *  ```
     */
    public fun overrideAnnotationFilter(config: Action<KoverAnnotationFilter>) {
        val newAnnotationFilter = objects.newInstance<KoverAnnotationFilter>()
        config.execute(newAnnotationFilter)
        annotationFilter.set(newAnnotationFilter)
    }
}

public open class KoverProjectsFilter {
    /**
     * Specifies the projects excluded from subprojects (included current project) using in the merged tasks. Both the project name (if it is unique) and the project path can be used.
     *
     * If empty, the current project and all subprojects are used.
     */
    @get:Input
    public val excludes: MutableList<String> = mutableListOf()
}


public open class KoverVerifyConfig @Inject constructor(private val objects: ObjectFactory) {
    internal val rules: ListProperty<VerificationRule> = objects.listProperty()

    /**
     * Specifies whether the verification task should be executed before the `check` task (if it exists) of the current project.
     *
     * By default, `true` for project reports and `false` for merged.
     */
    public val onCheck: Property<Boolean> = objects.property<Boolean>().value(true)

    /**
     * Add new coverage verification rule to check after test task execution.
     */
    public fun rule(configureRule: Action<VerificationRule>) {
        rules.add(objects.newInstance<VerificationRule>().also { configureRule.execute(it) })
    }
}

public open class KoverClassFilter {
    /**
     * Specifies class inclusion rules into report.
     * Only the specified classes may be present in the report.
     * Exclusion rules have priority over inclusion ones.
     *
     * Inclusion rules are represented as a set of fully-qualified names of the classes being instrumented.
     * It's possible to use `*` and `?` wildcards.
     */
    @get:Input
    public val includes: MutableList<String> = mutableListOf()

    /**
     * Specifies class exclusion rules into report.
     * The specified classes will definitely be missing from report.
     * Exclusion rules have priority over inclusion ones.
     *
     * Exclusion rules are represented as a set of fully-qualified names of the classes being instrumented.
     * It's possible to use `*` and `?` wildcards.
     */
    @get:Input
    public val excludes: MutableList<String> = mutableListOf()
}

public open class KoverAnnotationFilter {

    /**
     * Specifies annotations that should be used to mark declarations that should be excluded from reports
     *
     * Elements are represented as a set of fully-qualified names of the annotations.
     * It's possible to use `*` and `?` wildcards.
     */
    @get:Input
    public val excludes: MutableList<String> = mutableListOf()
}

public open class KoverSourceSetFilter {
    /**
     * Not implemented in beta version.
     */
    @get:Input
    public val excludes: MutableSet<String> = mutableSetOf()

    /**
     * Not implemented in beta version.
     */
    @get:Input
    public var excludeTests: Boolean = true
}


public open class VerificationRule @Inject constructor(private val objects: ObjectFactory) {
    @get:Nested
    @get:Optional
    internal val classFilter: Property<KoverClassFilter> = objects.property()

    @get:Nested
    @get:Optional
    internal val annotationFilter: Property<KoverAnnotationFilter> = objects.property()

    @get:Nested
    internal val bounds: ListProperty<VerificationBound> = objects.listProperty()

    /**
     * Specifies that the rule will be checked during verification.
     */
    @get:Input
    public var isEnabled: Boolean = true

    /**
     * Specifies custom name of the rule.
     */
    @get:Input
    @get:Nullable
    @get:Optional
    public var name: String? = null

    /**
     * Specifies by which entity the code for separate coverage evaluation will be grouped.
     */
    @get:Input
    public var target: VerificationTarget = VerificationTarget.ALL

    /**
     * Override class filter for the rule.
     */
    public fun overrideClassFilter(config: Action<KoverClassFilter>) {
        if (!classFilter.isPresent) {
            classFilter.set(objects.newInstance<KoverClassFilter>())
        }
        config.execute(classFilter.get())
    }

    /**
     * Override annotation filter for the verification rule.
     *
     * To filter the verification rule, only the annotations specified in this block will be used, the common annotation filter will be removed.
     *
     * Example:
     *  ```
     *  overrideAnnotationFilter {
     *      excludes += "com.example.Generated"
     *  }
     *  ```
     */
    public fun overrideAnnotationFilter(config: Action<KoverAnnotationFilter>) {
        val newAnnotationFilter = objects.newInstance<KoverAnnotationFilter>()
        config.execute(newAnnotationFilter)
        annotationFilter.set(newAnnotationFilter)
    }

    /**
     * Add a constraint on the value of the code coverage metric.
     */
    public fun bound(configureBound: Action<VerificationBound>) {
        bounds.add(objects.newInstance<VerificationBound>().also { configureBound.execute(it) })
    }
}

public open class VerificationBound {
    /**
     * Specifies minimal value to compare with counter value.
     */
    @get:Input
    @get:Nullable
    @get:Optional
    public var minValue: Int? = null

    /**
     * Specifies maximal value to compare with counter value.
     */
    @get:Input
    @get:Nullable
    @get:Optional
    public var maxValue: Int? = null

    /**
     * Specifies which metric will be evaluation code coverage.
     */
    @get:Input
    public var counter: CounterType = CounterType.LINE

    /**
     * Specifies type of lines counter value to compare with minimal and maximal values if them defined.
     * Default is [VerificationValueType.COVERED_PERCENTAGE]
     */
    @get:Input
    public var valueType: VerificationValueType = VerificationValueType.COVERED_PERCENTAGE
}

/**
 *  Entity type for grouping code to coverage evaluation.
 */
public enum class VerificationTarget {
    /**
     * Counts the coverage for all code.
     */
    ALL,

    /**
     * Counts the coverage for each class separately.
     */
    CLASS,

    /**
     * Counts the coverage for each package that has classes separately.
     */
    PACKAGE
}

/**
 * Type of the metric to evaluate code coverage.
 */
public enum class CounterType {
    /**
     * Evaluates coverage for lines.
     */
    LINE,

    /**
     * Evaluates coverage for JVM bytecode instructions.
     */
    INSTRUCTION,

    /**
     * Evaluates coverage for code branches excluded dead-branches.
     */
    BRANCH
}


/**
 * Type of counter value to compare with minimal and maximal values if them defined.
 */
public enum class VerificationValueType {
    COVERED_COUNT,
    MISSED_COUNT,
    COVERED_PERCENTAGE,
    MISSED_PERCENTAGE
}
