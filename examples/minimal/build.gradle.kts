plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.6.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}
