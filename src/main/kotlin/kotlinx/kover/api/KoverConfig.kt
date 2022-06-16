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
    public val isDisabled: Property<Boolean> = objects.property(Boolean::class.java)

    public val engine: Property<CoverageEngineVariant> = objects.property(CoverageEngineVariant::class.java)

    internal val filters: KoverProjectFilters = objects.newInstance(KoverProjectFilters::class.java, objects)

    internal val instrumentation: KoverProjectInstrumentation =
        objects.newInstance(KoverProjectInstrumentation::class.java)

    internal val xmlReport: KoverProjectXmlConfig = objects.newInstance(KoverProjectXmlConfig::class.java, objects)

    internal val htmlReport: KoverProjectHtmlConfig = objects.newInstance(KoverProjectHtmlConfig::class.java, objects)

    internal val verify: KoverVerifyConfig = objects.newInstance(KoverVerifyConfig::class.java, objects)

    public fun filters(config: Action<KoverProjectFilters>) {
        config.execute(filters)
    }

    public fun instrumentation(config: Action<KoverProjectInstrumentation>) {
        config.execute(instrumentation)
    }

    public fun xmlReport(config: Action<KoverProjectXmlConfig>) {
        config.execute(xmlReport)
    }

    public fun htmlReport(config: Action<KoverProjectHtmlConfig>) {
        config.execute(htmlReport)
    }

    public fun verify(config: Action<KoverVerifyConfig>) {
        config.execute(verify)
    }
}

public open class KoverProjectFilters @Inject constructor(private val objects: ObjectFactory) {
    internal val classes: Property<KoverClassFilters> = objects.property(KoverClassFilters::class.java)
        //it's ok to create an instance by a constructor because this object won't be used in Gradle
        .value(KoverClassFilters())

    internal val sourceSets: Property<KoverSourceSetFilters> = objects.property(KoverSourceSetFilters::class.java)
        //it's ok to create an instance by a constructor because this object won't be used in Gradle
        .value(KoverSourceSetFilters())

    public fun classes(config: Action<KoverClassFilters>) {
        val classFilters = objects.newInstance(KoverClassFilters::class.java)
        config.execute(classFilters)
        classes.set(classFilters)
    }

    public fun sourcesets(config: Action<KoverSourceSetFilters>) {
        val sourceSetFilters = objects.newInstance(KoverSourceSetFilters::class.java)
        config.execute(sourceSetFilters)
        sourceSets.set(sourceSetFilters)
    }
}

public open class KoverProjectInstrumentation {
    public val excludeTasks: MutableSet<String> = mutableSetOf()
}

public open class KoverProjectXmlConfig @Inject constructor(objects: ObjectFactory) {
    public val onCheck: Property<Boolean> = objects.property(Boolean::class.java)
    public val reportFile: RegularFileProperty = objects.fileProperty()
    internal val taskFilters: KoverProjectFilters = objects.newInstance(KoverProjectFilters::class.java, objects)

    public fun overrideFilters(config: Action<KoverProjectFilters>) {
        config.execute(taskFilters)
    }
}

public open class KoverProjectHtmlConfig @Inject constructor(private val objects: ObjectFactory) {
    public val onCheck: Property<Boolean> = objects.property(Boolean::class.java)

    public val reportDir: DirectoryProperty = objects.directoryProperty()
    internal val taskFilters: KoverProjectFilters = objects.newInstance(KoverProjectFilters::class.java, objects)

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

    public fun enable() {
        isEnabled.set(true)
    }

    public fun filters(config: Action<KoverMergedFilters>) {
        config.execute(filters)
    }

    public fun xmlReport(config: Action<KoverMergedXmlConfig>) {
        config.execute(xmlReport)
    }

    public fun htmlReport(config: Action<KoverMergedHtmlConfig>) {
        config.execute(htmlReport)
    }

    public fun verify(config: Action<KoverVerifyConfig>) {
        config.execute(verify)
    }
}

public open class KoverMergedFilters @Inject constructor(private val objects: ObjectFactory) {
    internal val classes: Property<KoverClassFilters> =
        objects.property(KoverClassFilters::class.java).value(KoverClassFilters())

    internal val projects: Property<KoverProjectsFilters> =
        objects.property(KoverProjectsFilters::class.java).value(KoverProjectsFilters())

    public fun classes(config: Action<KoverClassFilters>) {
        val classFilters = objects.newInstance(KoverClassFilters::class.java)
        config.execute(classFilters)
        classes.set(classFilters)
    }

