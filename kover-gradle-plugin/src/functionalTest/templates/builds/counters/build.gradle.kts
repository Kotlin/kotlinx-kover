plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

dependencies {
    testImplementation(kotlin("test"))
}
