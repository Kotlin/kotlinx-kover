plugins {
    id ("com.android.library")
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

    buildTypes {
        release {
            isMinifyEnabled = true
        }
    }

    /**
     * A dimension that is present only in the Library Android project.
     */
    flavorDimensions += "dimlib"
    /**
     * A dimension that is present in both the Application and Library Android projects.
     */
    flavorDimensions += "both"

    productFlavors {
        create("lib") {
            dimension = "both"
        }
        create("lib1") {
            dimension = "dimlib"
        }
        create("lib2") {
            dimension = "dimlib"
        }


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
}
