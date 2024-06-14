pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
rootProject.name = "copy-variant"

include(":first")
include(":second")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
