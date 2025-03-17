pluginManagement {
    includeBuild("build-logic")

    plugins {
        kotlin("jvm") version embeddedKotlinVersion
    }
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
include(":kover-reporter")
