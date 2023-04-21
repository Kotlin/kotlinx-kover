plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
    id ("org.jetbrains.kotlinx.kover")
}

android {
    namespace = "kotlinx.kover.test.android"
    compileSdk = 32

    defaultConfig {
        applicationId = "kotlinx.kover.test.android"
        minSdk = 21
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        /**
         * Since the 'dimlib' dimension is missing in this project, the default for it is to take 'lib2' flavor.
         *
         * If this is not specified, then ambiguous may arise, because the library can publish several artifacts with such a dimension, it is for this that we need to configure the flavor resolution.
         */
        missingDimensionStrategy("dimlib", "lib2")

        /**
         * This dimension is not used in any of the projects, however, this setting does not break the resolution.
         */
        missingDimensionStrategy("unused", "definitly")
    }

    /**
     * A dimension that is present only in the Application Android project.
     */
    flavorDimensions += "dimapp"
    /**
     * A dimension that is present in both the Application and Library Android projects.
     */
    flavorDimensions += "both"

    productFlavors {
        create("app1") {
            dimension = "dimapp"
            /**
             * Since the 'dimlib' dimension is missing in this project, for 'app1' flavor used 'lib1'.
             */
            missingDimensionStrategy("dimlib", "lib1")
        }
        create("app2") {
            dimension = "dimapp"
        }

        create("app") {
            dimension = "both"
            /**
             * 'lib' flavor is absent in the Library project, so we will take 'lib' flavor from it.
             */
            matchingFallbacks += "lib"
        }
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

dependencies {
    /**
     * Use artifacts from 'lib' project.
     */
    kover(project(":lib"))
}


koverReport {
    // filters for all report types of all build variants
    filters {
        excludes {
            classes(
                "*Fragment",
                "*Fragment\$*",
                "*Activity",
                "*Activity\$*",
                "*.databinding.*",
                "*.BuildConfig"
            )
        }
    }

    defaults {
        /**
         * Tests, sources, classes, and compilation tasks of the 'debug' build variant will be included in the default report.
         * Thus, information from the 'app1AppDebug' variant will be included in the default report for this project and any project that specifies this project as a dependency.
         */
        mergeWith("app1AppDebug")
    }

    androidReports("release") {
        // filters for all report types only of 'release' build type
        filters {
            excludes {
                classes(
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
