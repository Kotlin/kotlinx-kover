plugins {
    kotlin("jvm") version ("2.2.0")
    id("org.jetbrains.kotlinx.kover")
}

dependencies {
    kover(project(":subproject"))
}
