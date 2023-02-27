plugins {
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
}

dependencies {
    kover(project(":subproject-multiplatform"))
}
