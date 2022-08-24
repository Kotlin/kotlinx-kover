plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jetbrains.kotlinx.kover") version "0.6.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kover {
    isDisabled.set(false) // true to disable instrumentation and all Kover tasks in this project
    engine.set(kotlinx.kover.api.DefaultIntellijEngine) // change Coverage Engine
    filters { // common filters for all default Kover tasks
        classes { // common class filter for all default Kover tasks
            includes += "com.example.*" // class inclusion rules
            excludes += listOf("com.example.subpackage.*") // class exclusion rules
        }
    }

    instrumentation {
        excludeTasks += "dummy-tests" // set of test tasks names to exclude from instrumentation. The results of their execution will not be presented in the report
    }

    xmlReport {
        onCheck.set(false) // true to run koverXmlReport task during the execution of the check task (if it exists) of the current project
        reportFile.set(layout.buildDirectory.file("my-project-report/result.xml")) // change report file name
        overrideFilters {
            classes { // override common class filter
                includes += "com.example2.*" // override class inclusion rules
                excludes += listOf("com.example2.subpackage.*") // override class exclusion rules
            }
        }
    }

    htmlReport {
        onCheck.set(false) // true to run koverHtmlReport task during the execution of the check task (if it exists) of the current project
        reportDir.set(layout.buildDirectory.dir("my-project-report/html-result")) // change report directory
        overrideFilters {
            classes { // override common class filter
                includes += "com.example2.*" // class inclusion rules
                excludes += listOf("com.example2.subpackage.*") // override class exclusion rules
            }
        }
    }

    verify {
        onCheck.set(true) // true to run koverVerify task during the execution of the check task (if it exists) of the current project
        rule { // add verification rule
            isEnabled = true // false to disable rule checking
            name = null // custom name for the rule
            target = kotlinx.kover.api.VerificationTarget.ALL // specify by which entity the code for separate coverage evaluation will be grouped

            overrideClassFilter { // override common class filter
                includes += "com.example.verify.*" // override class inclusion rules
                excludes += listOf("com.example.verify.subpackage.*") // override class exclusion rules
            }

            bound { // add rule bound
                minValue = 1
                maxValue = 99
                counter = kotlinx.kover.api.CounterType.LINE // change coverage metric to evaluate (LINE, INSTRUCTION, BRANCH)
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE // change counter value (COVERED_COUNT, MISSED_COUNT, COVERED_PERCENTAGE, MISSED_PERCENTAGE)
            }
        }
    }
}

tasks.test {
    extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
        isDisabled.set(false) // true to disable instrumentation tests of this task, Kover reports will not depend on the results of their execution
        reportFile.set(file("$buildDir/custom/result.bin")) // set file name of binary report
        includes.addAll("com.example.*") // see "Instrumentation inclusion rules" below
        excludes.addAll("com.example.subpackage.*") // see "Instrumentation exclusion rules" below
    }
}

koverMerged {
    enable()  // create Kover merged reports

    filters { // common filters for all default Kover merged tasks
        classes { // common class filter for all default Kover merged tasks
            includes += "com.example.*" // class inclusion rules
            excludes += listOf("com.example.subpackage.*") // class exclusion rules
        }

        projects { // common projects filter for all default Kover merged tasks
            excludes += listOf() // Specifies the projects excluded from the merged tasks, eg listOf("project1", ":child:project")
        }
    }


    xmlReport {
        onCheck.set(false) // true to run koverMergedXmlReport task during the execution of the check task (if it exists) of the current project
        reportFile.set(layout.buildDirectory.file("my-merged-report/result.xml")) // change report file name
        overrideClassFilter { // override common class filter
            includes += "com.example2.*" // override class inclusion rules
            excludes += listOf("com.example2.subpackage.*") // override class exclusion rules
        }
    }

    htmlReport {
        onCheck.set(false) // true to run koverMergedHtmlReport task during the execution of the check task (if it exists) of the current project
        reportDir.set(layout.buildDirectory.dir("my-merged-report/html-result")) // change report directory
        overrideClassFilter { // override common class filter
            includes += "com.example2.*" // override class inclusion rules
            excludes += listOf("com.example2.subpackage.*") // override class exclusion rules
        }
    }

    verify {
        onCheck.set(true) // true to run koverMergedVerify task during the execution of the check task (if it exists) of the current project
        rule { // add verification rule
            isEnabled = true // false to disable rule checking
            name = null // custom name for the rule
            target = kotlinx.kover.api.VerificationTarget.ALL // specify by which entity the code for separate coverage evaluation will be grouped

            overrideClassFilter { // override common class filter
                includes += "com.example.verify.*" // override class inclusion rules
                excludes += listOf("com.example.verify.subpackage.*") // override class exclusion rules
            }

            bound { // add rule bound
                minValue = 1
                maxValue = 99
                counter = kotlinx.kover.api.CounterType.LINE // change coverage metric to evaluate (LINE, INSTRUCTION, BRANCH)
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE // change counter value (COVERED_COUNT, MISSED_COUNT, COVERED_PERCENTAGE, MISSED_PERCENTAGE)
            }
        }
    }
}
