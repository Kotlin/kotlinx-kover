pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
rootProject.name = "example-merged"

include(":subproject")
include(":excluded")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
