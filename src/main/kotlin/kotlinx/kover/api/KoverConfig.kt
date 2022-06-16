/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("RedundantVisibilityModifier")

package kotlinx.kover.api

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.model.*
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import javax.annotation.*
import javax.inject.*

public open class KoverProjectConfig @Inject constructor(objects: ObjectFactory) {
    internal val filters: KoverProjectFilters = objects.newInstance(KoverProjectFilters::class.java, objects)

    internal val instrumentation: KoverProjectInstrumentation =
        objects.newInstance(KoverProjectInstrumentation::class.java)

    internal val xmlReport: KoverProjectXmlConfig = objects.newInstance(KoverProjectXmlConfig::class.java, objects)

    internal val htmlReport: KoverProjectHtmlConfig = objects.newInstance(KoverProjectHtmlConfig::class.java, objects)

    internal val verify: KoverVerifyConfig = objects.newInstance(KoverVerifyConfig::class.java, objects)

    /**
     * Specifies whether instrumentation is disabled for all test tasks of current project.
     */
    public val isDisabled: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Specifies the coverage engine variant to be used to collect execution data.
     */
    public val engine: Property<CoverageEngineVariant> = objects.property(CoverageEngineVariant::class.java)

    /**
     * Configures filters for all Kover tasks of current project.
     */
    public fun filters(config: Action<KoverProjectFilters>) {
        config.execute(filters)
    }

    /**
     * Configures instrumentation of the test tasks of current project.
     */
    public fun instrumentation(config: Action<KoverProjectInstrumentation>) {
        config.execute(instrumentation)
    }

    /**
     * Configures the task of generating an XML report.
     */
    public fun xmlReport(config: Action<KoverProjectXmlConfig>) {
        config.execute(xmlReport)
    }

    /**
     * Configures the task of generating an HTML report.
     */
    public fun htmlReport(config: Action<KoverProjectHtmlConfig>) {
        config.execute(htmlReport)
    }

    /**
     * Configures the verification task.
     */
    public fun verify(config: Action<KoverVerifyConfig>) {
        config.execute(verify)
    }
}

public open class KoverProjectFilters @Inject constructor(private val objects: ObjectFactory) {
    internal val classes: Property<KoverClassFilter> = objects.property(KoverClassFilter::class.java)

    internal val sourceSets: Property<KoverSourceSetFilter> = objects.property(KoverSourceSetFilter::class.java)

    /**
     * Configures class filter.
     */
    public fun classes(config: Action<KoverClassFilter>) {
        val classFilter = objects.newInstance(KoverClassFilter::class.java)
        config.execute(classFilter)
        classes.set(classFilter)
    }

    /**
     * Configures source set filter.
     */
    public fun sourcesets(config: Action<KoverSourceSetFilter>) {
        val sourceSetFilters = objects.newInstance(KoverSourceSetFilter::class.java)
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
    internal val filters: KoverProjectFilters = objects.newInstance(KoverProjectFilters::class.java, objects)

    /**
     * Specifies whether the XML report generation task should be executed before the `check` task.
     */
    public val onCheck: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Specifies file path of generated XML report file with coverage data.
     */
    public val reportFile: RegularFileProperty = objects.fileProperty()

    /**
     * Override filters for the XML report generation task.
     * Only the explicitly specified filters will be overrided, the rest will be inherited from the common filters (see [KoverProjectConfig.filters]).
     */
    public fun overrideFilters(config: Action<KoverProjectFilters>) {
        config.execute(filters)
    }
}

public open class KoverProjectHtmlConfig @Inject constructor(private val objects: ObjectFactory) {
    internal val taskFilters: KoverProjectFilters = objects.newInstance(KoverProjectFilters::class.java, objects)

    /**
     * Specifies whether the HTML report generation task should be executed before the `check` task.
     */
    public val onCheck: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Specifies directory path of generated HTML report.
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
    internal var isEnabled: Property<Boolean> = objects.property(Boolean::class.java)
    internal val filters: KoverMergedFilters = objects.newInstance(KoverMergedFilters::class.java, objects)
    internal val xmlReport: KoverMergedXmlConfig = objects.newInstance(KoverMergedXmlConfig::class.java, objects)
    internal val htmlReport: KoverMergedHtmlConfig = objects.newInstance(KoverMergedHtmlConfig::class.java, objects)
    internal val verify: KoverVerifyConfig = objects.newInstance(KoverVerifyConfig::class.java, objects)

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
    internal val classes: Property<KoverClassFilter> = objects.property(KoverClassFilter::class.java)

    internal val projects: Property<KoverProjectsFilter> = objects.property(KoverProjectsFilter::class.java)

