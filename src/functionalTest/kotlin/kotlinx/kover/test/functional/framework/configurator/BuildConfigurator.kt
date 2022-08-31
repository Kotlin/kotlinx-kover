/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.configurator

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.common.kotlinVersion
import kotlinx.kover.test.functional.framework.checker.CheckerContext
import java.io.*

internal fun createConfigurator(): BuildConfigurator {
    return BuildConfiguratorImpl()
}

private open class BuildConfiguratorImpl : BuildConfigurator {
    private val projects: MutableMap<String, ProjectConfiguratorImpl> = mutableMapOf()
    private val runs: MutableList<TestRunConfig> = mutableListOf()
    private var useCache: Boolean = false

    override fun addProject(path: String, name: String, generator: ProjectConfigurator.() -> Unit) {
        projects[path] = ProjectConfiguratorImpl().also(generator)
    }

    override fun addKoverProject(path: String, name: String, generator: ProjectConfigurator.() -> Unit) {
        addProject(path, name) {
            plugins {
                if (path == ":") {
                    kotlin(kotlinVersion)
                    kover(koverVersion)
                } else {
                    kotlin()
                    kover()
                }
            }

            repositories {
                repository("mavenCentral()")
            }

            generator()
        }
    }

    override fun run(vararg args: String, checker: CheckerContext.() -> Unit) {
        runs += TestRunConfig(listOf(*args), checker)
    }

    override fun runWithError(vararg args: String, errorChecker: CheckerContext.() -> Unit) {
        runs += TestRunConfig(listOf(*args), errorChecker, true)
    }

    override fun useLocalCache(use: Boolean) {
        useCache = use
    }

    override fun prepare(): TestBuildConfig {
        return TestBuildConfig(projects, runs, useCache)
    }

}


private class ProjectConfiguratorImpl(private val name: String? = null) : ProjectConfigurator, TestProjectConfig {
    override val sourceTemplates: MutableSet<String> = mutableSetOf()
    override val plugins: PluginsConfiguratorImpl = PluginsConfiguratorImpl()
    override var kover: KoverConfiguratorImpl? = null
    override var merged: KoverMergedConfiguratorImpl? = null
    override val testTasks: TestTaskConfiguratorImpl = TestTaskConfiguratorImpl()
    override val projectDependencies: MutableList<String> = mutableListOf()

    private val repositoriesConfigurator: RepositoriesConfiguratorConfiguratorImpl =
        RepositoriesConfiguratorConfiguratorImpl()
    override val repositories: MutableList<String>
        get() = repositoriesConfigurator.repositories

    override fun plugins(block: PluginsConfigurator.() -> Unit) {
        plugins.also(block)
    }

    override fun repositories(block: RepositoriesConfigurator.() -> Unit) {
        repositoriesConfigurator.also(block)
    }

    override fun kover(config: KoverConfigurator.() -> Unit) {
        kover = KoverConfiguratorImpl().also(config)
    }

    override fun koverMerged(config: KoverMergedConfigurator.() -> Unit) {
        merged = KoverMergedConfiguratorImpl().also(config)
    }

    override fun testTasks(block: TestTaskConfigurator.() -> Unit) {
        testTasks.also(block)
    }

    override fun sourcesFrom(template: String) {
        sourceTemplates += template
    }

    override fun dependencyOnProject(path: String) {
        projectDependencies += path
    }
}


private class KoverConfiguratorImpl : KoverConfigurator, TestKoverConfig {
    override var isDisabled: Boolean? = null
    override var engine: CoverageEngineVariant? = null
    override val filters: KoverFiltersConfiguratorImpl = KoverFiltersConfiguratorImpl()
    override val instrumentation: KoverProjectInstrumentation = KoverProjectInstrumentation()
    override val xml: KoverXmlConfiguratorImpl = KoverXmlConfiguratorImpl()
    override val html: KoverHtmlConfiguratorImpl = KoverHtmlConfiguratorImpl()
    override val verify: TestVerifyConfiguratorImpl = TestVerifyConfiguratorImpl()

    override fun filters(config: KoverFiltersConfigurator.() -> Unit) {
        filters.also(config)
    }

    override fun instrumentation(config: KoverProjectInstrumentation.() -> Unit) {
        instrumentation.also(config)
    }

