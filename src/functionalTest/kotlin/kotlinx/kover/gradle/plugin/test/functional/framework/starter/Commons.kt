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
        var firstStatement = true
        originLines.forEach { line ->

            if (firstStatement && line.isNotBlank()) {
                val isPluginManagement = line.trimStart().startsWith("pluginManagement")

                writer.appendLine("pluginManagement {")

                val pluginManagementWriter = FormattedWriter { l -> writer.append(l) }
                pluginManagementWriter.writePluginManagement(language)

                if (!isPluginManagement) {
                    writer.appendLine("}")
                }

                firstStatement = false
            } else {
                writer.appendLine(line)
            }

        }

    }
}


