package kotlinx.kover.test.functional.core

import kotlinx.kover.api.*
import java.io.*

private const val TEMPLATES_PATH = "src/functionalTest/templates"
private const val BUILD_SCRIPTS_PATH = "$TEMPLATES_PATH/scripts/buildscripts"
private const val SETTINGS_PATH = "$TEMPLATES_PATH/scripts/settings"
private const val SOURCES_PATH = "$TEMPLATES_PATH/sources"

internal fun ProjectBuilderState.createProject(rootDir: File, slice: ProjectSlice): File {
    val projectDir = File(rootDir, slice.encodedString()).also { it.mkdirs() }

    val extension = slice.scriptExtension

    val buildScript = loadScriptTemplate(true, slice)
        .processRootBuildScript(this, slice)
        .processModuleBuildScript(rootModule, slice)

    val settings = buildSettings(slice)

    File(projectDir, "build.$extension").writeText(buildScript)
    File(projectDir, "settings.$extension").writeText(settings)

    rootModule.writeSources(projectDir, slice)

    return projectDir
}


private val ProjectSlice.scriptExtension get() = if (language == GradleScriptLanguage.KOTLIN) "gradle.kts" else "gradle"

private val ProjectSlice.srcPath: String
    get() {
        return when (type) {
            ProjectType.KOTLIN_JVM -> "src/main"
            ProjectType.KOTLIN_MULTIPLATFORM -> "src/jvmMain"
            ProjectType.ANDROID -> "src/jvmMain"
        }
    }

private val ProjectSlice.testPath: String
    get() {
        return when (type) {
            ProjectType.KOTLIN_JVM -> "src/test"
            ProjectType.KOTLIN_MULTIPLATFORM -> "src/jvmTest"
            ProjectType.ANDROID -> "src/jvmTest"
        }
    }


private fun String.processRootBuildScript(state: ProjectBuilderState, slice: ProjectSlice): String {
    return replace("//PLUGIN_VERSION", state.pluginVersion!!)
        .replace("//KOVER", state.buildRootExtension(slice))
}

private fun String.processModuleBuildScript(state: ModuleBuilderState, slice: ProjectSlice): String {
    return replace("//REPOSITORIES", "")
        .replace("//DEPENDENCIES", "")
        .replace("//SCRIPTS", state.buildScripts(slice))
        .replace("//TEST_TASK", state.buildTestTask(slice))
        .replace("//VERIFICATIONS", state.buildVerifications(slice))
}


private fun ModuleBuilderState.writeSources(projectDir: File, slice: ProjectSlice) {
    fun File.processDir(result: MutableMap<String, String>, targetRootPath: String, relativePath: String = "") {
        listFiles()?.forEach { file ->
            val filePath = "$relativePath/${file.name}"
            if (file.isDirectory) {
                file.processDir(result, targetRootPath, filePath)
            } else if (file.exists() && file.length() > 0) {
                val targetFile = File(projectDir, "$targetRootPath/$filePath")
                targetFile.parentFile.mkdirs()
                file.copyTo(targetFile)
            }
        }
    }

    val srcPath = slice.srcPath
    val testPath = slice.testPath

    sourceTemplates.forEach { template ->
        File(SOURCES_PATH, "$template/main").processDir(mainSources, srcPath)
        File(SOURCES_PATH, "$template/test").processDir(testSources, testPath)
    }
}

private fun ProjectSlice.scriptPath(): String {
    val languageString = if (language == GradleScriptLanguage.KOTLIN) "kotlin" else "groovy"
    val typeString = when (type) {
        ProjectType.KOTLIN_JVM -> "kjvm"
        ProjectType.KOTLIN_MULTIPLATFORM -> "kmp"
        ProjectType.ANDROID -> "android"
    }
    return "$BUILD_SCRIPTS_PATH/$languageString/$typeString"
}

private fun buildSubmodulesIncludes(submodules: Set<String>): String {
    if (submodules.isEmpty()) return ""

    return submodules.joinToString("\n", "\n", "\n") {
        """include("$it")"""
    }
}

private fun ProjectBuilderState.buildRootExtension(slice: ProjectSlice): String {
    if (slice.engine == null && koverConfig.isDefault) {
        return ""
    }

    val builder = StringBuilder()
    builder.appendLine()
    builder.appendLine("kover {")

    if (koverConfig.disabled != null) {
        val property = if (slice.language == GradleScriptLanguage.KOTLIN) "isEnabled" else "enabled"
        builder.appendLine("$property = ${koverConfig.disabled == false}")
    }

    if (slice.engine == CoverageEngine.INTELLIJ) {
        builder.appendLine("    coverageEngine.set(kotlinx.kover.api.CoverageEngine.INTELLIJ)")
        if (koverConfig.intellijVersion != null) {
            builder.appendLine("""    intellijEngineVersion.set("${koverConfig.intellijVersion}")""")
        }
    }
    if (slice.engine == CoverageEngine.JACOCO) {
        builder.appendLine("    coverageEngine.set(kotlinx.kover.api.CoverageEngine.JACOCO)")
        if (koverConfig.jacocoVersion != null) {
            builder.appendLine("""    jacocoEngineVersion.set("${koverConfig.jacocoVersion}")""")
        }
    }

    builder.appendLine("}")

    return builder.toString()
}

@Suppress("UNUSED_PARAMETER")
private fun ModuleBuilderState.buildTestTask(slice: ProjectSlice): String {
    val configs = if (slice.language == GradleScriptLanguage.KOTLIN) testKotlinScripts else testGroovyScripts

    if (configs.isEmpty()) {
        return ""
    }

    return loadTestTaskTemplate(slice).replace("//KOVER_TEST_CONFIG", configs.joinToString("\n"))
}

@Suppress("UNUSED_PARAMETER")
private fun ModuleBuilderState.buildVerifications(slice: ProjectSlice): String {
    return ""
}

private fun ProjectBuilderState.buildSettings(slice: ProjectSlice): String {
    return loadSettingsTemplate(slice)
        .replace("//SUBMODULES", buildSubmodulesIncludes(submodules.keys))
}

private fun ModuleBuilderState.buildScripts(slice: ProjectSlice): String {
    val scripts = if (slice.language == GradleScriptLanguage.KOTLIN) kotlinScripts else groovyScripts

    return if (scripts.isNotEmpty()) {
        scripts.joinToString("\n", "\n", "\n")
    } else {
        ""
    }
}


private fun loadSettingsTemplate(slice: ProjectSlice): String {
    return File("$SETTINGS_PATH/settings.${slice.scriptExtension}").readText()
}

private fun loadScriptTemplate(root: Boolean, slice: ProjectSlice): String {
    val filename = if (root) "root" else "child"
    return File("${slice.scriptPath()}/$filename").readText()
}

private fun loadTestTaskTemplate(slice: ProjectSlice): String {
    return File("${slice.scriptPath()}/testTask").readText()
}
