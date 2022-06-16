plugins {
    kotlin("multiplatform")
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
