plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}
