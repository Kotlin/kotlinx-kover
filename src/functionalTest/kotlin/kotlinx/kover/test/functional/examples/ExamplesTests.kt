/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.examples

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.common.*
import org.junit.*
import org.junit.rules.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.io.File
import kotlin.test.*
import kotlin.test.Test

private const val EXAMPLES_DIR = "examples"

private val pluginRegex =
    """id\(?\s*["']org.jetbrains.kotlinx.kover["']\s*\)?\s+version\s+["']([^"^']+)["']""".toRegex()
private val dependencyRegex = """classpath\(?\s*["']org.jetbrains.kotlinx:kover:([^"^']+)["']""".toRegex()
private val intellijEngineRegex = """IntellijEngine\s*\(\s*["']([^"^']+)["']\s*\)""".toRegex()
private val jacocoEngineRegex = """JacocoEngine\s*\(\s*["']([^"^']+)["']\s*\)""".toRegex()


@RunWith(Parameterized::class)
internal class ExamplesTests(private val example: File) {
    @Rule
    @JvmField
    internal val rootFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun testExample() {
        analyzeExample()
        val target = File(rootFolder.root, example.name)
        example.copyRecursively(target)
        target.gradleBuild(listOf("build"))
    }

    @Suppress("UNUSED_VARIABLE")
    private fun analyzeExample() {
        val settingsFile =
            example.listFiles { it -> it.isFile && (it.name == "settings.gradle.kts" || it.name == "settings.gradle") }
                ?.singleOrNull() ?: throw Exception("Settings file not found in example '${example.name}'")

        val buildFile =
            example.listFiles { it -> it.isFile && (it.name == "build.gradle.kts" || it.name == "build.gradle") }
                ?.singleOrNull() ?: throw Exception("Settings file not found in example '${example.name}'")


        val language = buildFile.extractScriptLanguage()

        val buildScript = buildFile.readText()


        val pluginVersion = pluginRegex.findAll(buildScript).singleOrNull()?.groupValues?.getOrNull(1)
        val dependencyVersion = dependencyRegex.findAll(buildScript).singleOrNull()?.groupValues?.getOrNull(1)

        if (pluginVersion != null && dependencyVersion != null) {
            throw Exception("Using the old and new ways of applying plugins in the example '${example.name}'")
        }
        val koverVersion =
            pluginVersion ?: dependencyVersion ?: throw Exception("Kover plugin is not used in the example '${example.name}'")

        val expectedVersion = System.getProperty("koverVersion")
        assertEquals(expectedVersion, koverVersion, "Invalid Kover version in example '${example.name}'")


        val intellijEngineVersion = intellijEngineRegex.findAll(buildScript).singleOrNull()?.groupValues?.getOrNull(1)
        val jacocoEngineVersion = jacocoEngineRegex.findAll(buildScript).singleOrNull()?.groupValues?.getOrNull(1)

        if (intellijEngineVersion != null && jacocoEngineVersion != null) {
            throw Exception("Both coverage engines used in example '${example.name}'")
        }
        if (intellijEngineVersion != null) {
            assertEquals(
                KoverVersions.DEFAULT_INTELLIJ_VERSION,
                intellijEngineVersion,
                "Invalid IntelliJ version in example '${example.name}'"
            )
        }
        if (jacocoEngineVersion != null) {
            assertEquals(
                KoverVersions.DEFAULT_JACOCO_VERSION,
                jacocoEngineVersion,
                "Invalid JaCoCo version in example '${example.name}'"
            )
        }
    }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic
        fun findExamples(): Collection<Array<File>> {
            val examplesDir = File(EXAMPLES_DIR)
            return examplesDir.listFiles { it -> it.isDirectory }?.map { arrayOf(it) } ?: emptyList()
        }
    }

}
