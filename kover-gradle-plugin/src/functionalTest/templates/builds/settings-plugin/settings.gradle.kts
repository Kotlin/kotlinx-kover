pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.jetbrains.kotlinx.kover.aggregation") version "SNAPSHOT"
}

buildCache {
    local {
        directory = "$settingsDir/build-cache"
    }
}

rootProject.name = "settings-plugin"

include(":subproject")
