plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
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
