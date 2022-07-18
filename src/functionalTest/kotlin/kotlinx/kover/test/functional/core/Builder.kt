/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.core

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.core.writer.initSlice
import org.gradle.api.*
import java.io.*

internal class DiverseBuildState(
    private val rootDir: File,
    private val languages: List<GradleScriptLanguage>,
    private val engines: List<CoverageEngineVendor>,
    private val types: List<ProjectType>,
    private val withCache: Boolean
) : DiverseBuild {
    private val projects: MutableMap<String, ProjectBuilderState> = mutableMapOf()

    override fun addProject(name: String, path: String, builder: ProjectBuilder.() -> Unit) {
        projects[path] = ProjectBuilderState(name).also(builder)
    }

    override fun prepare(): GradleRunner {
        val initSlices: MutableMap<ProjectSlice, File> = mutableMapOf()

        val targetEngines = engines.ifEmpty { listOf(null) }


        languages.forEach { language ->
            types.forEach { type ->
                targetEngines.forEach { engine ->
                    val slice = ProjectSlice(language, type, engine)
                    initSlices[slice] = initSlice(rootDir, slice, projects, withCache)
                }
            }
        }

        val extraArgs = if (withCache) {
            listOf("--build-cache")
        } else {
            listOf()
        }

        return DiverseGradleRunner(initSlices, extraArgs)
    }
}


internal class ProjectBuilderState(val name: String) : ProjectBuilder {
    val sourceTemplates: MutableSet<String> = mutableSetOf()
    val plugins: PluginsState = PluginsState()
    val repositories: RepositoriesState = RepositoriesState()
    var kover: TestKoverProjectConfigState? = null
    var merged: TestKoverMergedConfigState? = null
    val testTasks: TestTaskConfigState = TestTaskConfigState()
    val subprojects: MutableList<String> = mutableListOf()

    override fun plugins(block: Plugins.() -> Unit) {
        plugins.also(block)
    }

    override fun repositories(block: Repositories.() -> Unit) {
        repositories.also(block)
    }

    override fun kover(config: TestKoverProjectConfig.() -> Unit) {
        kover = TestKoverProjectConfigState().also(config)
    }

    override fun koverMerged(config: TestKoverMergedConfig.() -> Unit) {
        merged = TestKoverMergedConfigState().also(config)
    }

    override fun subproject(path: String) {
        subprojects += path
    }

    override fun testTasks(block: TestTaskConfig.() -> Unit) {
        testTasks.also(block)
    }

    override fun sourcesFrom(template: String) {
        sourceTemplates += template
    }
}

internal class TestKoverProjectConfigState : TestKoverProjectConfig {
    override var isDisabled: Boolean? = null
    override var engine: CoverageEngineVariant? = null
    val filters: TestKoverProjectFiltersState = TestKoverProjectFiltersState()
    val instrumentation: KoverProjectInstrumentation = KoverProjectInstrumentation()
    val xml: TestKoverProjectXmlConfigState = TestKoverProjectXmlConfigState()
    val html: TestKoverProjectHtmlConfigState = TestKoverProjectHtmlConfigState()
    val verify: TestKoverVerifyConfigState = TestKoverVerifyConfigState()
    override fun filters(config: TestKoverProjectFilters.() -> Unit) {
        filters.also(config)
    }

    override fun instrumentation(config: KoverProjectInstrumentation.() -> Unit) {
        instrumentation.also(config)
    }

    override fun xmlReport(config: TestKoverProjectXmlConfig.() -> Unit) {
        xml.also(config)
    }

    override fun htmlReport(config: TestKoverProjectHtmlConfig.() -> Unit) {
        html.also(config)
    }

    override fun verify(config: TestKoverVerifyConfig.() -> Unit) {
        verify.also(config)
    }
}

internal class TestKoverProjectFiltersState : TestKoverProjectFilters {
    var classes: KoverClassFilter? = null
    var sourcesets: KoverSourceSetFilter? = null

