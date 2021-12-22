package kotlinx.kover.test.functional.core

import kotlinx.kover.api.*
import java.io.*

internal fun createBuilder(rootDir: File, description: String): ProjectBuilder {
    return ProjectBuilderImpl(rootDir, description)
}

internal class ProjectBuilderState(val description: String) {
    var pluginVersion: String? = null
    val languages: MutableSet<GradleScriptLanguage> = mutableSetOf()
    val types: MutableSet<ProjectType> = mutableSetOf()
    val engines: MutableSet<CoverageEngine?> = mutableSetOf()
    val koverConfig: KoverRootConfig = KoverRootConfig()
    val rootModule: ModuleBuilderState = ModuleBuilderState()
    val submodules: MutableMap<String, ModuleBuilderState> = mutableMapOf()
}

internal class ModuleBuilderState {
    val sourceTemplates: MutableList<String> = mutableListOf()
    val scripts: MutableList<GradleScript> = mutableListOf()
    val testScripts: MutableList<GradleScript> = mutableListOf()
    val dependencies: MutableList<GradleScript> = mutableListOf()
    val rules: MutableList<VerificationRule> = mutableListOf()
    val mainSources: MutableMap<String, String> = mutableMapOf()
    val testSources: MutableMap<String, String> = mutableMapOf()
}

internal data class GradleScript(val kotlin: String, val groovy: String)

private class ProjectBuilderImpl(
    val rootDir: File,
    description: String,
    private val state: ProjectBuilderState = ProjectBuilderState(description)
) : ModuleBuilderImpl<ProjectBuilder>(state.rootModule), ProjectBuilder {

    override fun languages(vararg languages: GradleScriptLanguage) = also {
        state.languages += languages
    }

    override fun engines(vararg engines: CoverageEngine) = also {
        state.engines += engines
    }

    override fun types(vararg types: ProjectType) = also {
        state.types += types
    }

    override fun configKover(config: KoverRootConfig.() -> Unit) = also {
        state.koverConfig.config()
    }

    override fun submodule(name: String, builder: ModuleBuilder<*>.() -> Unit) = also {
        val moduleState = state.submodules.computeIfAbsent(name) { ModuleBuilderState() }
        @Suppress("UPPER_BOUND_VIOLATED_WARNING")
        ModuleBuilderImpl<ModuleBuilderImpl<*>>(moduleState).builder()
    }

    override fun build(): ProjectRunner {
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
            state.pluginVersion = "0.4.4" // TODO read from properties
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

        return ProjectRunnerImpl(projects)
    }

}


@Suppress("UNCHECKED_CAST")
private open class ModuleBuilderImpl<B : ModuleBuilder<B>>(val moduleState: ModuleBuilderState) : ModuleBuilder<B> {

    override fun verification(rules: Iterable<VerificationRule>): B {
        moduleState.rules += rules
        return this as B
    }

    override fun configTest(script: String): B {
        moduleState.testScripts += GradleScript(script, script)
        return this as B
    }

    override fun configTest(kotlin: String, groovy: String): B {
        moduleState.testScripts += GradleScript(kotlin, groovy)
        return this as B
    }

    override fun config(script: String): B {
        moduleState.scripts += GradleScript(script, script)
        return this as B
    }

    override fun config(kotlin: String, groovy: String): B {
        moduleState.testScripts += GradleScript(kotlin, groovy)
        return this as B
    }

    override fun dependency(script: String): B {
        moduleState.dependencies += GradleScript(script, script)
        return this as B
    }

    override fun dependency(kotlin: String, groovy: String): B {
        moduleState.dependencies += GradleScript(kotlin, groovy)
        return this as B
    }

    override fun sources(template: String): B {
        moduleState.sourceTemplates += template
        return this as B
    }
}



