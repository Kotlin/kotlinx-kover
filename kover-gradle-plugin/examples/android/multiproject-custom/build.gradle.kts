plugins {
    id("com.android.application") version "9.0.0" apply false
    id("com.android.library") version "9.0.0" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.6"
}

dependencies {
    kover(project(":app"))
    kover(project(":lib"))
}

kover {
    currentProject {
        createVariant("custom") {
        }
    }

    reports {
        // filters for all report types of all build variants
        filters {
            excludes {
                androidGeneratedClasses()
            }
        }

        variant("custom") {
            // verification only for 'custom' report variant
            verify {
                rule {
                    minBound(50)
                }
            }

            // filters for all report types only of 'custom' build type
            filters {
                excludes {
                    androidGeneratedClasses()
                    classes(
                        // excludes debug classes
                        "*.DebugUtil"
                    )
                }
            }
        }

    }
}
