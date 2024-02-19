plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
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
