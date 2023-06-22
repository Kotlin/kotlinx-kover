/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.runner.buildFromTemplate
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runWithParams
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.test.assertEquals

class BuildCacheRelocationTests {
    @Test
    fun testDefaultTasks() {
        val cachePath = Files.createTempDirectory("test-gradle-cache-").toFile().canonicalPath

        val cachePatch = """
            buildCache {
                local {
                    directory = "$cachePath"
                }
            }"""

        val buildSource = buildFromTemplate("counters")

        val gradleBuild1 = buildSource.generate()
        gradleBuild1.targetDir.resolve("settings.gradle.kts").appendText(cachePatch)
        val result1 = gradleBuild1.runWithParams("koverXmlReport", "koverHtmlReport", "koverVerify", "--build-cache")
        assertEquals("SUCCESS", result1.taskOutcome(":test"))
        assertEquals("SUCCESS", result1.taskOutcome(":koverGenerateArtifact"))
        assertEquals("SUCCESS", result1.taskOutcome(":koverXmlReport"))
        assertEquals("SUCCESS", result1.taskOutcome(":koverHtmlReport"))
        assertEquals("SUCCESS", result1.taskOutcome(":koverVerify"))

        /*
        since the build is created from a template,
        repeat generation will create a new directory with exactly the same contents as the previous one.

        Thus, when using the same cache, tasks will not be executed, and the result will be taken from the cache.
         */
        val gradleBuild2 = buildSource.generate()
        gradleBuild2.targetDir.resolve("settings.gradle.kts").appendText(cachePatch)
        val result2 = gradleBuild2.runWithParams("koverXmlReport", "koverHtmlReport", "koverVerify", "--build-cache")
        assertEquals("FROM-CACHE", result2.taskOutcome(":test"))
        assertEquals("FROM-CACHE", result2.taskOutcome(":koverGenerateArtifact"))
        assertEquals("FROM-CACHE", result2.taskOutcome(":koverXmlReport"))
        assertEquals("FROM-CACHE", result2.taskOutcome(":koverHtmlReport"))
        assertEquals("FROM-CACHE", result2.taskOutcome(":koverVerify"))
    }
}