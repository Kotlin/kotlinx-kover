plugins {
    kotlin("jvm") version ("2.2.0")
    id("org.jetbrains.kotlinx.kover") version "0.7.0"
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
