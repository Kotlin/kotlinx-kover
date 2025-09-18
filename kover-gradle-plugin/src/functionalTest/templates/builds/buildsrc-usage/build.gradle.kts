plugins {
    kotlin("jvm") version ("2.2.0")
}

apply(plugin = "org.jetbrains.kotlinx.kover")

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.0")
}

tasks.test {
    useJUnitPlatform()
}

