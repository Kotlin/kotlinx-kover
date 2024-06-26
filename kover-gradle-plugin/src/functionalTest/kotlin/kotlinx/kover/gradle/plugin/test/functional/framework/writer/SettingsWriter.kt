/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.writer

import kotlinx.kover.gradle.plugin.test.functional.framework.common.*
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.TestBuildConfig
import java.io.*

internal fun FormattedWriter.writePluginManagement(language: ScriptLanguage,
                                                   koverVersion: String,
                                                   snapshotRepos: List<String>,
                                                   overrideKotlinVersion: String?) {
    call("resolutionStrategy") {
        call("eachPlugin") {
            line("if (requested.id.id == \"org.jetbrains.kotlinx.kover\") { useVersion(\"$koverVersion\") }")
            line("if (requested.id.id == \"org.jetbrains.kotlinx.kover.aggregation\") { useVersion(\"$koverVersion\") }")
            if (overrideKotlinVersion != null) {
                line("if (requested.id.id == \"org.jetbrains.kotlin.jvm\") useVersion(\"$overrideKotlinVersion\")")
                line("if (requested.id.id == \"org.jetbrains.kotlin.multiplatform\") useVersion(\"$overrideKotlinVersion\")")
                line("if (requested.id.id == \"org.jetbrains.kotlin.android\") useVersion(\"$overrideKotlinVersion\")")
            }
        }
    }

    call("repositories") {
        snapshotRepos.forEach { repo ->
            line("maven { url=${repo.replace("\\", "\\\\").uriForScript(language)} }")
        }
        line("gradlePluginPortal()")
        line("mavenCentral()")
    }
}

internal fun FormattedWriter.writeDependencyManagement(language: ScriptLanguage, snapshotRepos: List<String>) {
    call("dependencyResolutionManagement") {
        call("repositories") {
            snapshotRepos.forEach { repo ->
                line("maven { url=${repo.replace("\\", "\\\\").uriForScript(language)} }")
            }
            line("mavenCentral()")
        }
    }
}


internal fun File.writeSettings(build: TestBuildConfig) {
    writeScript {
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
