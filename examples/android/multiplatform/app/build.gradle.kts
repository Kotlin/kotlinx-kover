plugins {
    id ("com.android.application")
    kotlin("multiplatform")
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
    android()

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
    default {
        /**
         * Tests, sources, classes, and compilation tasks of the 'debug' build variant will be included in the default artifact.
         * Thus, information from the 'debug' variant will be included in the default report for this project and any project that specifies this project as a dependency.
         *
         * Since the report already contains classes from the JVM target, they will be supplemented with classes from 'debug' build variant of Android target.
         */
        addWithDependencies("debug")
    }
}

koverAndroid {
    // filters for all report types of all build variants
    common {
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
    }

    report("release") {
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
