/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.common

import kotlinx.kover.api.*
import java.io.File

internal enum class ScriptLanguage { KOTLIN, GROOVY }

internal enum class KotlinPluginType { JVM, MULTIPLATFORM, ANDROID }


internal fun File.extractScriptLanguage(): ScriptLanguage {
    return when(name) {
        "build.gradle.kts" -> ScriptLanguage.KOTLIN
        "build.gradle" -> ScriptLanguage.GROOVY
        else -> throw Exception("File $name is not a Gradle build script file")
    }
}

internal val BuildSlice.scriptExtension get() = if (language == ScriptLanguage.KOTLIN) "gradle.kts" else "gradle"


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
    val engine: CoverageEngineVendor?
) {
    override fun toString(): String {
        val languageText = when (language) {
            ScriptLanguage.KOTLIN -> "Kotlin"
            ScriptLanguage.GROOVY -> "Groovy"
        }
        val typeText = when (type) {
            KotlinPluginType.JVM -> "K/JVM"
            KotlinPluginType.MULTIPLATFORM -> "KMP"
            KotlinPluginType.ANDROID -> "Android"
        }
        val engineText = when(engine) {
            CoverageEngineVendor.INTELLIJ -> "IntelliJ"
            CoverageEngineVendor.JACOCO -> "JaCoCo"
            null -> "Default"
        }
        return "language=$languageText, type=$typeText, engine=$engineText"
    }
}


internal const val SAMPLES_SOURCES_PATH = "src/functionalTest/templates/sources"

internal fun File.sub(relativePath: String): File = File(this, relativePath)
internal val File.uri: String get() = "file://${this.canonicalPath}"



