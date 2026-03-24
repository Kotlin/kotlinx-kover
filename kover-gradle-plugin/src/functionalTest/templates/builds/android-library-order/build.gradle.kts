import kotlinx.kover.gradle.plugin.KoverGradlePlugin

plugins {
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false

    id("org.jetbrains.kotlinx.kover") version "0.9.7" apply false
}

subprojects {
    apply<KoverGradlePlugin>()
}
