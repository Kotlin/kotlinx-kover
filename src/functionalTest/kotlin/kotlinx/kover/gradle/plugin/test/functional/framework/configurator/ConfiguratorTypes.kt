/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.configurator

import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.*


internal interface BuildConfigurator {
    fun addProjectWithKover(path: String = ":", name: String = path.substringAfterLast(":"), generator: ProjectConfigurator.() -> Unit)

    fun addProject(path: String, name: String, generator: ProjectConfigurator.() -> Unit)

    fun run(vararg args: String, checker: CheckerContext.() -> Unit = {})

    fun runWithError(vararg args: String, errorChecker: CheckerContext.() -> Unit = {})

    fun useLocalCache(use: Boolean = true)

    fun prepare(): TestBuildConfig
}

internal interface ProjectConfigurator {
    fun plugins(block: PluginsConfigurator.() -> Unit)

    fun repositories(block: RepositoriesConfigurator.() -> Unit)

    fun kover(config: KoverProjectExtension.() -> Unit)

    fun koverReport(config: KoverReportExtension.() -> Unit)

    fun sourcesFrom(template: String)

    fun dependencyKover(path: String)
}

internal interface PluginsConfigurator {
    fun kotlin(version: String? = null)
    fun kover(version: String? = null)
}

internal interface RepositoriesConfigurator {
    fun repository(name: String)
}


internal abstract class BuilderConfiguratorWrapper(private val origin: BuildConfigurator) : BuildConfigurator {

    override fun addProjectWithKover(path: String, name: String, generator: ProjectConfigurator.() -> Unit) {
        origin.addProjectWithKover(path, name, generator)
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
