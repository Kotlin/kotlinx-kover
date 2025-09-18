plugins {
    kotlin("multiplatform") version ("2.2.0")
    id("org.jetbrains.kotlinx.kover") version "0.7.0"
}

kover.currentProject.sources.excludedSourceSets.add("extra")

sourceSets.create("extra")

kotlin {
    jvm {
    }

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
