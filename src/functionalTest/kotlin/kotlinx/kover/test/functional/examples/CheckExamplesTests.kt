/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.examples

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.common.*
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.junit.*
import org.junit.rules.*
import java.io.File
import kotlin.test.*
import kotlin.test.Test

private const val EXAMPLES_DIR = "examples"

private val pluginRegex = """id\(?\s*["']org.jetbrains.kotlinx.kover["']\s*\)?\s+version\s+["']([^"^']+)["']""".toRegex()
private val dependencyRegex = """classpath\(?\s*["']org.jetbrains.kotlinx:kover:([^"^']+)["']""".toRegex()
private val intellijEngineRegex = """IntellijEngine\s*\(\s*["']([^"^']+)["']\s*\)""".toRegex()
private val jacocoEngineRegex = """JacocoEngine\s*\(\s*["']([^"^']+)["']\s*\)""".toRegex()


internal class CheckExamplesTests {
    @Rule
    @JvmField
    internal val rootFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun testExamples() {
        findExamples().forEach {
            it.analyzeExample()
            val target = File(rootFolder.root, it.name)
            it.copyRecursively(target)
            try {
                target.gradleBuild(listOf("build"))
            } catch (e: UnexpectedBuildFailure) {
                throw Exception("An error occurred while building the example '${it.name}'\nBUILD LOG\n${e.buildResult.output}", e)
            }
        }
    }
}


@Suppress("UNUSED_VARIABLE")
private fun File.analyzeExample() {
    val settingsFile =
        listFiles { it -> it.isFile && (it.name == "settings.gradle.kts" || it.name == "settings.gradle") }
            ?.singleOrNull() ?: throw Exception("Settings file not found in example '$name'")

    val buildFile =
        listFiles { it -> it.isFile && (it.name == "build.gradle.kts" || it.name == "build.gradle") }
            ?.singleOrNull() ?: throw Exception("Settings file not found in example '$name'")


    val language = buildFile.extractScriptLanguage()

    val buildScript = buildFile.readText()



    val pluginVersion = pluginRegex.findAll(buildScript).singleOrNull()?.groupValues?.getOrNull(1)
    val dependencyVersion = dependencyRegex.findAll(buildScript).singleOrNull()?.groupValues?.getOrNull(1)

    if (pluginVersion != null && dependencyVersion != null) {
        throw Exception("Using the old and new ways of applying plugins in the example '$name'")
    }
    val koverVersion =
        pluginVersion ?: dependencyVersion ?: throw Exception("Kover plugin is not used in the example '$name'")

    val expectedVersion = System.getProperty("koverVersion")
    assertEquals(expectedVersion, koverVersion, "Invalid Kover version in example '$name'")



    val intellijEngineVersion = intellijEngineRegex.findAll(buildScript).singleOrNull()?.groupValues?.getOrNull(1)
    val jacocoEngineVersion = jacocoEngineRegex.findAll(buildScript).singleOrNull()?.groupValues?.getOrNull(1)

    if (intellijEngineVersion != null && jacocoEngineVersion != null) {
        throw Exception("Both coverage engines used in example '$name'")
    }
    if (intellijEngineVersion != null) {
        assertEquals(KoverVersions.DEFAULT_INTELLIJ_VERSION, intellijEngineVersion, "Invalid IntelliJ version in example '$name'")
    }
    if (jacocoEngineVersion != null) {
        assertEquals(KoverVersions.DEFAULT_JACOCO_VERSION, jacocoEngineVersion, "Invalid JaCoCo version in example '$name'")
    }
}


private fun findExamples(): List<File> {
    val examplesDir = File(EXAMPLES_DIR)
    return examplesDir.listFiles { it -> it.isDirectory }?.toList() ?: emptyList()
}
