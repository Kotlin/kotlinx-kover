plugins {
    kotlin("jvm") version ("2.2.20")
    id("org.jetbrains.kotlinx.kover") version "0.9.7"
}

dependencies {
    implementation(project(":subproject"))
    implementation(project(":excluded"))
    testImplementation(kotlin("test"))

    kover(project(":subproject"))
}

kover.reports {
    filters {
        excludes.classes("kotlinx.kover.examples.merged.utils.*", "kotlinx.kover.examples.merged.subproject.utils.*")
        includes.classes("kotlinx.kover.examples.merged.*")
    }

    verify {
        rule {
            bound {
                minValue.set(50)
                maxValue.set(75)
            }
        }
    }
}
