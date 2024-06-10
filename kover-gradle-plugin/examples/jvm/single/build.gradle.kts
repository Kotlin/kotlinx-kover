plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.8.1"
}

dependencies {
    testImplementation(kotlin("test"))
}

kover.reports.verify.rule {
    minBound(50)
}