plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.6.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":subproject"))
    implementation(project(":excluded"))
    testImplementation(kotlin("test"))
}

koverMerged {
    enable()

    filters {
        classes {
            includes += "kotlinx.kover.examples.merged.*"
            excludes += listOf("kotlinx.kover.examples.merged.utils.*", "kotlinx.kover.examples.merged.subproject.utils.*")
        }

        projects {
            excludes += "excluded"
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
