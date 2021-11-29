package kotlinx.kover.test.functional.core

import kotlinx.kover.api.*
import org.gradle.testkit.runner.*
import java.io.*

internal enum class GradleScriptLanguage { KOTLIN, GROOVY }

internal enum class ProjectType { KOTLIN_JVM, KOTLIN_MULTIPLATFORM, ANDROID }

internal interface ModuleBuilder<S : ModuleBuilder<S>> {
    fun sources(template: String): S
    fun verification(rules: Iterable<VerificationRule>): S
    fun config(script: String): S
    fun config(kotlin: String, groovy: String): S
}

internal interface ProjectRunner : ModuleBuilder<ProjectRunner> {
    fun case(description: String): ProjectRunner
    fun languages(vararg languages: GradleScriptLanguage): ProjectRunner
    fun engines(vararg engines: CoverageEngine): ProjectRunner
    fun types(vararg types: ProjectType): ProjectRunner
    fun setIntellijVersion(version: String): ProjectRunner
    fun setJacocoVersion(version: String): ProjectRunner
    fun submodule(name: String, builder: ModuleBuilder<*>.() -> Unit): ProjectRunner
    fun kover(rootExtensionScript: String): ProjectRunner
    fun check(vararg args: String, block: RunResult.() -> Unit): ProjectRunner
}

internal class RunResult(private val result: BuildResult, private val dir: File) {
    val buildDir: File = File(dir, "build")

    val output = result.output

    fun file(name: String): File {
        return File(buildDir, name)
    }

    fun outcome(taskPath: String): TaskOutcome {
        return result.task(taskPath)?.outcome
            ?: throw IllegalArgumentException("Task '$taskPath' not found in build result")
    }
}
