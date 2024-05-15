plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
}

dependencies {
    testImplementation(kotlin("test"))
}

kover {
    currentProject {
        createVariant("custom") {
            add("jvm")
        }
    }
}
