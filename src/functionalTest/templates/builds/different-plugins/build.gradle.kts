plugins {
//    kotlin("multiplatform") version ("1.7.20")
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
}

dependencies {
    kover(project(":subproject-multiplatform"))
}



//kotlin {
//    jvm()
//}
