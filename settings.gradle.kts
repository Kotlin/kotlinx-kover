pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        mavenLocal()
    }

    plugins {
        kotlin("jvm") version embeddedKotlinVersion
    }
}

plugins {
    id("org.jetbrains.kotlinx.artifacts-validator-plugin") version "0.0.1-SNAPSHOT"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs")
    }
}

rootProject.name = "kover"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":kover-jvm-agent")
include(":kover-features-jvm")
include(":kover-gradle-plugin")
include(":kover-maven-plugin")
include(":kover-cli")
include(":kover-offline-runtime")
