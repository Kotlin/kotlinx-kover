/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*
import java.io.*
import kotlin.test.*

internal class ConfigurationCacheTests {
    private val subprojectPath = ":common"

    @GeneratedTest
    fun BuildConfigurator.testConfigCache() {
        addProjectWithKover(subprojectPath) {
            sourcesFrom("multiproject-common")

        }

        addProjectWithKover {
            sourcesFrom("multiproject-user")
            dependencyKover(subprojectPath)
        }

        run(
            "build",
            "koverXmlReport",
            "koverHtmlReport",
            "koverVerify",
            "--configuration-cache",
        )
    }

    @GeneratedTest
    fun BuildConfigurator.testProjectIsolation() {
        addProjectWithKover(subprojectPath) {
            sourcesFrom("multiproject-common")

        }

        addProjectWithKover {
            sourcesFrom("multiproject-user")
            dependencyKover(subprojectPath)
        }

        run(
            ":koverXmlReport",
            ":koverHtmlReport",
            ":koverVerify",
            "-Dorg.gradle.unsafe.isolated-projects=true",
            errorExpected = null,
        ) {
            if (!hasError) {
                return@run
            }

            // With the current versions of the Kotlin plugin, an error occurs breaking the isolation of projects. The easiest way to check that the error is not caused by the Kover plugin, then it is enough to find occurrences of this word.
            // However, the report in any case contains 'kover' words (for example, task names), so they need to be excluded
            val errorLine: String = output.lineSequence().firstOrNull { it.startsWith("See the complete report at ") }
                ?: throw AssertionError("Expected Project isolation errors for current Kotlin implementations")
            val filePath = errorLine.substringAfter("See the complete report at ")

            val hasKoverErrors = File(filePath.removePrefix("file://")).bufferedReader()
                .containsWord("kover", "kover-functional-test", "koverXmlReport", "koverHtmlReport", "koverVerify",
                    // {"name":"build/kover/default.artifact"}]},{"trace":[{"kind":"BuildLogicClass","type":"kotlinx.kover.gradle.plugin.commons.ArtifactsKt"}]
                    "kover/default.artifact",
                    "kover.gradle.plugin.commons.ArtifactsKt",
                    // Execution of task ':common:compileKotlin' caused invocation of 'Task.project' by task ':common:koverFindJar' at execution time which is unsupported.
                    "koverFindJar' at execution time which is unsupported.",
                    // Execution of task ':common:compileKotlin' caused invocation of 'Task.project' by task ':common:koverGenerateArtifact' at execution time which is unsupported.
                    "koverGenerateArtifact' at execution time which is unsupported.")

            assertFalse(hasKoverErrors, "Project isolation report contains unexpected words 'kover', perhaps the Kover plugin breaks the project isolation")
        }
    }

    // Text contains [searchingWord] except of words [exceptions]
    private fun BufferedReader.containsWord(searchingWord: String, vararg exceptions: String): Boolean {
        lineSequence().forEach { line ->
            var index = 0
            while (true) {
                index = line.indexOf(searchingWord, index)
                if (index < 0) break

                val tail = line.substring(index)
                if (exceptions.none { exception -> tail.startsWith(exception) }) {
                    return true
                }
                index++
            }
        }
        return false
    }
}
