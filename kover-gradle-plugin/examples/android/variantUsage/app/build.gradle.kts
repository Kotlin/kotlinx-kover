plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlinx.kover")
}

android {
    namespace = "kotlinx.kover.test.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "kotlinx.kover.test.android"
        minSdk = 21
        targetSdk = 34
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


/*
 * Kover configs
 */
dependencies {
    /**
     * Use artifacts from 'lib' project.
     */
    kover(project(":lib"))
}

kover {
    currentProject {
        createVariant("custom") {
            /**
             * Tests, sources, classes, and compilation tasks of the 'debug' build variant will be included in the report variant `custom`.
             * Thus, information from the 'debug' variant will be included in the `custom` report for this project and any project that specifies this project as a dependency.
             */
            addWithDependencies("debug")
        }
    }

    reports {
        // filters for all report types of all build variants
        filters {
            excludes.androidGeneratedClasses()
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
