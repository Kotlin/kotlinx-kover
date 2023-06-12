plugins {
    kotlin("multiplatform") version ("1.8.20")
    id ("org.jetbrains.kotlinx.kover") version "0.7.1"
}

kotlin {
    jvm()
    jvmToolchain(8)
}

repositories {
    mavenCentral()
}

/*
 * Kover configs
 */

koverReport {
    defaults {
        verify {
            rule {
                minBound(50)
            }
        }
    }
}
