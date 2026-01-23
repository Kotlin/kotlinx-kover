plugins {
    id("com.android.application") version "9.0.0" apply false
    id("com.android.library") version "9.0.0" apply false
    id("org.jetbrains.kotlinx.kover") version "0.7.1"
}

/*
 * Kover configs
 */

subprojects {
    apply(plugin = "org.jetbrains.kotlinx.kover")
}