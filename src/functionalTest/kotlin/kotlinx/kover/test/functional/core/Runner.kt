package kotlinx.kover.test.functional.core

import kotlinx.kover.api.*
import org.gradle.testkit.runner.*
import java.io.*


internal fun createRunner(rootDir: File): ProjectRunner {
    return ProjectRunnerImpl(rootDir)
}

fun GradleRunner.addPluginTestRuntimeClasspath() = apply {
    val classpathFile = File(System.getProperty("plugin-classpath"))
    if (!classpathFile.exists()) {
        throw IllegalStateException("Could not find classpath resource")
    }

    val pluginClasspath = pluginClasspath + classpathFile.readLines().map { File(it) }
    withPluginClasspath(pluginClasspath)
}

private const val TEMPLATES_PATH = "src/functionalTest/templates"
private const val BUILD_SCRIPTS_PATH = "$TEMPLATES_PATH/scripts/buildscripts"
private const val SETTINGS_PATH = "$TEMPLATES_PATH/scripts/settings"
private const val SOURCES_PATH = "$TEMPLATES_PATH/sources"

private class ProjectRunnerImpl(val rootDir: File) : ProjectRunner {
    private var initialized: Boolean = false

    private var description: String? = null
    private var pluginVersion: String? = null

    private var intellijVersion: String? = null
    private var jacocoVersion: String? = null
    private var pluginEnabled: Boolean? = null

    private val languages: MutableSet<GradleScriptLanguage> = mutableSetOf()
    private val types: MutableSet<ProjectType> = mutableSetOf()
    private val engines: MutableSet<CoverageEngine?> = mutableSetOf()


    private val koverConfigs: MutableList<String> = mutableListOf()
    private val kotlinScripts: MutableList<String> = mutableListOf()
    private val groovyScripts: MutableList<String> = mutableListOf()
    private val rules: MutableList<VerificationRule> = mutableListOf()
    private val mainSources: MutableMap<String, String> = mutableMapOf()
    private val testSources: MutableMap<String, String> = mutableMapOf()
    private val submodules: MutableMap<String, SubmoduleBuilderImpl> = mutableMapOf()

    override fun case(description: String): ProjectRunnerImpl {
        this.description = description
        return this
    }

    override fun languages(vararg languages: GradleScriptLanguage): ProjectRunnerImpl {
        this.languages += languages
        return this
    }

    override fun engines(vararg engines: CoverageEngine): ProjectRunnerImpl {
        this.engines += engines
        return this
    }

    override fun types(vararg types: ProjectType): ProjectRunnerImpl {
        this.types += types
        return this
    }

    override fun setIntellijVersion(version: String): ProjectRunnerImpl {
        this.intellijVersion = version
        return this
    }

    override fun setJacocoVersion(version: String): ProjectRunnerImpl {
        this.jacocoVersion = version
        return this
    }

    override fun submodule(name: String, builder: ModuleBuilder<*>.() -> Unit) {
        submodules[name] = SubmoduleBuilderImpl().apply(builder)
    }

    override fun kover(rootExtensionScript: String) {
        koverConfigs += rootExtensionScript
    }

    override fun sources(template: String): ProjectRunner {
        fun File.processDir(result: MutableMap<String, String>, path: String = "") {
            listFiles()?.forEach { file ->
                val filePath = "$path/${file.name}"
                if (file.isDirectory) {
                    file.processDir(result, filePath)
                } else if (file.exists() && file.length() > 0) {
                    result += filePath to file.readText()
                }
            }
        }

        File(SOURCES_PATH, "$template/main").processDir(mainSources)
        File(SOURCES_PATH, "$template/test").processDir(testSources)
        return this
    }

    override fun verification(rules: Iterable<VerificationRule>): ProjectRunner {
        this.rules += rules
        return this
    }

    override fun config(script: String): ProjectRunner {
        kotlinScripts += script
        groovyScripts += script
        return this
    }

    override fun config(kotlin: String, groovy: String): ProjectRunner {
        kotlinScripts += kotlin
        groovyScripts += groovy
        return this
    }

    override fun check(vararg args: String, block: RunResult.() -> Unit) {
        setDefaults()
        initialized = true

        languages.forEach { language ->
            types.forEach { type ->
                engines.forEach { engine ->
                    createProject(language, type, engine).run(listOf(*args), block)
                }
            }
        }
    }

    private fun setDefaults() {
        if (languages.isEmpty()) {
            languages += GradleScriptLanguage.KOTLIN
        }
        if (types.isEmpty()) {
            types += ProjectType.KOTLIN_JVM
        }
        if (engines.isEmpty()) {
            engines += null
        }
        if (pluginVersion == null) {
            pluginVersion = "0.4.2" // TODO read from properties
        }
    }

    private fun File.run(args: List<String>, block: RunResult.() -> Unit) {
        val buildResult = GradleRunner.create()
            .withProjectDir(this)
            .withPluginClasspath()
            .addPluginTestRuntimeClasspath()
            .withArguments(args)
            .build()

        RunResult(buildResult, this).apply(block)
    }

