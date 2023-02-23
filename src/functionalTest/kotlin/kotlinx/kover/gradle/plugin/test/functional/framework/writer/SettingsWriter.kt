/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.writer

import kotlinx.kover.gradle.plugin.test.functional.framework.common.*
import kotlinx.kover.gradle.plugin.test.functional.framework.common.BuildSlice
import kotlinx.kover.gradle.plugin.test.functional.framework.common.localRepositoryPath
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.TestBuildConfig
import java.io.*

internal fun FormattedWriter.writePluginManagement(language: ScriptLanguage) {
    call("resolutionStrategy") {
        call("eachPlugin") {
            line("if (requested.id.id == \"org.jetbrains.kotlinx.kover\") { useVersion(\"$koverVersion\") }")
            if (overriddenTestKotlinVersion != null) {
                line("if (requested.id.id == \"org.jetbrains.kotlin.jvm\") useVersion(\"$overriddenTestKotlinVersion\")")
                line("if (requested.id.id == \"org.jetbrains.kotlin.multiplatform\") useVersion(\"$overriddenTestKotlinVersion\")")
                line("if (requested.id.id == \"org.jetbrains.kotlin.android\") useVersion(\"$overriddenTestKotlinVersion\")")
            }
        }
    }

    call("repositories") {
        line("maven { url=${localRepositoryPath.uriForScript(language)} }")
        line("gradlePluginPortal()")
        line("mavenCentral()")
    }
}


internal fun File.writeSettings(build: TestBuildConfig, slice: BuildSlice) {
    writeScript {
        call("pluginManagement") {
            writePluginManagement(slice.language)
        }
        line("")
        line("""rootProject.name = "kover-functional-test"""")
        build.projects.keys.forEach { path ->
            if (path != ":") {
                line("""include("$path")""")
            }
        }
        if (build.useLocalCache) {
            call("buildCache") {
                call("local") {
                    line("""directory = "${"$"}settingsDir/build-cache"""")
                }
            }
        }
    }
}
