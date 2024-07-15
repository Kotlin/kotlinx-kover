/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.TemplateTest
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SettingsPluginTests {
    @TemplateTest("settings-plugin", [":tasks"])
    fun CheckerContext.testNoReportTasks() {
        taskOutput(":tasks") {
            assertFalse("koverXmlReport" in this)
            assertFalse("koverHtmlReport" in this)
        }
    }

    @TemplateTest("settings-plugin", ["test"])
    fun CheckerContext.testNoInstrumentation() {
        checkDefaultBinReport(false)
        subproject("subproject") {
            checkDefaultBinReport(false)
        }
    }

    @TemplateTest("settings-plugin", ["-Pkover", ":tasks", "-Dorg.gradle.unsafe.isolated-projects=true", "--configuration-cache", "--build-cache"])
    fun CheckerContext.testHasReportTasks() {
        taskOutput(":tasks") {
            assertTrue("koverXmlReport" in this)
            assertTrue("koverHtmlReport" in this)
        }
    }

    @TemplateTest("settings-plugin", ["-Pkover", "koverXmlReport", "-Dorg.gradle.unsafe.isolated-projects=true", "--configuration-cache", "--build-cache"])
    fun CheckerContext.testNoCompilations() {
        xmlReport {
            classCounter("tests.settings.root.RootClass").assertAbsent()
            classCounter("tests.settings.subproject.SubprojectClass").assertAbsent()
        }
    }

    @TemplateTest("settings-plugin", ["-Pkover", ":compileKotlin", "koverXmlReport", "-Dorg.gradle.unsafe.isolated-projects=true", "--configuration-cache", "--build-cache"])
    fun CheckerContext.testCompilationOnlyForRoot() {
        xmlReport {
            classCounter("tests.settings.root.RootClass").assertFullyMissed()
            classCounter("tests.settings.subproject.SubprojectClass").assertAbsent()
        }
    }

    @TemplateTest("settings-plugin", ["-Pkover", ":subproject:compileKotlin", ":test", "koverXmlReport", "-Dorg.gradle.unsafe.isolated-projects=true", "--configuration-cache", "--build-cache"])
    fun CheckerContext.testRootAndOnlyCompileSubproject() {
        xmlReport {
            classCounter("tests.settings.root.RootClass").assertFullyCovered()
            classCounter("tests.settings.subproject.SubprojectClass").assertFullyMissed()
        }
    }


    @TemplateTest("settings-plugin", ["-Pkover", "test", "koverXmlReport", "-Dorg.gradle.unsafe.isolated-projects=true", "--configuration-cache", "--build-cache"])
    fun CheckerContext.testAll() {
        xmlReport {
            classCounter("tests.settings.root.RootClass").assertFullyCovered()
            classCounter("tests.settings.subproject.SubprojectClass").assertFullyCovered()
        }
    }

    @TemplateTest("settings-plugin", ["-Pkover", "test", "koverXmlReport", "-Pkover.projects.excludes=:subproject", "-Dorg.gradle.unsafe.isolated-projects=true", "--configuration-cache", "--build-cache"])
    fun CheckerContext.testExcludeSubproject() {
        xmlReport {
            classCounter("tests.settings.root.RootClass").assertFullyCovered()
            classCounter("tests.settings.subproject.SubprojectClass").assertAbsent()
        }
    }

    @TemplateTest("settings-plugin", ["-Pkover", "test", "koverXmlReport", "-Pkover.classes.excludes=tests.settings.subproject.*", "-Dorg.gradle.unsafe.isolated-projects=true", "--configuration-cache", "--build-cache"])
    fun CheckerContext.testExcludeClasses() {
        xmlReport {
            classCounter("tests.settings.root.RootClass").assertFullyCovered()
            classCounter("tests.settings.subproject.SubprojectClass").assertAbsent()
        }
    }

    @TemplateTest("settings-plugin-android", ["-Pkover", ":app:testDebugUnitTest", "koverXmlReport"])
    fun CheckerContext.testAndroid() {
        xmlReport {
            classCounter("kotlinx.kover.test.android.MainClass").assertCovered()
            classCounter("kotlinx.kover.test.android.DebugClass").assertFullyMissed()
            classCounter("kotlinx.kover.test.android.ReleaseClass").assertAbsent()
            classCounter("kotlinx.kover.test.android.LocalTests").assertAbsent()
        }
    }
}