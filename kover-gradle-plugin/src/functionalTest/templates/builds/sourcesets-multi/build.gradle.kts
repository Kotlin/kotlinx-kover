plugins {
    kotlin("jvm") version ("2.2.20")
    id("org.jetbrains.kotlinx.kover") version "0.7.0"
}

sourceSets.create("extra")
sourceSets.create("foo")

dependencies {
    testImplementation(kotlin("test"))
}
