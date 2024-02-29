plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.kotlinx.kover")
}

dependencies {
    kover(project(":subproject"))
}
