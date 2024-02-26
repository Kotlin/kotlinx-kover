plugins {
    base
    id("org.jetbrains.kotlinx.kover")
}

dependencies {
    kover(project(":subprojects:alpha-project"))
}
