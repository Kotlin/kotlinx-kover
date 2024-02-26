plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.7.0"
}

repositories {
    mavenCentral()
}

sourceSets.create("extra")

kover {
    currentProject {
        sources {
            excludedSourceSets.add("extra")
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
}
