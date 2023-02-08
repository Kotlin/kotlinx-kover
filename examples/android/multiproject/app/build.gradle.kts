plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
    id ("org.jetbrains.kotlinx.kover")
}

android {
    namespace = "kotlinx.kover.test.android"
    compileSdk = 33

    defaultConfig {
        applicationId = "kotlinx.kover.test.android"
        minSdk = 28
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.6.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}


/*
 * Kover configs
 */

dependencies {
    kover(project(":lib"))
}


koverAndroid {
    // filters for all report types of all build variants
    common {
        filters {
            excludes {
                className(
                    "*Fragment",
                    "*Fragment\$*",
                    "*Activity",
                    "*Activity\$*",
                    "*.databinding.*",
                    "*.BuildConfig"
                )
            }
        }
    }

    report("release") {
        // filters for all report types only of 'release' build type
        filters {
            excludes {
                className(
                        "*Fragment",
                        "*Fragment\$*",
                        "*Activity",
                        "*Activity\$*",
                        "*.databinding.*",
                        "*.BuildConfig",

                        // excludes debug classes
                        "*.DebugUtil"
                )
            }
        }
    }

}