    private fun createProject(language: GradleScriptLanguage, type: ProjectType, engine: CoverageEngine?): File {
        val projectDir = File(rootDir, System.currentTimeMillis().toString()).also { it.mkdirs() }

        val extension = if (language == GradleScriptLanguage.KOTLIN) "gradle.kts" else "gradle"

        File(projectDir, "build.$extension").writeText(
            scriptTemplate(true, language, type)
                .replace("//PLUGIN_VERSION", pluginVersion!!)
                .replace("//REPOSITORIES", "")
                .replace("//DEPENDENCIES", "")
                .replace("//KOVER", buildRootExtension(language, engine))
                .replace("//SCRIPTS", buildScripts(language))
                .replace("//VERIFICATIONS", buildVerifications(language, rules))
        )
        File(projectDir, "settings.$extension").writeText(buildSettings(language))

        val srcDir: File
        val testDir: File
        when (type) {
            ProjectType.KOTLIN_JVM -> {
                srcDir = File(projectDir, "src/main")
                testDir = File(projectDir, "src/test")
            }
            ProjectType.KOTLIN_MULTIPLATFORM -> {
                srcDir = File(projectDir, "src/jvmMain")
                testDir = File(projectDir, "src/jvmTest")
            }
            ProjectType.ANDROID -> {
                srcDir = File(projectDir, "src/jvmMain")
                testDir = File(projectDir, "src/jvmTest")
            }
        }

        mainSources.forEach { (name, content) ->
            val srcFile = File(srcDir, name)
            srcFile.parentFile.mkdirs()
            srcFile.writeText(content)
        }
        testSources.forEach { (name, content) ->
            val srcFile = File(testDir, name)
            srcFile.parentFile.mkdirs()
            srcFile.writeText(content)
        }

        return projectDir
    }

    private fun buildRootExtension(language: GradleScriptLanguage, engine: CoverageEngine?): String {
        if (engine == null && koverConfigs.isEmpty() && pluginEnabled == null) {
            return ""
        }

        val builder = StringBuilder()
        builder.appendLine()
        builder.appendLine("kover {")

        if (pluginEnabled != null) {
            val property = if (language == GradleScriptLanguage.KOTLIN) "isEnabled" else "enabled"
            builder.appendLine("$property = $pluginEnabled")
        }

        if (engine == CoverageEngine.INTELLIJ) {
            builder.appendLine("    coverageEngine.set(kotlinx.kover.api.CoverageEngine.INTELLIJ)")
            if (intellijVersion != null) {
                builder.appendLine("""    intellijEngineVersion.set("$intellijVersion")""")
            }
        }
        if (engine == CoverageEngine.JACOCO) {
            builder.appendLine("    coverageEngine.set(kotlinx.kover.api.CoverageEngine.JACOCO)")
            if (jacocoVersion != null) {
                builder.appendLine("""    jacocoEngineVersion.set("$jacocoVersion")""")
            }
        }

        koverConfigs.forEach {
            builder.appendLine("    $it")
        }
        builder.appendLine("}")

        return builder.toString()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun buildVerifications(language: GradleScriptLanguage, verifications: List<VerificationRule>): String {
        return ""
    }

    private fun buildSettings(language: GradleScriptLanguage): String {
        return settingsTemplate(language)
            .replace("//SUBMODULES", buildSubmodulesIncludes(submodules.keys))
    }

    private fun buildScripts(language: GradleScriptLanguage): String {
        val scripts = if (language == GradleScriptLanguage.KOTLIN) kotlinScripts else groovyScripts

        return if (scripts.isNotEmpty()) {
            scripts.joinToString("\n", "\n", "\n")
        } else {
            ""
        }
    }
}

private class SubmoduleBuilderImpl : ModuleBuilder<SubmoduleBuilderImpl> {
    override fun verification(rules: Iterable<VerificationRule>): SubmoduleBuilderImpl {
        TODO("Not yet implemented")
    }

    override fun config(script: String): SubmoduleBuilderImpl {
        TODO("Not yet implemented")
    }

    override fun config(kotlin: String, groovy: String): SubmoduleBuilderImpl {
        TODO("Not yet implemented")
    }

    override fun sources(template: String): SubmoduleBuilderImpl {
        TODO("Not yet implemented")
    }

}

private fun buildSubmodulesIncludes(submodules: Set<String>): String {
    if (submodules.isEmpty()) return ""

    return submodules.joinToString("\n", "\n", "\n") {
        """include("$it")"""
    }
}


private fun scriptTemplate(root: Boolean, language: GradleScriptLanguage, type: ProjectType): String {
    val languageString = if (language == GradleScriptLanguage.KOTLIN) "kotlin" else "groovy"

    val typeString = when (type) {
        ProjectType.KOTLIN_JVM -> "kjvm"
        ProjectType.KOTLIN_MULTIPLATFORM -> "kmp"
        ProjectType.ANDROID -> "android"
    }
    val filename = if (root) "root" else "child"
    return File("$BUILD_SCRIPTS_PATH/$languageString/$typeString/$filename").readText()
}

private fun settingsTemplate(language: GradleScriptLanguage): String {
    val filename = if (language == GradleScriptLanguage.KOTLIN) "settings.gradle.kts" else "settings.gradle"
    return File("$SETTINGS_PATH/$filename").readText()
}

