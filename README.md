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
The example of Kover configuration is given below
```

kover {
    disabledForProject = false

    useKoverTool()

    excludeInstrumentation {
        classes("com.example.Foo*", "*Bar?")
        packages("com.example.subpackage")
    }
}

koverReport {
    filters {
        excludes {
            classes("com.example.Foo*", "*Bar?")
            packages("com.example.subpackage")
            annotatedBy("*Generated*", "com.example.ExcludeKover")
        }
        includes {
            classes("com.example.Biz"")
            packages("com.example")
        }
    }

    xml {
        onCheck = false
        setReportFile(layout.buildDirectory.file("my-project-report/result.xml"))

        filters {
            excludes {
                classes("com.example.xml.Foo*", "*Bar?")
                packages("com.example.subpackage.xml")
                annotatedBy("*Generated*", "com.example.xml.ExcludeKover")
            }
            includes {
                classes("com.example.xml.Biz"")
                packages("com.example.xml")
            }
        }
    }

    html {
        title = "My report title"
        onCheck = false
        setReportDir(layout.buildDirectory.dir("my-project-report/html-result"))

        filters {
            excludes {
                classes("com.example.html.Foo*", "*Bar?")
                packages("com.example.subpackage.html")
                annotatedBy("*Generated*", "com.example.html.ExcludeKover")
            }
            includes {
                classes("com.example.html.Biz"")
                packages("com.example.html")
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
                    classes("com.example.verify.Foo*", "*Bar?")
                    packages("com.example.subpackage.verify")
                    annotatedBy("*Generated*", "com.example.verify.ExcludeKover")
                }
                includes {
                    classes("com.example.verify.Biz"")
                    packages("com.example.verify")
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
```

To create report, combining coverage info from different projects, needs to add dependency to project, in which the report task will be run
```
dependencies {
  kover(project(":another:project"))
}
```

More examples can be found in [example folder](examples)
