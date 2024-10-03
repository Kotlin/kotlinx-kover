pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

import kotlinx.kover.gradle.aggregation.settings.dsl.*

plugins {
    id("org.jetbrains.kotlinx.kover.aggregation") version "SNAPSHOT"
}

extensions.configure<kotlinx.kover.gradle.aggregation.settings.dsl.KoverSettingsExtension> {
    enableCoverage()

    skipProjects(
        // skip root project and everything works ok
        // skip by path
        ":",
        // skip by project name
        "ignored"
    )

    reports {
        verify {
            warningInsteadOfFailure = true

            eachProjectRule {
                minBound(100)
            }
        }
    }
}

buildCache {
    local {
        directory = "$settingsDir/build-cache"
    }
}

rootProject.name = "settings-plugin-verify"

include(":subproject")
include(":subproject2")
include(":ignored")
