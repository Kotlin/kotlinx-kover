plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}
