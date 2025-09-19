plugins {
    id("com.android.application") version "8.12.0" apply false
    id("com.android.kotlin.multiplatform.library") version "8.12.0" apply false
    kotlin("multiplatform") version ("2.2.20") apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.2"
}

dependencies {
    kover(project(":app"))
}

kover {
    currentProject {
        createVariant("custom") { }
    }
}