    /**
     * Configures class filter.
     */
    public fun classes(config: Action<KoverClassFilter>) {
        val classFilter = objects.newInstance(KoverClassFilter::class.java)
        config.execute(classFilter)
        classes.set(classFilter)
    }

    /**
     * Configures projects filter.
     */
    public fun projects(config: Action<KoverProjectsFilter>) {
        val projectsFilters = objects.newInstance(KoverProjectsFilter::class.java)
        config.execute(projectsFilters)
        projects.set(projectsFilters)
    }
}


public open class KoverMergedXmlConfig @Inject constructor(private val objects: ObjectFactory) {
    internal val classFilter: Property<KoverClassFilter> = objects.property(KoverClassFilter::class.java)

    /**
     * Specifies whether the merged XML report generation task should be executed before the `check` task.
     */
    public val onCheck: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Specifies file path of generated XML report file with coverage data.
     */
    public val reportFile: RegularFileProperty = objects.fileProperty()

    /**
     * Override class filter for the merged XML report generation task.
     */
    public fun overrideClassFilter(config: Action<KoverClassFilter>) {
        val newClassFilter = objects.newInstance(KoverClassFilter::class.java)
        config.execute(newClassFilter)
        classFilter.set(newClassFilter)
    }
}

public open class KoverMergedHtmlConfig @Inject constructor(private val objects: ObjectFactory) {
    internal val classFilter: Property<KoverClassFilter> = objects.property(KoverClassFilter::class.java)

    /**
     * Specifies whether the merged HTML report generation task should be executed before the `check` task.
     */
    public val onCheck: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Specifies directory path of generated HTML report.
     */
    public val reportDir: DirectoryProperty = objects.directoryProperty()

    /**
     * Override class filter for the merged HTML report generation task.
     */
    public fun overrideClassFilter(config: Action<KoverClassFilter>) {
        val newClassFilter = objects.newInstance(KoverClassFilter::class.java)
        config.execute(newClassFilter)
        classFilter.set(newClassFilter)
    }
}

public open class KoverProjectsFilter {
    /**
     * Specifies the projects involved in the merged tasks. Both the project name (if it is unique) and the project path can be used.
     *
     * If empty, the current project and all subprojects are used.
     */
    @get:Input
    public val includes: MutableList<String> = mutableListOf()
}


public open class KoverVerifyConfig @Inject constructor(private val objects: ObjectFactory) {
    internal val rules: ListProperty<VerificationRule> = objects.listProperty(VerificationRule::class.java)

    /**
     * Specifies whether the verification task should be executed before the `check` task.
     */
    public val onCheck: Property<Boolean> = objects.property(Boolean::class.java).value(true)

    /**
     * Add new coverage verification rule to check after test task execution.
     */
    public fun rule(configureRule: Action<VerificationRule>) {
        rules.add(objects.newInstance(VerificationRule::class.java, objects).also { configureRule.execute(it) })
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

public open class KoverSourceSetFilter {
    /**
     * TBD
     */
    @get:Input
    public val excludes: MutableSet<String> = mutableSetOf()

    /**
     * TBD
     */
    @get:Input
    public var excludeTests: Boolean = true
}


public open class VerificationRule @Inject constructor(private val objects: ObjectFactory) {
    @get:Nested
    @get:Optional
    internal val classFilter: Property<KoverClassFilter> = objects.property(KoverClassFilter::class.java)

    @get:Nested
    internal val bounds: ListProperty<VerificationBound> = objects.listProperty(VerificationBound::class.java)

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
            classFilter.set(objects.newInstance(KoverClassFilter::class.java))
        }
        config.execute(classFilter.get())
    }

    /**
     * Add a constraint on the value of the code coverage metric.
     */
    public fun bound(configureBound: Action<VerificationBound>) {
        bounds.add(objects.newInstance(VerificationBound::class.java).also { configureBound.execute(it) })
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
     * Count the coverage for all code.
     */
    ALL,

    /**
     * Count the coverage for each class separately.
     */
    CLASS,

    /**
     * Count the coverage for each package that has classes separately.
     */
    PACKAGE
}

/**
 * Type of the metric to evaluate code coverage.
 */
public enum class CounterType {
    /**
     * Evaluate coverage for lines.
     */
    LINE,

    /**
     * Evaluate coverage for JVM bytecode instructions.
     */
    INSTRUCTION,

    /**
     * Evaluate coverage for code branches.
     */
    BRANCH
}


/**
 * Type of lines counter value to compare with minimal and maximal values if them defined.
 */
public enum class VerificationValueType {
    COVERED_COUNT,
    MISSED_COUNT,
    COVERED_PERCENTAGE,
    MISSED_PERCENTAGE
}
