package kotlinx.kover.test.functional.core

import kotlinx.kover.api.*
import java.io.*

internal fun createBuilder(rootDir: File): ProjectBuilder {
    return ProjectBuilderImpl(rootDir)
}

internal class ProjectBuilderState {
    var description: String? = null
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
    val kotlinScripts: MutableList<String> = mutableListOf()
    val groovyScripts: MutableList<String> = mutableListOf()
    val testKotlinScripts: MutableList<String> = mutableListOf()
    val testGroovyScripts: MutableList<String> = mutableListOf()
    val rules: MutableList<VerificationRule> = mutableListOf()
    val mainSources: MutableMap<String, String> = mutableMapOf()
    val testSources: MutableMap<String, String> = mutableMapOf()
}

private class ProjectBuilderImpl(
    val rootDir: File,
    private val state: ProjectBuilderState = ProjectBuilderState()
) : ModuleBuilderImpl<ProjectBuilder>(state.rootModule), ProjectBuilder {

    override fun case(description: String) = also {
        state.description = description
    }

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
        moduleState.testKotlinScripts += script
        moduleState.testGroovyScripts += script
        return this as B
    }

    override fun configTest(kotlin: String, groovy: String): B {
        moduleState.testKotlinScripts += kotlin
        moduleState.testGroovyScripts += groovy
        return this as B
    }

    override fun config(script: String): B {
        moduleState.kotlinScripts += script
        moduleState.groovyScripts += script
        return this as B
    }

    override fun config(kotlin: String, groovy: String): B {
        moduleState.kotlinScripts += kotlin
        moduleState.groovyScripts += groovy
        return this as B
    }

    override fun sources(template: String): B {
        moduleState.sourceTemplates += template
        return this as B
    }
}



