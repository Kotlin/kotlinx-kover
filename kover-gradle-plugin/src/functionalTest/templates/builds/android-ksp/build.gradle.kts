plugins {
	id("com.android.library") version "8.13.0"
	id("org.jetbrains.kotlin.android") version "2.2.20"
	id("com.google.devtools.ksp") version "2.2.20-2.0.4"
	id("org.jetbrains.kotlinx.kover") version "0.9.3-SNAPSHOT"
}

android {
	namespace = "com.example.lib"
	compileSdk = 36
	defaultConfig.minSdk = 24
}
