plugins {
    base
    id("org.jetbrains.kotlinx.kover")
}

repositories { mavenCentral() }

dependencies {
    kover(project(":subprojects:alpha-project"))
}
