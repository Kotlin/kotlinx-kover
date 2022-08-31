/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.configurator

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.framework.checker.CheckerContext
import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.common.BuildSlice
import kotlinx.kover.test.functional.framework.starter.*
import kotlinx.kover.test.functional.framework.starter.SlicedBuildConfigurator
import org.gradle.api.*
import java.io.*


internal interface BuildConfigurator {
    fun addKoverProject(path: String = ":", name: String = path.substringAfterLast(":"), generator: ProjectConfigurator.() -> Unit)

    fun addProject(path: String, name: String, generator: ProjectConfigurator.() -> Unit)

    fun run(vararg args: String, checker: CheckerContext.() -> Unit = {})

    fun runWithError(vararg args: String, errorChecker: CheckerContext.() -> Unit = {})

    fun useLocalCache(use: Boolean = true)

    fun prepare(): TestBuildConfig
}

internal interface ProjectConfigurator {
    fun plugins(block: PluginsConfigurator.() -> Unit)

    fun repositories(block: RepositoriesConfigurator.() -> Unit)

    fun kover(config: KoverConfigurator.() -> Unit)

    fun koverMerged(config: KoverMergedConfigurator.() -> Unit)

    fun testTasks(block: TestTaskConfigurator.() -> Unit)

    fun sourcesFrom(template: String)

    fun dependencyOnProject(path: String)
}

/**
 * Same as [KoverProjectConfig]
 */
internal interface KoverConfigurator {
    var isDisabled: Boolean?

    var engine: CoverageEngineVariant?

    fun filters(config: KoverFiltersConfigurator.() -> Unit)

    fun instrumentation(config: KoverProjectInstrumentation.() -> Unit)

    fun xmlReport(config: KoverXmlConfigurator.() -> Unit)

    fun htmlReport(config: KoverHtmlConfigurator.() -> Unit)

    fun verify(config: KoverVerifyConfigurator.() -> Unit)
}

internal interface KoverXmlConfigurator {
    var onCheck: Boolean?
    var reportFile: File?

    fun overrideFilters(config: KoverFiltersConfigurator.() -> Unit)
}

internal interface KoverHtmlConfigurator {
    var onCheck: Boolean?
    var reportDir: File?

    fun overrideFilters(config: KoverFiltersConfigurator.() -> Unit)
}

internal interface KoverVerifyConfigurator {
    val onCheck: Boolean?

    fun rule(config: VerificationRuleConfigurator.() -> Unit)
}

internal class VerificationRuleConfigurator {
    var isEnabled: Boolean? = null
    var name: String? = null
    var target: VerificationTarget? = null

    var overrideClassFilter: KoverClassFilter? = null
    val bounds: MutableList<VerificationBoundConfigurator> = mutableListOf()

    fun overrideClassFilter(config: Action<KoverClassFilter>) {
        overrideClassFilter = KoverClassFilter().also { config.execute(it) }
    }

    fun bound(configureBound: VerificationBoundConfigurator.() -> Unit) {
        bounds += VerificationBoundConfigurator().also(configureBound)
    }
}

internal class VerificationBoundConfigurator {
    var minValue: Int? = null
    var maxValue: Int? = null
    var counter: CounterType? = null
    var valueType: VerificationValueType? = null
}

internal interface KoverFiltersConfigurator {
    fun classes(config: KoverClassFilter.() -> Unit)

    fun sourceSets(config: KoverSourceSetFilter.() -> Unit)
}

interface TestTaskConfigurator {
    fun excludes(vararg classes: String)
    fun includes(vararg classes: String)
}

internal interface KoverMergedConfigurator {
    public fun enable()

    public fun filters(config: KoverMergedFiltersConfigurator.() -> Unit)

    public fun verify(config: KoverVerifyConfigurator.() -> Unit)
}

public interface KoverMergedFiltersConfigurator {
    public fun classes(config: KoverClassFilter.() -> Unit)

    public fun projects(config: KoverProjectsFilter.() -> Unit)
}

interface PluginsConfigurator {
    fun kotlin(version: String? = null)
    fun kover(version: String? = null)
}

interface RepositoriesConfigurator {
    fun repository(name: String)
}


internal abstract class BuilderConfiguratorWrapper(private val origin: BuildConfigurator) : BuildConfigurator {

    override fun addKoverProject(path: String, name: String, generator: ProjectConfigurator.() -> Unit) {
        origin.addKoverProject(path, name, generator)
    }

    override fun addProject(path: String, name: String, generator: ProjectConfigurator.() -> Unit) {
        origin.addProject(path, name, generator)
    }

    override fun run(vararg args: String, checker: CheckerContext.() -> Unit) {
        origin.run(*args) { checker() }
    }

    override fun runWithError(vararg args: String, errorChecker: CheckerContext.() -> Unit) {
        origin.runWithError(*args) { errorChecker() }
    }

    override fun useLocalCache(use: Boolean) {
        origin.useLocalCache(use)
    }

    override fun prepare(): TestBuildConfig {
        return origin.prepare()
    }
}
