plugins {
    id ("org.jetbrains.kotlinx.kover")
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
}

android {
    namespace = "kotlinx.kover.test.android"
    compileSdk = 32

    defaultConfig {
        applicationId = "kotlinx.kover.test.android"
        minSdk = 21
        targetSdk = 31
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
    kotlinOptions {
        jvmTarget = "1.8"
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


/*
 * Kover configs
 */

kover {
    currentProject {
        createVariant("custom") {
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
