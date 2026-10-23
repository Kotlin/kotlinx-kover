plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
}

dependencies {
    testImplementation(kotlin("test"))
}

group = "org.jetbrains.a"

tasks.test {
    useJUnitPlatform()
}