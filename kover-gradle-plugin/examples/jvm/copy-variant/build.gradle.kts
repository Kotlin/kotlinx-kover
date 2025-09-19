plugins {
    kotlin("jvm") version ("2.2.20")
    id("org.jetbrains.kotlinx.kover") version "0.9.2"
}

dependencies {
    implementation(project(":first"))
    implementation(project(":second"))
    testImplementation(kotlin("test"))
}

kover {
    merge {
        subprojects()

        createVariant("aggregated") {
            add("jvm")
        }
    }

    currentProject {
        copyVariant("first", "aggregated")
        copyVariant("second", "aggregated")
    }

    reports {
        variant("first") {
            filters.includes.projects.add(":first")
        }
        variant("second") {
            filters.includes.projects.add(":second")
        }
    }
}
