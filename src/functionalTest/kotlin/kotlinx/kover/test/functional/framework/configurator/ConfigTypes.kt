/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.configurator

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.framework.checker.CheckerContext
import java.io.*

/**
 * [projects] - is a map of project path -> project config
 */
internal class TestBuildConfig(
    val projects: Map<String, TestProjectConfig>,
    val runs: List<TestRunConfig>,
    val useLocalCache: Boolean
)

internal data class TestRunConfig(
    val args: List<String>,
    val checker: CheckerContext.() -> Unit,
    val errorExpected: Boolean = false
)

internal interface TestProjectConfig {
    val sourceTemplates: Set<String>
    val plugins: TestPluginsConfig
    val repositories: List<String>
    val kover: TestKoverConfig?
    val merged: TestKoverMergedConfig?
    val testTasks: TestTaskConfig
    val projectDependencies: List<String>
}

internal interface TestKoverConfig {
    val isDisabled: Boolean?
    val engine: CoverageEngineVariant?
    val filters: TestKoverFiltersConfig
    val instrumentation: KoverProjectInstrumentation
    val xml: TestXmlConfig
    val html: TestHtmlConfig
    val verify: TestVerifyConfig
}

internal interface TestKoverMergedConfig {
    val enabled: Boolean
    val filters: TestKoverMergedFiltersConfig
    val xml: TestMergedXmlConfig
    val verify: TestVerifyConfig
}

internal interface TestKoverFiltersConfig {
    val classes: KoverClassFilter?
    val annotations: KoverAnnotationFilter?
    val sourceSets: KoverSourceSetFilter?
}

internal interface TestXmlConfig {
    val onCheck: Boolean?
    val reportFile: File?
    val overrideFilters: TestKoverFiltersConfig?
}

internal interface TestMergedXmlConfig {
    val onCheck: Boolean?
    val reportFile: File?
    val overrideClassFilter: KoverClassFilter?
    val overrideAnnotationFilter: KoverAnnotationFilter?
}

internal interface TestHtmlConfig {
    val onCheck: Boolean?
    val reportDir: File?
    val overrideFilters: TestKoverFiltersConfig?
}

internal interface TestVerifyConfig {
    val onCheck: Boolean?
    val rules: MutableList<VerificationRuleConfigurator>
}

internal interface TestTaskConfig {
    val excludes: List<String>?
    val includes: List<String>?
}

internal interface TestPluginsConfig {
    val useKotlin: Boolean
    val useKover: Boolean
    val kotlinVersion: String?
    val koverVersion: String?
}

internal interface TestKoverMergedFiltersConfig {
    val classes: KoverClassFilter?
    val annotations: KoverAnnotationFilter?
    val projects: KoverProjectsFilter?
}
