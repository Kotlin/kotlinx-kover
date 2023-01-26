rootProject.name = "kover-gradle-plugin"

pluginManagement {
    val kotlinVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
    }
}
