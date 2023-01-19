/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.writer

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.configurator.*
import java.io.*

internal fun File.writeBuildScript(projectConfig: TestProjectConfigurator, slice: BuildSlice) {
    this.writeScript {
        writePlugins(projectConfig.plugins, slice)
        writeRepositories(projectConfig.repositories)
        writeDependencies(projectConfig.projectDependencies, slice)
        writeBlocks(projectConfig.rawBlocks)
        writeOverriddenTool(slice)
    }
}

private fun FormattedWriter.writePlugins(plugins: PluginsConfiguratorImpl, slice: BuildSlice) {
    if (!plugins.useKotlin && !plugins.useKover) return

    val (language, type) = slice

    call("plugins") {
        if (plugins.useKotlin) {
            val name = when {
                language == ScriptLanguage.GROOVY && type == KotlinPluginType.JVM -> """id "org.jetbrains.kotlin.jvm""""
                language == ScriptLanguage.GROOVY && type == KotlinPluginType.MULTI_PLATFORM -> """id "org.jetbrains.kotlin.multiplatform""""
                language == ScriptLanguage.KOTLIN && type == KotlinPluginType.JVM -> """kotlin("jvm")"""
                language == ScriptLanguage.KOTLIN && type == KotlinPluginType.MULTI_PLATFORM -> """kotlin("multiplatform")"""
                else -> throw Exception("Unsupported test combination: language $language and project type $type")
            }
            val version = plugins.kotlinVersion?.let { """version "$it"""" } ?: ""
            line("$name $version")
        }

        if (plugins.useKover) {
            val name = if (language == ScriptLanguage.KOTLIN) {
                """id("org.jetbrains.kotlinx.kover")"""
            } else {
                """id "org.jetbrains.kotlinx.kover""""
            }
            val version = plugins.koverVersion?.let { """version "$it"""" } ?: ""
            line("$name $version")
        }
    }
}

private fun FormattedWriter.writeRepositories(repositories: List<String>) {
    if (repositories.isEmpty()) return

    call("repositories") {
        repositories.forEach { line(it) }
    }
}

private fun FormattedWriter.writeDependencies(koverDependencies: List<String>, slice: BuildSlice) {
    val (language, type) = slice

    val subprojectsPart = koverDependencies.joinToString(separator = "\n") {
        "implementation(project(\"$it\"))"
    }

    val template = when {
        language == ScriptLanguage.GROOVY && type == KotlinPluginType.JVM -> GROOVY_JVM_DEPS
        language == ScriptLanguage.GROOVY && type == KotlinPluginType.MULTI_PLATFORM -> GROOVY_KMP_DEPS
        language == ScriptLanguage.KOTLIN && type == KotlinPluginType.JVM -> KOTLIN_JVM_DEPS
        language == ScriptLanguage.KOTLIN && type == KotlinPluginType.MULTI_PLATFORM -> KOTLIN_KMP_DEPS
        else -> throw Exception("Unsupported test combination: language $language and project type $type")
    }
    template.replace(DEPS_PLACEHOLDER, subprojectsPart).lines().forEach {
        line(it)
    }

    if (koverDependencies.isNotEmpty()) {
        call("dependencies") {
            koverDependencies.forEach {
                line("kover(project(\"$it\"))")
            }
        }
    }
}

private fun FormattedWriter.writeBlocks(rawBlocks: List<String>) {
    rawBlocks.forEach {
        text(it)
    }
}

private fun FormattedWriter.writeOverriddenTool(slice: BuildSlice) {
    val vendor = slice.toolVendor ?: return

    call("kover") {
        val koverWriter = KoverWriter(this)
        when (vendor) {
            CoverageToolVendor.KOVER -> koverWriter.useKoverToolDefault()
            CoverageToolVendor.JACOCO -> koverWriter.useJacocoToolDefault()
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
