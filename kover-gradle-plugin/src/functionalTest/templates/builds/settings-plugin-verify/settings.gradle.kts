pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.jetbrains.kotlinx.kover.aggregation") version "SNAPSHOT"
}

extensions.configure<kotlinx.kover.gradle.aggregation.settings.dsl.KoverSettingsExtension> {
    enableCoverage()

    reports {
        verify {
            warningInsteadOfFailure = true

            rule("named rule") {
                // should fail
                bound {
                    minValue = 100
                }
            }
            rule {
                // shoul pass because RootClass is fully covered
                name = "include class Rule"
                filters {
                    includedClasses.add("tests.settings.root.RootClass")
                }
                bound {
                    minValue = 100
                }
            }
            rule {
                name = "included project"
                // shoul pass because project ':subproject' is fully covered
                filters {
                    includedProjects.add(":subproject")
                }
                bound {
                    minValue = 100
                }
            }
            rule {
                // should fail
                bound {
                    maxValue = 10
                }
            }
        }
    }
}

buildCache {
    local {
        directory = "$settingsDir/build-cache"
    }
}

rootProject.name = "settings-plugin-verify"

include(":subproject")
