plugins {
    kotlin("multiplatform") version "1.9.20"
    id("org.jetbrains.kotlinx.kover") version "0.8.1"
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
