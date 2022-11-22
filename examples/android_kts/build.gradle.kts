buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.ANDROID_GRADLE_PLUGIN}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KOTLIN}")
        classpath("org.jetbrains.kotlinx:kover:0.6.1")
    }
}

plugins {
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    apply(plugin = "kover")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

koverMerged {
    enable()

    filters {
        classes {
            excludes.addAll(
                listOf(
                    "*Fragment",
                    "*Fragment\$*",
                    "*Activity",
                    "*Activity\$*",
                    "*.databinding.*", // ViewBinding
                    "org.jetbrains.kover_android_kts_example.BuildConfig"
                )
            )
        }
    }
}
