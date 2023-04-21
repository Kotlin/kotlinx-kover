plugins {
    id("com.android.application") version "7.2.2" apply false
    id("com.android.library") version "7.2.2" apply false
    kotlin("multiplatform") version ("1.7.20") apply false
    id("org.jetbrains.kotlinx.kover") version "0.7.0-Alpha"
}

dependencies {
    kover(project(":app"))
}
