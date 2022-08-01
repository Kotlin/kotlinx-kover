/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.core

import kotlinx.kover.api.*
import org.gradle.testkit.runner.*
import java.io.*

internal enum class GradleScriptLanguage { KOTLIN, GROOVY }

internal enum class ProjectType { KOTLIN_JVM, KOTLIN_MULTIPLATFORM, ANDROID }

internal interface DiverseBuild {
    fun addProject(name: String, path: String, builder: ProjectBuilder.() -> Unit)
    fun prepare(): GradleRunner
}

internal interface ProjectBuilder {
    fun plugins(block: Plugins.() -> Unit)

    fun repositories(block: Repositories.() -> Unit)

    fun kover(config: TestKoverProjectConfig.() -> Unit)

    fun koverMerged(config: TestKoverMergedConfig.() -> Unit)

    fun testTasks(block: TestTaskConfig.() -> Unit)

    fun subproject(path: String)

    fun sourcesFrom(template: String)
}


interface Plugins {
    fun kotlin(version: String? = null)
    fun kover(version: String? = null)
}

interface Repositories {
    fun repository(name: String)
}

interface TestTaskConfig {
    fun excludes(vararg classes: String)
    fun includes(vararg classes: String)
}

/**
 * Same as [KoverProjectConfig]
 */
internal interface TestKoverProjectConfig {
    var isDisabled: Boolean?

    var engine: CoverageEngineVariant?

    fun filters(config: TestKoverProjectFilters.() -> Unit)

    fun instrumentation(config: KoverProjectInstrumentation.() -> Unit)

    fun xmlReport(config: TestKoverProjectXmlConfig.() -> Unit)

    fun htmlReport(config: TestKoverProjectHtmlConfig.() -> Unit)

    fun verify(config: TestKoverVerifyConfig.() -> Unit)
}


internal interface TestKoverVerifyConfig {
    val onCheck: Boolean?

    fun rule(config: TestVerificationRule.() -> Unit)
}

internal interface TestKoverProjectXmlConfig {
    var onCheck: Boolean?
    var reportFile: File?

    fun overrideFilters(config: TestKoverProjectFilters.() -> Unit)
}

internal interface TestKoverProjectHtmlConfig {
    var onCheck: Boolean?
    var reportDir: File?

    fun overrideFilters(config: TestKoverProjectFilters.() -> Unit)
}

internal interface TestKoverProjectFilters {
    fun classes(config: KoverClassFilter.() -> Unit)

    fun sourceSets(config: KoverSourceSetFilter.() -> Unit)
}

internal interface TestKoverMergedConfig {
    public fun enable()

    public fun filters(config: TestKoverMergedFilters.() -> Unit)
}

public interface TestKoverMergedFilters {
    public fun classes(config: KoverClassFilter.() -> Unit)

    public fun projects(config: KoverProjectsFilter.() -> Unit)
}


internal data class ProjectSlice(
    val language: GradleScriptLanguage,
    val type: ProjectType,
    val engine: CoverageEngineVendor?
) {
    fun encodedString(): String {
        return "${language.ordinal}_${type.ordinal}_${engine?.ordinal ?: "default"}"
    }
}

internal interface GradleRunner {
    fun run(vararg args: String, checker: RunResult.() -> Unit = {}): GradleRunner
    fun runWithError(vararg args: String, errorChecker: RunResult.() -> Unit = {}): GradleRunner
}

internal interface RunResult {
    val engine: CoverageEngineVendor
    val projectType: ProjectType

    fun subproject(name: String, checker: RunResult.() -> Unit)

    fun output(checker: String.() -> Unit)

    fun file(name: String, checker: File.() -> Unit)

    fun xml(filename: String, checker: XmlReport.() -> Unit)

    fun outcome(taskName: String, checker: TaskOutcome.() -> Unit)
}


internal class Counter(val type: String, val missed: Int, val covered: Int) {
    val isEmpty: Boolean
        get() = missed == 0 && covered == 0
}

internal interface XmlReport {
    fun classCounter(className: String, type: String = "INSTRUCTION"): Counter?
    fun methodCounter(className: String, methodName: String, type: String = "INSTRUCTION"): Counter?
}
