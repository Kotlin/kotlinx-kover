plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
}

dependencies {
    testImplementation(kotlin("test"))
}

group = "org.jetbrains.b"

tasks.test {
    useJUnitPlatform()
}