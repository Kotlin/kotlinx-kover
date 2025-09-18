plugins {
    id ("org.jetbrains.kotlinx.kover") version "0.7.1"
    id ("com.android.application") version "8.12.0"
    kotlin("multiplatform") version ("2.2.0")
}

android {
    namespace = "kotlinx.kover.test.android"
    compileSdk = 32

    defaultConfig {
        applicationId = "kotlinx.kover.test.android"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.5.0")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
}

kotlin {
    androidTarget()

    jvm() {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

    jvmToolchain(8)
}




/*
 * Kover configs
 */


kover {
    currentProject {
        createVariant("custom") {
            /**
             * Tests, sources, classes, and compilation tasks of the 'release' build variant will be included in the `custom` reports.
             * Thus, information from the 'release' variant will be included in the `custom` report for this project and any project that specifies this project as a dependency.
             */
            add("release")
        }
    }

    reports {
        variant("release") {
            // filters for all report types only of 'release' build type
            filters {
                excludes {
                    androidGeneratedClasses()

                    classes(
                        // excludes debug classes
                        "*.DebugUtil"
                    )
                }
            }
        }
    }

}