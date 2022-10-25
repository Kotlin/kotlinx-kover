/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.starter

import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.common.koverVersion
import kotlinx.kover.test.functional.framework.writer.*
import java.io.*

/**
 * Override Kover version and add local repository to find artifact for current build.
 */
internal fun File.patchSettingsFile(description: String) {
    val settingsFile = (listFiles()?.firstOrNull { it.name == "settings.gradle" || it.name == "settings.gradle.kts" }
        ?: throw Exception("No Gradle settings file in project ${this.canonicalPath}"))
    val language = if (settingsFile.name.endsWith(".kts")) ScriptLanguage.KOTLIN else ScriptLanguage.GROOVY

    val content = settingsFile.readText()
    if (content.contains("pluginManagement")) {
        throw Exception("Illegal usage of 'pluginManagement' in Gradle settings file for $description\nPlease remove its use for functional tests")
    }
    val extendedContent = pluginManagement(language) + content
    settingsFile.writeText(extendedContent)
}


private fun pluginManagement(language: ScriptLanguage): String {
    return """
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.jetbrains.kotlinx.kover") {
                useVersion("$koverVersion")
            }
        }
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url=${localRepositoryPath.asUri(language)} }
        google()
    }
}
"""
}
