import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.version

plugins {
    id("com.android.application") version "9.0.0" apply false
    id("org.jetbrains.kotlin.android") version "2.3.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0" apply false
    id("com.google.devtools.ksp") version "2.3.4" apply false
    id("androidx.room") version "2.8.4" apply false
}
