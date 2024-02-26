pluginManagement {
    includeBuild("build-logic")

    plugins {
        kotlin("jvm") version embeddedKotlinVersion
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
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
