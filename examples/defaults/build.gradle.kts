plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kover {
    isDisabled.set(false)
    tool.set(kotlinx.kover.api.KoverToolDefault)
    filters {
        classes {
            includes += "com.example.*"
            excludes += listOf("com.example.subpackage.*")
        }
    }

    instrumentation {
        excludeTasks += "dummy-tests"
    }

    xmlReport {
        onCheck.set(false)
        reportFile.set(layout.buildDirectory.file("my-project-report/result.xml"))
        overrideFilters {
            classes {
                includes += "com.example2.*"
                excludes += listOf("com.example2.subpackage.*")
            }
        }
    }

    htmlReport {
        onCheck.set(false)
        reportDir.set(layout.buildDirectory.dir("my-project-report/html-result"))
        overrideFilters {
            classes {
                includes += "com.example2.*"
                excludes += listOf("com.example2.subpackage.*")
            }
        }
    }

    verify {
        onCheck.set(true)
        rule {
            isEnabled = true
            name = null
            target = kotlinx.kover.api.VerificationTarget.ALL

            overrideClassFilter {
                includes += "com.example.verify.*"
                excludes += listOf("com.example.verify.subpackage.*")
            }

            bound {
                minValue = 1
                maxValue = 99
                counter = kotlinx.kover.api.CounterType.LINE
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE
            }
        }
    }
}

tasks.test {
    extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
        isDisabled.set(false)
        reportFile.set(file("$buildDir/custom/result.bin"))
        includes.addAll("com.example.*")
        excludes.addAll("com.example.subpackage.*")
    }
}

koverMerged {
    enable()

    filters {
        classes {
            includes += "com.example.*"
            excludes += listOf("com.example.subpackage.*")
        }

        projects {
            excludes += listOf()
        }
    }


    xmlReport {
        onCheck.set(false)
        reportFile.set(layout.buildDirectory.file("my-merged-report/result.xml"))
        overrideClassFilter {
            includes += "com.example2.*"
            excludes += listOf("com.example2.subpackage.*")
        }
    }

    htmlReport {
        onCheck.set(false)
        reportDir.set(layout.buildDirectory.dir("my-merged-report/html-result"))
        overrideClassFilter {
            includes += "com.example2.*"
            excludes += listOf("com.example2.subpackage.*")
        }
    }

    verify {
        onCheck.set(true)
        rule { // add verification rule
            isEnabled = true
            name = null
            target = kotlinx.kover.api.VerificationTarget.ALL

            overrideClassFilter {
                includes += "com.example.verify.*"
                excludes += listOf("com.example.verify.subpackage.*")
            }

            bound {
                minValue = 1
                maxValue = 99
                counter = kotlinx.kover.api.CounterType.LINE
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE
            }
        }
    }
}
