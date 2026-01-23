plugins {
    kotlin("jvm") version ("2.2.20")
    id("org.jetbrains.kotlinx.kover")
}

dependencies {
    kover(project(":subproject"))
}

repositories {
    mavenCentral()
}
