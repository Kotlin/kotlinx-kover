plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.7.0-Beta"
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
            classes("kotlinx.kover.examples.merged.utils.*", "kotlinx.kover.examples.merged.subproject.utils.*")
        }
        includes {
            classes("kotlinx.kover.examples.merged.*")
        }
    }

    defaults {
        verify {
            rule {
                bound {
                    minValue = 50
                    maxValue = 75
                }
            }
        }
    }
}
