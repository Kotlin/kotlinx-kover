pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.jetbrains.kotlinx.kover.aggregation") version "SNAPSHOT"
}

extensions.configure<kotlinx.kover.gradle.aggregation.settings.dsl.KoverSettingsExtension> {
    enableCoverage()

    instrumentation.excludedClasses.add("*Class")
}

buildCache {
    local {
        directory = "$settingsDir/build-cache"
    }
}

rootProject.name = "settings-plugin-verify"

include(":subproject")
include(":subproject2")
