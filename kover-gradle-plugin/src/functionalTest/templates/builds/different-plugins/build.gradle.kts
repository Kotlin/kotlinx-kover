plugins {
    id("org.jetbrains.kotlinx.kover")
}

dependencies {
    kover(project(":subproject-multiplatform"))
}
