plugins {
    id ("com.android.dynamic-feature")
    id ("org.jetbrains.kotlin.android")
    id ("org.jetbrains.kotlinx.kover")
}

android {
    namespace = "kotlinx.kover.test.android"

    compileSdk = 32

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.5.0")
    testImplementation("junit:junit:4.13.2")
    implementation(project(":app"))
}


/*
 * Kover configs
 */

dependencies {
    kover(project(":app"))
}

kover {
    reports {
        // filters for all report types of all build variants
        filters {
            excludes {
                androidGeneratedClasses()
            }
        }

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

