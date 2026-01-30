import kotlinx.kover.gradle.plugin.dsl.tasks.*

plugins {
    id ("org.jetbrains.kotlinx.kover")
    id ("com.android.application")
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

tasks.register("findAllTasks") {
    dependsOn("findTotalTasks")
    dependsOn("findDebugTasks")
}

tasks.register("findDebugTasks") {
    doLast {
        val xmlName = tasks.withType<KoverXmlReport>().matching {
            it.variantName == "debug"
        }.single().name

        val htmlName = tasks.withType<KoverHtmlReport>().matching {
            it.variantName == "debug"
        }.single().name

        val verifyName = tasks.withType<KoverVerifyReport>().matching {
            it.variantName == "debug"
        }.single().name

        val logName = tasks.withType<KoverLogReport>().matching {
            it.variantName == "debug"
        }.single().name

        val binaryName = tasks.withType<KoverBinaryReport>().matching {
            it.variantName == "debug"
        }.single().name

        println("XML=$xmlName")
        println("HTML=$htmlName")
        println("Verify=$verifyName")
        println("Log=$logName")
        println("Binary=$binaryName")
    }
}

tasks.register("findTotalTasks") {
    doLast {
        val xmlName = tasks.withType<KoverXmlReport>().matching {
            it.variantName == ""
        }.single().name

        val htmlName = tasks.withType<KoverHtmlReport>().matching {
            it.variantName == ""
        }.single().name

        val verifyName = tasks.withType<KoverVerifyReport>().matching {
            it.variantName == ""
        }.single().name

        val logName = tasks.withType<KoverLogReport>().matching {
            it.variantName == ""
        }.single().name

        val binaryName = tasks.withType<KoverBinaryReport>().matching {
            it.variantName == ""
        }.single().name

        println("XML=$xmlName")
        println("HTML=$htmlName")
        println("Verify=$verifyName")
        println("Log=$logName")
        println("Binary=$binaryName")
    }
}
