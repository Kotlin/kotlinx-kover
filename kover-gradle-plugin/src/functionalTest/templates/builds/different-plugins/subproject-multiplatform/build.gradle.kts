plugins {
    kotlin("multiplatform") version ("2.2.20")
    id("org.jetbrains.kotlinx.kover")
}

kotlin {
    jvm()

    sourceSets {
        commonTest {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
    }
}
