plugins {
    kotlin("multiplatform") version ("2.2.0")
    id ("org.jetbrains.kotlinx.kover") version "0.7.1"
}

kotlin {
    jvm()
    jvmToolchain(8)
}

/*
 * Kover configs
 */

kover {
    reports {
        verify {
            rule {
                minBound(50)
            }
        }
    }
}
