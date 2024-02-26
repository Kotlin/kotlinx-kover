plugins {
    kotlin("multiplatform") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.7.0"
}

repositories {
    mavenCentral()
}

kover {
    currentProject {
        sources {
            excludedSourceSets.add("extra")
        }
    }
}

sourceSets.create("extra")

kotlin {
    jvm {
        withJava()
    }

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