    public fun projects(config: Action<KoverProjectsFilters>) {
        val projectsFilters = objects.newInstance(KoverProjectsFilters::class.java)
        config.execute(projectsFilters)
        projects.set(projectsFilters)
    }
}


public open class KoverMergedXmlConfig @Inject constructor(private val objects: ObjectFactory) {
    public val onCheck: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Specifies file path of generated XML report file with coverage data.
     */
    public val reportFile: RegularFileProperty = objects.fileProperty()

    internal val classes: Property<KoverClassFilters> = objects.property(KoverClassFilters::class.java)

    public fun overrideClassFilters(config: Action<KoverClassFilters>) {
        val classFilters = objects.newInstance(KoverClassFilters::class.java)
        config.execute(classFilters)
        classes.set(classFilters)
    }
}

public open class KoverMergedHtmlConfig @Inject constructor(private val objects: ObjectFactory) {

    public val onCheck: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Specifies directory path of generated HTML report.
     */
    public val reportDir: DirectoryProperty = objects.directoryProperty()

    internal val classes: Property<KoverClassFilters> = objects.property(KoverClassFilters::class.java)

    public fun overrideClassFilters(config: Action<KoverClassFilters>) {
        val classFilters = objects.newInstance(KoverClassFilters::class.java)
        config.execute(classFilters)
        classes.set(classFilters)
    }
}

public open class KoverProjectsFilters {
    @get:Input
    public val includes: MutableList<String> = mutableListOf()
}


public open class KoverVerifyConfig @Inject constructor(private val objects: ObjectFactory) {
    public val onCheck: Property<Boolean> = objects.property(Boolean::class.java).value(true)

    internal val rules: ListProperty<VerificationRule> = objects.listProperty(VerificationRule::class.java)

    public fun rule(configureRule: Action<VerificationRule>) {
        rules.add(objects.newInstance(VerificationRule::class.java, objects).also { configureRule.execute(it) })
    }
}

public open class KoverSourceSetFilters {
    @get:Input
    public val excludes: MutableSet<String> = mutableSetOf()

    @get:Input
    public var excludeTests: Boolean = true
}

public open class KoverClassFilters {
    @get:Input
    public val includes: MutableList<String> = mutableListOf()

    @get:Input
    public val excludes: MutableList<String> = mutableListOf()
}

public open class VerificationRule @Inject constructor(private val objects: ObjectFactory) {
    @get:Input
    public var isEnabled: Boolean = true

    @get:Input
    @get:Nullable
    @get:Optional
    public var name: String? = null

    @get:Input
    public var target: VerificationTarget = VerificationTarget.ALL

    /**
     * Absent default value to indicate that class filters are not overridden for the rule.
     */
    @get:Nested
    @get:Optional
    internal val filters: Property<KoverClassFilters> = objects.property(KoverClassFilters::class.java)

    @get:Nested
    internal val bounds: ListProperty<VerificationBound> = objects.listProperty(VerificationBound::class.java)

    public fun overrideClassFilters(config: Action<KoverClassFilters>) {
        if (!filters.isPresent) {
            filters.set(objects.newInstance(KoverClassFilters::class.java))
        }
        config.execute(filters.get())
    }

    public fun bound(configureBound: Action<VerificationBound>) {
        bounds.add(objects.newInstance(VerificationBound::class.java).also { configureBound.execute(it) })
    }
}

public open class VerificationBound {
    /**
     * Minimal value to compare with counter value.
     */
    @get:Input
    @get:Nullable
    @get:Optional
    public var minValue: Int? = null

    /**
     * Maximal value to compare with counter value.
     */
    @get:Input
    @get:Nullable
    @get:Optional
    public var maxValue: Int? = null

    /**
     * TODO
     */
    @get:Input
    public var counter: CounterType = CounterType.LINE

    /**
     * Type of lines counter value to compare with minimal and maximal values if them defined.
     * Default is [VerificationValueType.COVERED_PERCENTAGE]
     */
    @get:Input
    public var valueType: VerificationValueType = VerificationValueType.COVERED_PERCENTAGE
}

/**
 * TODO
 */
public enum class VerificationTarget {
    /**
     * TODO
     */
    ALL,

    /**
     * TODO
     */
    CLASS,

    /**
     * TODO
     */
    PACKAGE
}

/**
 * TODO
 */
public enum class CounterType {
    LINE,
    INSTRUCTION,
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
