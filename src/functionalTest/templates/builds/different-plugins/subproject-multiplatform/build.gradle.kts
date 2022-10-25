plugins {
    kotlin("multiplatform") version ("1.7.20")
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
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
