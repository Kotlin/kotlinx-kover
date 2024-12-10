plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.9.0"
}

dependencies {
    testImplementation(kotlin("test"))
}

kover.reports.verify.rule {
    minBound(50)
}