plugins {
    id("com.android.application") version "7.4.0" apply false
    id("com.android.library") version "7.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.20" apply false
    id("org.jetbrains.kotlinx.kover") version "0.8.0"
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
            // verification ony for 'custom' report variant
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
