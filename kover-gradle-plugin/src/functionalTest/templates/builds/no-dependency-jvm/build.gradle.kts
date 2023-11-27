plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
}

dependencies {
    kover(project(":subproject"))
}
