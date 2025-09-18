plugins {
    id("com.android.kotlin.multiplatform.library")
    kotlin("multiplatform")
    id ("org.jetbrains.kotlinx.kover")
}

kotlin {
    androidLibrary {
        namespace = "kotlinx.kover.test.android"
        compileSdk = 33
        minSdk = 24

        withJava()
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        withHostTest {  }
    }
}


dependencies {
    commonTestImplementation("junit:junit:4.13.2")
}


kover {
    currentProject {
        createVariant("custom") {
            add("jvm")
        }
    }
}
