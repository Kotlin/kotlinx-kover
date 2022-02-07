package kotlinx.kover.test.functional.core

import kotlinx.kover.api.*
import java.io.*

internal fun createBuilder(rootDir: File, description: String): TestCaseBuilder {
    return TestCaseBuilderImpl(rootDir, description)
}

internal class CommonBuilderState(val description: String) {
    var pluginVersion: String? = null
    val languages: MutableSet<GradleScriptLanguage> = mutableSetOf()
    val types: MutableSet<ProjectType> = mutableSetOf()
    val engines: MutableSet<CoverageEngine?> = mutableSetOf()
    val koverConfig: KoverRootConfig = KoverRootConfig()
    val rootProject: ProjectBuilderState = ProjectBuilderState()
    val subprojects: MutableMap<String, ProjectBuilderState> = mutableMapOf()
    var localCache: Boolean = false
}

internal class ProjectBuilderState {
    val sourceTemplates: MutableList<String> = mutableListOf()
    val scripts: MutableList<GradleScript> = mutableListOf()
    val testScripts: MutableList<GradleScript> = mutableListOf()
    val dependencies: MutableList<GradleScript> = mutableListOf()
    val rules: MutableList<VerificationRule> = mutableListOf()
    val mainSources: MutableMap<String, String> = mutableMapOf()
    val testSources: MutableMap<String, String> = mutableMapOf()
}

internal data class GradleScript(val kotlin: String, val groovy: String)

private class TestCaseBuilderImpl(
    val rootDir: File,
    description: String,
    private val state: CommonBuilderState = CommonBuilderState(description)
) : ProjectBuilderImpl<TestCaseBuilder>(state.rootProject), TestCaseBuilder {

    override fun languages(vararg languages: GradleScriptLanguage) = also {
        state.languages += languages
    }

    override fun engines(vararg engines: CoverageEngine) = also {
        state.engines += engines
    }

    override fun types(vararg types: ProjectType) = also {
        state.types += types
    }

    override fun withLocalCache(): TestCaseBuilder = also {
        state.localCache = true
    }

    override fun configKover(config: KoverRootConfig.() -> Unit) = also {
        state.koverConfig.config()
    }

    override fun subproject(name: String, builder: ProjectBuilder<*>.() -> Unit) = also {
        val projectState = state.subprojects.computeIfAbsent(name) { ProjectBuilderState() }
        @Suppress("UPPER_BOUND_VIOLATED_WARNING")
        ProjectBuilderImpl<ProjectBuilderImpl<*>>(projectState).builder()
    }

    override fun build(): GradleRunner {
        if (state.languages.isEmpty()) {
            state.languages += GradleScriptLanguage.KOTLIN
        }
        if (state.types.isEmpty()) {
            state.types += ProjectType.KOTLIN_JVM
        }
        if (state.engines.isEmpty()) {
            state.engines += null
        }
        if (state.pluginVersion == null) {
            state.pluginVersion = "0.5.0"
        }

        val projects: MutableMap<ProjectSlice, File> = mutableMapOf()

        state.languages.forEach { language ->
            state.types.forEach { type ->
                state.engines.forEach { engine ->
                    val slice = ProjectSlice(language, type, engine ?: CoverageEngine.INTELLIJ)
                    projects[slice] = state.createProject(rootDir, slice)
                }
            }
        }

        return GradleRunnerImpl(projects)
    }

}


@Suppress("UNCHECKED_CAST")
private open class ProjectBuilderImpl<B : ProjectBuilder<B>>(val projectState: ProjectBuilderState) : ProjectBuilder<B> {

    override fun verification(rules: Iterable<VerificationRule>): B {
        projectState.rules += rules
        return this as B
    }

    override fun configTest(script: String): B {
        projectState.testScripts += GradleScript(script, script)
        return this as B
    }

    override fun configTest(kotlin: String, groovy: String): B {
        projectState.testScripts += GradleScript(kotlin, groovy)
        return this as B
    }

    override fun config(script: String): B {
        projectState.scripts += GradleScript(script, script)
        return this as B
    }

    override fun config(kotlin: String, groovy: String): B {
        projectState.scripts += GradleScript(kotlin, groovy)
        return this as B
    }

    override fun dependency(script: String): B {
        projectState.dependencies += GradleScript(script, script)
        return this as B
    }

    override fun dependency(kotlin: String, groovy: String): B {
        projectState.dependencies += GradleScript(kotlin, groovy)
        return this as B
    }

    override fun sources(template: String): B {
        projectState.sourceTemplates += template
        return this as B
    }
}



