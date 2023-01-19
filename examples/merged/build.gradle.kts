plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":subproject"))
    implementation(project(":excluded"))
    testImplementation(kotlin("test"))

    kover(project(":subproject"))
}

koverReport {
    filters {
        excludes {
            className("kotlinx.kover.examples.merged.utils.*", "kotlinx.kover.examples.merged.subproject.utils.*")
        }
        includes {
            className("kotlinx.kover.examples.merged.*")
        }
    }

    verify {
        rule {
            bound {
                minValue = 50
                maxValue = 75
            }
        }
    }
}
