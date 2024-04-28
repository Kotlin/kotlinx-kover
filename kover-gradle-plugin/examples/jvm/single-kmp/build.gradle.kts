plugins {
    kotlin("multiplatform") version "1.9.20"
    id("org.jetbrains.kotlinx.kover") version "0.8.0-Beta2"
}

kotlin {
    jvm {
        withJava()
    }
}

dependencies {
    commonTestImplementation(kotlin("test"))
}
