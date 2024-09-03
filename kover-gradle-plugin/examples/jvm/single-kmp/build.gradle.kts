plugins {
    kotlin("multiplatform") version "1.9.20"
    id("org.jetbrains.kotlinx.kover") version "0.9.0-RC"
}

kotlin {
    jvm {
        withJava()
    }
}

dependencies {
    commonTestImplementation(kotlin("test"))
}

kover.reports.verify.rule {
    minBound(50)
}
