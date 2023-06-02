rootProject.name = "kover"

pluginManagement {
    includeBuild("build-logic")

    val kotlinVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
    }
}

include(":kover-gradle-plugin")
include(":kover-cli")
include(":kover-offline-runtime")