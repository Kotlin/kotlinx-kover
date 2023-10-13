/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.configurator

import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.*
import kotlinx.kover.gradle.plugin.test.functional.framework.common.*
import kotlinx.kover.gradle.plugin.test.functional.framework.common.kotlinVersionCurrent
import kotlinx.kover.gradle.plugin.test.functional.framework.writer.*

internal fun createConfigurator(): BuildConfigurator {
    return TestBuildConfigurator()
}

/**
 * [projects] - is a map of project path -> project config
 */
internal class TestBuildConfig(
    val projects: Map<String, TestProjectConfigurator>,
    val steps: List<TestExecutionStep>,
    val useLocalCache: Boolean
)

internal sealed class TestExecutionStep {
    abstract val name: String
}

internal data class TestGradleStep(
    val args: List<String>,
    val checker: CheckerContext.() -> Unit,
    val errorExpected: Boolean? = null
): TestExecutionStep() {
    override val name: String = "Gradle: '${args.joinToString(" ")}'"
}

internal data class TestFileEditStep(
    val filePath: String,
    val editor: (String) -> String
): TestExecutionStep() {
    override val name: String = "Edit file: $filePath"
}

internal data class TestFileAddStep(
    val filePath: String,
    val editor: () -> String
): TestExecutionStep() {
    override val name: String = "Add file: $filePath"
}

internal data class TestFileDeleteStep(val filePath: String): TestExecutionStep() {
    override val name: String = "Delete file: $filePath"
}

private open class TestBuildConfigurator : BuildConfigurator {
    private val projects: MutableMap<String, TestProjectConfigurator> = mutableMapOf()
    private val steps: MutableList<TestExecutionStep> = mutableListOf()
    private var useCache: Boolean = false

    override fun addProject(path: String, name: String, generator: ProjectConfigurator.() -> Unit) {
        if (projects.contains(path)) throw IllegalArgumentException("Project with path $path has already been added")
        projects[path] = TestProjectConfigurator().also(generator)
    }

    override fun addProjectWithKover(path: String, name: String, generator: ProjectConfigurator.() -> Unit) {
        addProject(path, name) {
            plugins {
                if (path == ":") {
                    kotlin(kotlinVersionCurrent)
                    kover(koverVersionCurrent)
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

    override fun run(vararg args: String, errorExpected: Boolean?, checker: CheckerContext.() -> Unit) {
        steps += TestGradleStep(listOf(*args), checker, errorExpected)
    }

    override fun edit(filePath: String, editor: (String) -> String) {
        steps += TestFileEditStep(filePath, editor)
    }

    override fun add(filePath: String, editor: () -> String) {
        steps += TestFileAddStep(filePath, editor)
    }

    override fun delete(filePath: String) {
        steps += TestFileDeleteStep(filePath)
    }

    override fun useLocalCache(use: Boolean) {
        useCache = use
    }

    override fun prepare(): TestBuildConfig {
        return TestBuildConfig(projects, steps, useCache)
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
