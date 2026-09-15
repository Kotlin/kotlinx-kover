plugins {
    kotlin("jvm") version "2.4.0" apply false

    id("org.jetbrains.kotlinx.kover")

}

allprojects {
    group = "org.jetbrains"
}

dependencies {
    kover(project(":a:common"))
    kover(project(":b:common"))
}