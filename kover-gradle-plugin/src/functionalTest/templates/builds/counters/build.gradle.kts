plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_9
    targetCompatibility = JavaVersion.VERSION_1_9
}

dependencies {
    testImplementation(kotlin("test"))
}
