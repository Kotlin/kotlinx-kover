plugins {
    kotlin("jvm") version ("2.2.20")
    id("org.jetbrains.kotlinx.kover") version "0.9.5"
}

dependencies {
    testImplementation(kotlin("test"))
}

kover.reports.verify.rule {
    minBound(50)
}