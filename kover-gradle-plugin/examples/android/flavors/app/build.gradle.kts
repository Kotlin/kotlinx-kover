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
        targetSdk = 31
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


kover {
    variants {
        create("custom") {
            /**
             * Tests, sources, classes, and compilation tasks of the 'app1AppDebug' build variant will be included in the report variant `custom`.
             * Thus, information from the 'app1AppDebug' variant will be included in the 'custom' report for this project and any project that specifies this project as a dependency.
             */
            addWithDependencies("app1AppDebug")
        }
    }

    reports {
        // filters for all report types of all build variants
        filters {
            excludes {
                androidGeneratedClasses()
            }
        }

        variant("app1AppRelease") {
            // filters for all report types only of 'app1AppRelease' build variant
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
