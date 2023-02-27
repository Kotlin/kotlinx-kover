plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.7.0-Alpha"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kover {
    disabledForProject = false

    useKoverTool()

    excludeInstrumentation {
        classes("com.example.subpackage.*")
    }
}

koverReport {
    filters {
        excludes {
            classes("com.example.subpackage.*")
        }
        includes {
            classes("com.example.*")
        }
    }

    xml {
        onCheck = false
        setReportFile(layout.buildDirectory.file("my-project-report/result.xml"))

        filters {
            excludes {
                classes("com.example2.subpackage.*")
            }
            includes {
                classes("com.example2.*")
            }
        }
    }

    html {
        onCheck = false
        setReportDir(layout.buildDirectory.dir("my-project-report/html-result"))

        filters {
            excludes {
                classes("com.example2.subpackage.*")
            }
            includes {
                classes("com.example2.*")
            }
        }
    }

    verify {
        onCheck = true
        rule {
            isEnabled = true
            entity = kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION

            filters {
                excludes {
                    classes("com.example.verify.subpackage.*")
                }
                includes {
                    classes("com.example.verify.*")
                }
            }

            bound {
                minValue = 1
                maxValue = 99
                metric = kotlinx.kover.gradle.plugin.dsl.MetricType.LINE
                aggregation = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
            }
        }
    }
}
