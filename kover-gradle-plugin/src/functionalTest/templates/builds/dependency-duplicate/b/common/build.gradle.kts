plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}