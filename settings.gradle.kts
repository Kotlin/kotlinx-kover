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

include(":kover-jvm-agent")
include(":kover-features-jvm")
include(":kover-gradle-plugin")
include(":kover-cli")
include(":kover-offline-runtime")
