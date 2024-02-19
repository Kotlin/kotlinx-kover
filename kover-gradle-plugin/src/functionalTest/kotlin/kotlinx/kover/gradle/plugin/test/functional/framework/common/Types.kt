/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.common

import kotlinx.kover.gradle.plugin.commons.*
import java.io.File

internal enum class ScriptLanguage { KTS, GROOVY }


internal fun File.extractScriptLanguage(): ScriptLanguage {
    return when(name) {
        "build.gradle.kts" -> ScriptLanguage.KTS
        "build.gradle" -> ScriptLanguage.GROOVY
        else -> throw Exception("File $name is not a Gradle build script file")
    }
}

internal val BuildSlice.scriptExtension get() = if (language == ScriptLanguage.KTS) "gradle.kts" else "gradle"


internal val BuildSlice.mainPath: String
    get() {
        return when (type) {
            KotlinPluginType.JVM -> "src/main"
            KotlinPluginType.MULTIPLATFORM -> "src/jvmMain"
            KotlinPluginType.ANDROID -> "src/jvmMain"
        }
    }

internal val BuildSlice.testPath: String
    get() {
        return when (type) {
            KotlinPluginType.JVM -> "src/test"
            KotlinPluginType.MULTIPLATFORM -> "src/jvmTest"
            KotlinPluginType.ANDROID -> "src/jvmTest"
        }
    }




internal data class BuildSlice(
    val language: ScriptLanguage,
    val type: KotlinPluginType,
    val toolVendor: CoverageToolVendor?
) {
    override fun toString(): String {
        val languageText = when (language) {
            ScriptLanguage.KTS -> "Kotlin"
            ScriptLanguage.GROOVY -> "Groovy"
        }
        val typeText = when (type) {
            KotlinPluginType.JVM -> "K/JVM"
            KotlinPluginType.MULTIPLATFORM -> "K/MPP"
            KotlinPluginType.ANDROID -> "Android"
        }
        val toolText = when(toolVendor) {
            CoverageToolVendor.KOVER -> "Kover"
            CoverageToolVendor.JACOCO -> "JaCoCo"
            null -> "Default"
        }
        return "language=$languageText, type=$typeText, tool=$toolText"
    }
}

internal val File.uri: String get() = "file://${this.canonicalPath}"



