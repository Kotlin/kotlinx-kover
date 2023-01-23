plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")

    id("org.jetbrains.kotlinx.kover")
}

android {
    compileSdk = Versions.COMPILE_SDK

    defaultConfig {
        applicationId = "org.jetbrains.kover_android_kts_example"
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK
        versionCode = Versions.VERSION_CODE
        versionName = Versions.VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    api(platform(project(":depconstraints")))
    kapt(platform(project(":depconstraints")))
    androidTestApi(platform(project(":depconstraints")))


    // Android

    implementation(Libs.APPCOMPAT)
    implementation(Libs.CONSTRAINT_LAYOUT)
    implementation(Libs.CORE_KTX)
    implementation(Libs.FRAGMENT_KTX)
    implementation(Libs.LIFECYCLE_LIVE_DATA_KTX)
    implementation(Libs.MATERIAL)


    // Unit tests

    testImplementation(Libs.ARCH_CORE_TESTING)
    testImplementation(Libs.COROUTINES_TEST)
    testImplementation(Libs.JUNIT)
    testImplementation(Libs.MOCKK)
}

koverReport {
    filters {
        excludes {
            className(
                "*Fragment",
                "*Fragment\$*",
                "*Activity",
                "*Activity\$*",
                "*.databinding.*", // ViewBinding
                "org.jetbrains.kover_android_kts_example.BuildConfig"
            )
        }
    }
}
