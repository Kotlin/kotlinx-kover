# Kover Gradle Plugin

[![Kotlin Alpha](https://kotl.in/badges/alpha.svg)](https://kotlinlang.org/docs/components-stability.html)
[![JetBrains incubator project](https://jb.gg/badges/incubator.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

Gradle plugin for Kotlin code coverage tools: [Kover](https://github.com/JetBrains/intellij-coverage)
and [JaCoCo](https://github.com/jacoco/jacoco).

Minimum supported version of `Gradle` is `6.8`.



For more information about recent stable release, please refer to the [documentation of the latest release](https://Kotlin.github.io/kotlinx-kover)

## Features

* Collection of code coverage through `JVM` test tasks (JS and native targets are not supported yet).
* generating `HTML` and `XML` reports.
* Support for `Kotlin JVM`, `Kotlin Multiplatform` projects.
* Support for `Kotlin Android` projects with build variants (instrumentation tests executing on the Android device are not supported yet).
* Support mixed `Kotlin` and `Java` sources
* Verification rules with bounds to keep track of coverage.
* Using Kover or JaCoCo Coverage tools for coverage measuring and report generation.

## Quickstart

The recommended way of applying Kover is with the
[plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block).

Add the following to your top-level build file:

<details open>
<summary>Kotlin</summary>

```kotlin
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.7.0-Alpha"
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
plugins {
    id 'org.jetbrains.kotlinx.kover' version '0.7.0-Alpha'
}
```
</details>

#### Legacy Plugin Application

[Legacy method](https://docs.gradle.org/current/userguide/plugins.html#sec:old_plugin_application) of applying plugins
can be used if you cannot use the plugins DSL for some reason.

<details open>
<summary>Kotlin</summary>

```kotlin
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlinx:kover-gradle-plugin:0.7.0-Alpha")
    }
}

apply(plugin = "kover")

```

</details>

<details>
<summary>Groovy</summary>

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'org.jetbrains.kotlinx:kover-gradle-plugin:0.7.0-Alpha'
    }
}
  
apply plugin: 'kover'
```

</details>

## About Beta version
This is unstable test version of Kover Gradle Plugin with updated API.
Using this version is preferable in pet projects.

Detailed documentation has not yet been completed.
Refer to [migration guide](docs/migration-to-0.7.0.md) in order to migrate from version `0.6.0` or `0.6.1`.

## DSL
### The example of Kover configuration for Kotlin/JVM or Kotlin/MPP projects is given below

```groovy
kover {
    // disable()

    excludeJavaCode()

    useKoverTool()

    excludeInstrumentation {
        classes("com.example.subpackage.*")
    }

    excludeTests {
        tasks("myTest")
    }
}

koverReport {
    // common filters for all reports
    filters {
        // exclusions for reports
        excludes {
            // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
            classes("com.example.*")
            // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
            packages("com.another.subpackage")
            // excludes all classes and functions, annotated by specified annotations, wildcards '*' and '?' are available
            annotatedBy("*Generated*")
        }

        // inclusions for reports
        includes {
            // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
            classes("com.example.*")
            // includes all classes located in specified package and it subpackages
            packages("com.another.subpackage")
        }
    }

    // configure default reports - for Kotlin/JVM or Kotlin/MPP projects or merged android variants  
    defaults {
        // configure XML report
        xml {
            //  generate an XML report when running the `check` task
            onCheck = false

            // XML report file
            setReportFile(layout.buildDirectory.file("my-project-report/result.xml"))

            // overriding filters only for the XML report 
            filters {
                // exclusions for XML reports
                excludes {
                    // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("com.example.*")
                    // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
                    packages("com.another.subpackage")
                    // excludes all classes and functions, annotated by specified annotations, wildcards '*' and '?' are available
                    annotatedBy("*Generated*")
                }

                // inclusions for XML reports
                includes {
                    // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("com.example.*")
                    // includes all classes located in specified package and it subpackages
                    packages("com.another.subpackage")
                }
            }
        }

        // configure HTML report
        html {
            // custom header in HTML reports, project path by default
            title = "My report title"

            //  generate a HTML report when running the `check` task
            onCheck = false

            // directory for HTML report
            setReportDir(layout.buildDirectory.dir("my-project-report/html-result"))

            // overriding filters only for the HTML report
            filters {
                // exclusions for HTML reports
                excludes {
                    // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("com.example.*")
                    // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
                    packages("com.another.subpackage")
                    // excludes all classes and functions, annotated by specified annotations, wildcards '*' and '?' are available
                    annotatedBy("*Generated*")
                }

                // inclusions for HTML reports
                includes {
                    // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("com.example.*")
                    // includes all classes located in specified package and it subpackages
                    packages("com.another.subpackage")
                }
            }
        }

        // configure verification
        verify {
            //  verify coverage when running the `check` task
            onCheck = true

            // add verification rule
            rule {
                // check this rule during verification 
                isEnabled = true

                // specify the code unit for which coverage will be aggregated 
                entity = kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION

                // overriding filters only for current rule
                filters {
                    excludes {
                        // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                        classes("com.example.*")
                        // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
                        packages("com.another.subpackage")
                        // excludes all classes and functions, annotated by specified annotations, wildcards '*' and '?' are available
                        annotatedBy("*Generated*")
                    }
                    includes {
                        // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                        classes("com.example.*")
                        // includes all classes located in specified package and it subpackages
                        packages("com.another.subpackage")
                    }
                }

                // specify verification bound for this rule
                bound {
                    // lower bound
                    minValue = 1

                    // upper bound
                    maxValue = 99

                    // specify which units to measure coverage for
                    metric = kotlinx.kover.gradle.plugin.dsl.MetricType.LINE

                    // specify an aggregating function to obtain a single value that will be checked against the lower and upper boundaries
                    aggregation = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                }

                // add lower bound for percentage of covered lines
                minBound(2)

                // add upper bound for percentage of covered lines
                maxBound(98)
            }
        }
    }
}
```

### Example for Kotlin + Android projects 
```groovy
kover {
    // disable()

    excludeJavaCode()

    useKoverTool()

    excludeInstrumentation {
        classes("com.example.subpackage.*")
    }

    excludeTests {
        tasks("myTest")
    }
}

koverReport {
    // common filters for all reports of all variants
    filters {
        // exclusions for reports
        excludes {
            // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
            classes("com.example.*")
            // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
            packages("com.another.subpackage")
            // excludes all classes and functions, annotated by specified annotations, wildcards '*' and '?' are available
            annotatedBy("*Generated*")
        }

        // inclusions for reports
        includes {
            // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
            classes("com.example.*")
            // includes all classes located in specified package and it subpackages
            packages("com.another.subpackage")
        }
    }

    defaults {
        // add reports of 'release' Android build variant to default reports - generated by tasks `koverXmlReport`, `koverHtmlReport` etc
        mergeWith("release")
    }
    
    // configure report for `release` build variant (Build Type + Flavor) - generated by tasks `koverXmlReportRelease`, `koverHtmlReportRelease` etc
    androidReports("release") {
        // configure XML report for `release` build variant (task `koverXmlReportRelease`)
        xml {
            //  generate an XML report when running the `check` task
            onCheck = false

            // XML report file
            setReportFile(layout.buildDirectory.file("my-project-report/result.xml"))

            // overriding filters only for the XML report 
            filters {
                // exclusions for XML reports
                excludes {
                    // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("com.example.*")
                    // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
                    packages("com.another.subpackage")
                    // excludes all classes and functions, annotated by specified annotations, wildcards '*' and '?' are available
                    annotatedBy("*Generated*")
                }

                // inclusions for XML reports
                includes {
                    // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("com.example.*")
                    // includes all classes located in specified package and it subpackages
                    packages("com.another.subpackage")
                }
            }
        }

        // configure HTML report for `release` build variant (task `koverHtmlReportRelease`)
        html {
            // custom header in HTML reports, project path by default
            title = "My report title"

            //  generate a HTML report when running the `check` task
            onCheck = false

            // directory for HTML report
            setReportDir(layout.buildDirectory.dir("my-project-report/html-result"))

            // overriding filters only for the HTML report
            filters {
                // exclusions for HTML reports
                excludes {
                    // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("com.example.*")
                    // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
                    packages("com.another.subpackage")
                    // excludes all classes and functions, annotated by specified annotations, wildcards '*' and '?' are available
                    annotatedBy("*Generated*")
                }

                // inclusions for HTML reports
                includes {
                    // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                    classes("com.example.*")
                    // includes all classes located in specified package and it subpackages
                    packages("com.another.subpackage")
                }
            }
        }

        // configure verification for `release` build variant (task `koverVerifyRelease`)
        verify {
            //  verify coverage when running the `check` task
            onCheck = true

            // add verification rule
            rule {
                // check this rule during verification 
                isEnabled = true

                // specify the code unit for which coverage will be aggregated 
                entity = kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION

                // overriding filters only for current rule
                filters {
                    excludes {
                        // excludes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                        classes("com.example.*")
                        // excludes all classes located in specified package and it subpackages, wildcards '*' and '?' are available
                        packages("com.another.subpackage")
                        // excludes all classes and functions, annotated by specified annotations, wildcards '*' and '?' are available
                        annotatedBy("*Generated*")
                    }
                    includes {
                        // includes class by fully-qualified JVM class name, wildcards '*' and '?' are available
                        classes("com.example.*")
                        // includes all classes located in specified package and it subpackages
                        packages("com.another.subpackage")
                    }
                }

                // specify verification bound for this rule
                bound {
                    // lower bound
                    minValue = 1

                    // upper bound
                    maxValue = 99

                    // specify which units to measure coverage for
                    metric = kotlinx.kover.gradle.plugin.dsl.MetricType.LINE

                    // specify an aggregating function to obtain a single value that will be checked against the lower and upper boundaries
                    aggregation = kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
                }

                // add lower bound for percentage of covered lines
                minBound(2)

                // add upper bound for percentage of covered lines
                maxBound(98)
            }
        }
    }
}
```

### To create report combining coverage info from different projects
You have to add dependency on the project, in which the report task will be run
```groovy
dependencies {
  kover(project(":another:project"))
}
```

in this case report will be generated for current project joined with `:another:project` project.

More examples can be found in [example folder](examples)
