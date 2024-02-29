plugins {
    id("com.android.application") version "7.4.0" apply false
    id("com.android.library") version "7.4.0" apply false
    kotlin("multiplatform") version ("1.8.20") apply false
    id("org.jetbrains.kotlinx.kover") version "0.8.0-Beta"
}

dependencies {
    kover(project(":app"))
}

kover {
    currentProject {
        createVariant("custom") { }
    }
}