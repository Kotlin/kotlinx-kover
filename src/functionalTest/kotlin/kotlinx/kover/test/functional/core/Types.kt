package kotlinx.kover.test.functional.core

import kotlinx.kover.api.*
import org.gradle.testkit.runner.*
import java.io.*

internal enum class GradleScriptLanguage { KOTLIN, GROOVY }

internal enum class ProjectType { KOTLIN_JVM, KOTLIN_MULTIPLATFORM, ANDROID }

internal interface ProjectBuilder<B : ProjectBuilder<B>> {
    fun sources(template: String): B
    fun verification(rules: Iterable<VerificationRule>): B

    fun configTest(script: String): B
    fun configTest(kotlin: String, groovy: String): B

    fun config(script: String): B
    fun config(kotlin: String, groovy: String): B

    fun dependency(script: String): B
    fun dependency(kotlin: String, groovy: String): B
}

internal interface TestCaseBuilder : ProjectBuilder<TestCaseBuilder> {
    fun languages(vararg languages: GradleScriptLanguage): TestCaseBuilder
    fun engines(vararg engines: CoverageEngine): TestCaseBuilder
    fun types(vararg types: ProjectType): TestCaseBuilder

    fun withLocalCache(): TestCaseBuilder

    fun configKover(config: KoverRootConfig.() -> Unit): TestCaseBuilder

    fun subproject(name: String, builder: ProjectBuilder<*>.() -> Unit): TestCaseBuilder

    fun build(): GradleRunner
}

internal data class ProjectSlice(val language: GradleScriptLanguage, val type: ProjectType, val engine: CoverageEngine?) {
    fun encodedString(): String {
        return "${language.ordinal}_${type.ordinal}_${engine?.ordinal?:"default"}"
    }
}

internal data class KoverRootConfig(
    var disabled: Boolean? = null,
    var intellijVersion: String? = null,
    var jacocoVersion: String? = null,
    var generateReportOnCheck: Boolean? = null,
    var runAllTestsForProjectTask: Boolean? = null,
    val disabledProjects: MutableSet<String> = mutableSetOf()
) {
    val isDefault =
        disabled == null && intellijVersion == null && jacocoVersion == null && generateReportOnCheck == null
}

internal interface GradleRunner {
    fun run(vararg args: String, checker: RunResult.() -> Unit = {}): GradleRunner
}

internal interface RunResult {
    val engine: CoverageEngine
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
