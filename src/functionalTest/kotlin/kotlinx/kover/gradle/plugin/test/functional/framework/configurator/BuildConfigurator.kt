/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.configurator

import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.*
import kotlinx.kover.gradle.plugin.test.functional.framework.common.*
import kotlinx.kover.gradle.plugin.test.functional.framework.common.kotlinVersion
import kotlinx.kover.gradle.plugin.test.functional.framework.writer.*

internal fun createConfigurator(): BuildConfigurator {
    return TestBuildConfigurator()
}

/**
 * [projects] - is a map of project path -> project config
 */
internal class TestBuildConfig(
    val projects: Map<String, TestProjectConfigurator>,
    val runs: List<TestRunConfig>,
    val useLocalCache: Boolean
)

internal data class TestRunConfig(
    val args: List<String>,
    val checker: CheckerContext.() -> Unit,
    val errorExpected: Boolean = false
)

private open class TestBuildConfigurator : BuildConfigurator {
    private val projects: MutableMap<String, TestProjectConfigurator> = mutableMapOf()
    private val runs: MutableList<TestRunConfig> = mutableListOf()
    private var useCache: Boolean = false

    override fun addProject(path: String, name: String, generator: ProjectConfigurator.() -> Unit) {
        projects[path] = TestProjectConfigurator().also(generator)
    }

    override fun addProjectWithKover(path: String, name: String, generator: ProjectConfigurator.() -> Unit) {
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


internal class TestProjectConfigurator(private val name: String? = null) : ProjectConfigurator {
    val sourceTemplates: MutableSet<String> = mutableSetOf()
    val plugins: PluginsConfiguratorImpl = PluginsConfiguratorImpl()
    val projectDependencies: MutableList<String> = mutableListOf()
    val rawBlocks: MutableList<String> = mutableListOf()

    private val repositoriesConfigurator: TestRepositoriesConfigurator = TestRepositoriesConfigurator()

    val repositories: MutableList<String>
        get() = repositoriesConfigurator.repositories

    override fun plugins(block: PluginsConfigurator.() -> Unit) {
        plugins.also(block)
    }

    override fun repositories(block: RepositoriesConfigurator.() -> Unit) {
        repositoriesConfigurator.also(block)
    }

    override fun kover(config: KoverProjectExtension.() -> Unit) {
        val builder = StringBuilder()
        val writer = FormattedWriter(builder::append)

        writer.call("kover") {
            val koverWriter = KoverWriter(this)
            config(koverWriter)
        }

        rawBlocks += builder.toString()
    }

    override fun koverReport(config: KoverReportExtension.() -> Unit) {
        val builder = StringBuilder()
        val writer = FormattedWriter(builder::append)

        writer.call("koverReport") {
            val koverWriter = KoverReportExtensionWriter(this)
            config(koverWriter)
        }


        rawBlocks += builder.toString()
    }


    override fun sourcesFrom(template: String) {
        sourceTemplates += template
    }

    override fun dependencyKover(path: String) {
        projectDependencies += path
    }
}



internal class PluginsConfiguratorImpl : PluginsConfigurator {
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

internal class TestRepositoriesConfigurator: RepositoriesConfigurator {
    val repositories = mutableListOf<String>()

    override fun repository(name: String) {
        repositories += name
    }
}
