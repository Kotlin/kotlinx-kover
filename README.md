# Kover

[![Kotlin Alpha](https://kotl.in/badges/alpha.svg)](https://kotlinlang.org/docs/components-stability.html)
[![JetBrains incubator project](https://jb.gg/badges/incubator.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

Kotlin Code Coverage Toolset

For more information about Kover Gradle Plugin, please refer to the [documentation of the latest release](https://kotlin.github.io/kotlinx-kover/gradle-plugin).

For more information about Kover CLI, please refer to the [documentation of the latest release](https://kotlin.github.io/kotlinx-kover/cli).

## Features

* Collection of code coverage through `JVM` tests (JS and native targets are not supported yet).
* Generating `HTML` and `XML` reports.
* Support for `Kotlin JVM`, `Kotlin Multiplatform` projects.
* Support for `Kotlin Android` projects with build variants (instrumentation tests executing on the Android device are not supported yet).
* Support mixed `Kotlin` and `Java` sources
* Verification rules with bounds in the Gradle plugin to keep track of coverage.
* Using JaCoCo library in Gradle plugin as an alternative for coverage measuring and report generation.
* Offline instrumentation of class files.
* Instrumentation and report generation using Command Line Interface

## Gradle Quickstart

The recommended way of applying Kover is with the
[plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block).

Minimum supported version of `Gradle` is `6.8`.

Add the following to your top-level build file:

<details open>
<summary>Kotlin</summary>

```kotlin
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.7.6"
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
plugins {
    id 'org.jetbrains.kotlinx.kover' version '0.7.6'
}
```
</details>

After you applied Kover Gradle plugin, [Kover tasks](https://kotlin.github.io/kotlinx-kover/gradle-plugin#kover-tasks) will be created for generating reports and verification. 
E.g. to generate HTML report for non-Android project run `./gradlew koverHtmlReport` - this will automatically start code compilation, execution of instrumented tests, and an HTML report will be generated with measurement results in the build folder.

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
        classpath("org.jetbrains.kotlinx:kover-gradle-plugin:0.7.6")
    }
}

apply(plugin = "org.jetbrains.kotlinx.kover")

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
        classpath 'org.jetbrains.kotlinx:kover-gradle-plugin:0.7.6'
    }
}
  
apply plugin: 'org.jetbrains.kotlinx.kover'
```
</details>


### To create report combining coverage info from different Gradle projects
You have to add dependency on the project, in which the report task will be run
```groovy
dependencies {
  kover(project(":another:project"))
}
```

in this case report will be generated for current project joined with `:another:project` project.

**More examples of Gradle plugin applying can be found in [example folder](kover-gradle-plugin/examples)**

## Building locally and Contributing

See [Contributing Guidelines](https://github.com/Kotlin/kotlinx-kover/tree/main/CONTRIBUTING.md).

