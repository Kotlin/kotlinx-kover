plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.8.0-Beta2"
}

dependencies {
    testImplementation(kotlin("test"))
}

kover {
    reports {
        verify {
            rule {
                minBound(50)
            }
        }
    }
}