    override fun classes(config: KoverClassFilter.() -> Unit) {
        classes = KoverClassFilter().also(config)
    }

    override fun sourcesets(config: KoverSourceSetFilter.() -> Unit) {
        sourcesets = KoverSourceSetFilter().also(config)
    }
}

internal class TestKoverProjectXmlConfigState : TestKoverProjectXmlConfig {
    override var onCheck: Boolean? = null
    override var reportFile: File? = null
    var overrideFilters: TestKoverProjectFiltersState? = null

    override fun overrideFilters(config: TestKoverProjectFilters.() -> Unit) {
        if (overrideFilters == null) {
            overrideFilters = TestKoverProjectFiltersState()
        }
        overrideFilters!!.also(config)
    }
}

internal class TestKoverProjectHtmlConfigState : TestKoverProjectHtmlConfig {
    override var onCheck: Boolean? = null
    override var reportDir: File? = null
    var overrideFilters: TestKoverProjectFiltersState? = null

    override fun overrideFilters(config: TestKoverProjectFilters.() -> Unit) {
        if (overrideFilters == null) {
            overrideFilters = TestKoverProjectFiltersState()
        }
        overrideFilters!!.also(config)
    }
}

internal class TestKoverVerifyConfigState : TestKoverVerifyConfig {
    override val onCheck: Boolean? = null
    val rules: MutableList<TestVerificationRule> = mutableListOf()

    override fun rule(config: TestVerificationRule.() -> Unit) {
        rules += TestVerificationRule().also(config)
    }
}

internal class TestVerificationRule {
    var isEnabled: Boolean? = null
    var name: String? = null
    var target: VerificationTarget? = null

    var overrideClassFilter: KoverClassFilter? = null
    val bounds: MutableList<VerificationBoundState> = mutableListOf()

    fun overrideClassFilter(config: Action<KoverClassFilter>) {
        overrideClassFilter = KoverClassFilter().also { config.execute(it) }
    }

    fun bound(configureBound: VerificationBoundState.() -> Unit) {
        bounds += VerificationBoundState().also(configureBound)
    }
}

internal class VerificationBoundState {
    var minValue: Int? = null
    var maxValue: Int? = null
    var counter: CounterType? = null
    var valueType: VerificationValueType? = null
}

internal class TestTaskConfigState : TestTaskConfig {
    var excludes: List<String>? = null
    var includes: List<String>? = null
    override fun excludes(vararg classes: String) {
        excludes = classes.toList()
    }

    override fun includes(vararg classes: String) {
        includes = classes.toList()
    }

}

internal class RepositoriesState : Repositories {
    val repositories: MutableList<String> = mutableListOf()
    override fun repository(name: String) {
        repositories += name
    }

}

internal class TestKoverMergedConfigState : TestKoverMergedConfig {
    var enabled: Boolean = false
    val filters: TestKoverMergedFiltersState = TestKoverMergedFiltersState()
    override fun enable() {
        enabled = true
    }

    override fun filters(config: TestKoverMergedFilters.() -> Unit) {
        filters.also(config)
    }
}

internal class TestKoverMergedFiltersState : TestKoverMergedFilters {
    var classes: KoverClassFilter? = null
    var projects: KoverProjectsFilter? = null
    override fun classes(config: KoverClassFilter.() -> Unit) {
        classes = KoverClassFilter().also(config)
    }

    override fun projects(config: KoverProjectsFilter.() -> Unit) {
        projects = KoverProjectsFilter().also(config)
    }
}

internal class PluginsState : Plugins {
    var useKotlin: Boolean = false
    var useKover: Boolean = false
    var kotlinVersion: String? = null
    var koverVersion: String? = null

    override fun kotlin(version: String?) {
        useKotlin = true
        kotlinVersion = version
    }

    override fun kover(version: String?) {
        useKover = true
        koverVersion = version
    }

}