    override fun xmlReport(config: KoverXmlConfigurator.() -> Unit) {
        xml.also(config)
    }

    override fun htmlReport(config: KoverHtmlConfigurator.() -> Unit) {
        html.also(config)
    }

    override fun verify(config: KoverVerifyConfigurator.() -> Unit) {
        verify.also(config)
    }
}

private class KoverFiltersConfiguratorImpl : KoverFiltersConfigurator, TestKoverFiltersConfig {
    override var classes: KoverClassFilter? = null
    override var sourceSets: KoverSourceSetFilter? = null

    override fun classes(config: KoverClassFilter.() -> Unit) {
        classes = KoverClassFilter().also(config)
    }

    override fun sourceSets(config: KoverSourceSetFilter.() -> Unit) {
        sourceSets = KoverSourceSetFilter().also(config)
    }
}

private class KoverXmlConfiguratorImpl : KoverXmlConfigurator, TestXmlConfig {
    override var onCheck: Boolean? = null
    override var reportFile: File? = null
    override var overrideFilters: KoverFiltersConfiguratorImpl? = null

    override fun overrideFilters(config: KoverFiltersConfigurator.() -> Unit) {
        if (overrideFilters == null) {
            overrideFilters = KoverFiltersConfiguratorImpl()
        }
        overrideFilters!!.also(config)
    }
}

private class KoverHtmlConfiguratorImpl : KoverHtmlConfigurator, TestHtmlConfig {
    override var onCheck: Boolean? = null
    override var reportDir: File? = null
    override var overrideFilters: KoverFiltersConfiguratorImpl? = null

    override fun overrideFilters(config: KoverFiltersConfigurator.() -> Unit) {
        if (overrideFilters == null) {
            overrideFilters = KoverFiltersConfiguratorImpl()
        }
        overrideFilters!!.also(config)
    }
}

private class TestVerifyConfiguratorImpl : KoverVerifyConfigurator, TestVerifyConfig {
    override val onCheck: Boolean? = null
    override val rules: MutableList<VerificationRuleConfigurator> = mutableListOf()

    override fun rule(config: VerificationRuleConfigurator.() -> Unit) {
        rules += VerificationRuleConfigurator().also(config)
    }
}

private class RepositoriesConfiguratorConfiguratorImpl : RepositoriesConfigurator {
    val repositories: MutableList<String> = mutableListOf()
    override fun repository(name: String) {
        repositories += name
    }
}

private class KoverMergedConfiguratorImpl : KoverMergedConfigurator, TestKoverMergedConfig {
    override var enabled: Boolean = false
    override val filters: KoverMergedFiltersConfiguratorImpl = KoverMergedFiltersConfiguratorImpl()
    override val verify: TestVerifyConfiguratorImpl = TestVerifyConfiguratorImpl()

    override fun enable() {
        enabled = true
    }

    override fun filters(config: KoverMergedFiltersConfigurator.() -> Unit) {
        filters.also(config)
    }

    override fun verify(config: KoverVerifyConfigurator.() -> Unit) {
        verify.also(config)
    }
}

private class KoverMergedFiltersConfiguratorImpl : KoverMergedFiltersConfigurator, TestKoverMergedFiltersConfig {
    override var classes: KoverClassFilter? = null
    override var projects: KoverProjectsFilter? = null

    override fun classes(config: KoverClassFilter.() -> Unit) {
        classes = KoverClassFilter().also(config)
    }

    override fun projects(config: KoverProjectsFilter.() -> Unit) {
        projects = KoverProjectsFilter().also(config)
    }
}

private class TestTaskConfiguratorImpl : TestTaskConfigurator, TestTaskConfig {
    override var excludes: List<String>? = null
    override var includes: List<String>? = null
    override fun excludes(vararg classes: String) {
        excludes = classes.toList()
    }

    override fun includes(vararg classes: String) {
        includes = classes.toList()
    }
}


private class PluginsConfiguratorImpl : PluginsConfigurator, TestPluginsConfig {
    override var useKotlin: Boolean = false
    override var useKover: Boolean = false
    override var kotlinVersion: String? = null
    override var koverVersion: String? = null

    override fun kotlin(version: String?) {
        useKotlin = true
        kotlinVersion = version
    }

    override fun kover(version: String?) {
        useKover = true
        koverVersion = version
    }
}
