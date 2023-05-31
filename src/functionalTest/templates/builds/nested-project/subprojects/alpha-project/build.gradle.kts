plugins {
    kotlin("jvm") version ("1.7.20")
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}
