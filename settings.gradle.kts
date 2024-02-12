rootProject.name = "kover"

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

include(":kover-features-java")
include(":kover-gradle-plugin")
include(":kover-cli")
include(":kover-offline-runtime")
