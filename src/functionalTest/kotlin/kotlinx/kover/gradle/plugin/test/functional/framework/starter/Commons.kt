/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.starter

import kotlinx.kover.gradle.plugin.test.functional.framework.common.*
import kotlinx.kover.gradle.plugin.test.functional.framework.writer.*
import java.io.*

/**
 * Override Kover version and add local repository to find artifact for current build.
 */
@Suppress("UNUSED_PARAMETER")
internal fun File.patchSettingsFile(description: String) {
    val settingsFile = (listFiles()?.firstOrNull { it.name == "settings.gradle" || it.name == "settings.gradle.kts" }
        ?: throw Exception("No Gradle settings file in project ${this.canonicalPath}"))
    val language = if (settingsFile.name.endsWith(".kts")) ScriptLanguage.KOTLIN else ScriptLanguage.GROOVY

    val originLines = settingsFile.readLines()

    settingsFile.bufferedWriter().use { writer ->

        originLines.forEach { line ->
            writer.appendLine(line)
            if (line.trimStart().startsWith("pluginManagement")) {
                val additionalManagement = pluginManagement(language)
                additionalManagement.forEach {
                    writer.appendLine(it)
                }
            }
        }

    }
}


private fun pluginManagement(language: ScriptLanguage): List<String> {
    return """
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.jetbrains.kotlinx.kover") {
                useVersion("$koverVersion")
            }
        }
    }
    repositories {
        maven { url=${localRepositoryPath.uriForScript(language)} }
    }
""".lines()
}
