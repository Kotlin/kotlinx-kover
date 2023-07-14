plugins {
    kotlin("jvm") version "1.4.20"
}

apply(plugin = "org.jetbrains.kotlinx.kover")

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.0")
}

tasks.test {
    useJUnitPlatform()
}

