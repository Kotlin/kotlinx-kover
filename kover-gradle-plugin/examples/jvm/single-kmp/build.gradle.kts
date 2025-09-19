plugins {
    kotlin("multiplatform") version ("2.2.20")
    id("org.jetbrains.kotlinx.kover") version "0.9.2"
}

kotlin {
    jvm {
    }
}

dependencies {
    commonTestImplementation(kotlin("test"))
}

kover.reports.verify.rule {
    minBound(50)
}
