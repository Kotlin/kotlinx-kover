
import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    androidLibrary {
        namespace = "kotlinx.kover.test.android.multiplatform-library"
        compileSdk = 33
        minSdk = 24

        withHostTest { }
    }
}

dependencies {
    commonTestImplementation("junit:junit:4.13.2")
}