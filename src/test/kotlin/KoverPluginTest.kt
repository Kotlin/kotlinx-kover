package kotlinx.kover.json

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class KoverPluginTest {

    @Test
    fun koverPluginTasksTest() {

        val projectDir = Files.createTempDirectory("kover-gradle-test").toFile()

        val settings = projectDir.resolve("settings.gradle.kts")
        settings.writeText(
            """
                rootProject.name = "kover-plugin-tasks-test"
            """.trimIndent()
        )

        val buildFile = projectDir.resolve("build.gradle.kts")
        buildFile.writeText(
            """
                plugins {
                    java
                    id("org.jetbrains.kotlinx.kover")
                }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(":tasks", "--stacktrace", "--info")
            .withPluginClasspath()
            .build()

        println(result.output)

        assertTrue(result.output.contains("Hello world!"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":helloWorld")?.outcome)
    }
}
