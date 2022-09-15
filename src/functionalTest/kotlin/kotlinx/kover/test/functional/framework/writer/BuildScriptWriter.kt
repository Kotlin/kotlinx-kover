/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.writer

import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.configurator.*
import java.io.*

internal fun File.writeBuildScript(projectConfig: TestProjectConfig, slice: BuildSlice) {
    this.writeScript(slice.language, slice.type, slice.engine) {
        writePlugins(projectConfig.plugins)
        writeRepositories(projectConfig.repositories)
        writeDependencies(projectConfig.projectDependencies)
        writeKover(projectConfig.kover)
        writeKoverMerged(projectConfig.merged)
        writeTestTasks(projectConfig.testTasks)
    }
}

private fun FormattedScriptAppender.writePlugins(plugins: TestPluginsConfig) {
    block("plugins", plugins.useKotlin || plugins.useKover) {
        lineIf(plugins.useKotlin) {
            val name = when {
                language == ScriptLanguage.GROOVY && type == KotlinPluginType.JVM -> """id "org.jetbrains.kotlin.jvm""""
                language == ScriptLanguage.GROOVY && type == KotlinPluginType.MULTIPLATFORM -> """id "org.jetbrains.kotlin.multiplatform""""
                language == ScriptLanguage.KOTLIN && type == KotlinPluginType.JVM -> """kotlin("jvm")"""
                language == ScriptLanguage.KOTLIN && type == KotlinPluginType.MULTIPLATFORM -> """kotlin("multiplatform")"""
                else -> throw Exception("Unsupported test combination: language $language and project type $type")
            }
            val version = plugins.kotlinVersion?.let { """version "$it"""" } ?: ""
            "$name $version"
        }

        lineIf(plugins.useKover) {
            val name = if (language == ScriptLanguage.KOTLIN) {
                """id("org.jetbrains.kotlinx.kover")"""
            } else {
                """id "org.jetbrains.kotlinx.kover""""
            }
            val version = plugins.koverVersion?.let { """version "$it"""" } ?: ""
            "$name $version"
        }
    }
}

private fun FormattedScriptAppender.writeRepositories(repositories: List<String>) {
    blockForEach(repositories, "repositories", repositories.isNotEmpty()) { repository ->
        line(repository)
    }
}

private fun FormattedScriptAppender.writeDependencies(projectDependencies: List<String>) {
    val subprojectsPart = projectDependencies.joinToString(separator = "\n") {
        if (language == ScriptLanguage.KOTLIN) "implementation(project(\"$it\"))" else "implementation project('$it')"
    }

    val template = when {
        language == ScriptLanguage.GROOVY && type == KotlinPluginType.JVM -> GROOVY_JVM_DEPS
        language == ScriptLanguage.GROOVY && type == KotlinPluginType.MULTIPLATFORM -> GROOVY_KMP_DEPS
        language == ScriptLanguage.KOTLIN && type == KotlinPluginType.JVM -> KOTLIN_JVM_DEPS
        language == ScriptLanguage.KOTLIN && type == KotlinPluginType.MULTIPLATFORM -> KOTLIN_KMP_DEPS
        else -> throw Exception("Unsupported test combination: language $language and project type $type")
    }
    template.replace(DEPS_PLACEHOLDER, subprojectsPart).lines().forEach {
        line(it)
    }
}

private fun FormattedScriptAppender.writeTestTasks(state: TestTaskConfig) {
    val testTaskName = when {
        type == KotlinPluginType.JVM -> "test"
        type == KotlinPluginType.MULTIPLATFORM && language == ScriptLanguage.KOTLIN -> "named(\"jvmTest\").configure"
        type == KotlinPluginType.MULTIPLATFORM -> "jvmTest"
        else -> throw Exception("Project with type $type and language $language not supported for test task configuring")
    }

    block("tasks.$testTaskName", state.excludes != null || state.includes != null) {
        val extension =
            if (language == ScriptLanguage.KOTLIN) "extensions.configure(kotlinx.kover.api.KoverTaskExtension::class)" else "kover"
        block(extension) {
            lineIf(state.excludes != null) {
                val excludesString = state.excludes!!.joinToString(separator = ",") { "\"$it\"" }
                "excludes.addAll($excludesString)"
            }
            lineIf(state.includes != null) {
                val includesString = state.includes!!.joinToString(separator = ",") { "\"$it\"" }
                "includes.addAll($includesString)"
            }
        }
    }
}


private const val DEPS_PLACEHOLDER = "/*DEPS*/"

private const val KOTLIN_JVM_DEPS = """
dependencies {
    $DEPS_PLACEHOLDER
    testImplementation(kotlin("test"))
}
"""

private const val KOTLIN_KMP_DEPS = """
kotlin {
    jvm() {
        withJava()
    }
    dependencies {
        commonTestImplementation(kotlin("test"))
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                $DEPS_PLACEHOLDER
            }
        }
    }
}
"""

private const val GROOVY_JVM_DEPS = """
dependencies {
    $DEPS_PLACEHOLDER
    testImplementation 'org.jetbrains.kotlin:kotlin-test'
}
"""

private const val GROOVY_KMP_DEPS = """
kotlin {
    jvm() {
        withJava()
    }
    dependencies {
        commonTestImplementation 'org.jetbrains.kotlin:kotlin-test'
    }
    sourceSets {
        jvmMain {
            dependencies {
                $DEPS_PLACEHOLDER
            }
        }
    }
}
"""
