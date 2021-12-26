# Kotlinx-Kover
[![JetBrains incubator project](https://jb.gg/badges/incubator.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

**Kover** - Gradle plugin for Kotlin code coverage agents: [IntelliJ](https://github.com/JetBrains/intellij-coverage)
and [JaCoCo](https://github.com/jacoco/jacoco).

Minimal supported `Gradle` version: `6.4`. 

## Table of content
- [Features](#features)
- [Quickstart](#quickstart)
  - [Apply plugin to a single-project build](#apply-plugin-to-a-single-project-build)
    - [Applying plugins with the plugins DSL](#applying-plugins-with-the-plugins-dsl)
    - [Legacy Plugin Application: applying plugins with the buildscript block](#legacy-plugin-application-applying-plugins-with-the-buildscript-block)
  - [Apply plugin to a multi-project build](#apply-plugin-to-a-multi-project-build)
- [Configuration](#configuration)
  - [Configuring JVM test task](#configuring-jvm-test-task)
  - [Configuring aggregated reports](#configuring-aggregated-reports)
  - [Configuring project reports](#configuring-project-reports)
  - [Configuring entire plugin](#configuring-entire-plugin)
- [Verification](#verification)
- [Tasks](#tasks)
- [Implicit plugin dependencies](#implicit-plugin-dependencies)

## Features

* Collecting the code coverage for `JVM` test tasks
* `XML` and `HTML` reports generation
* Support of `Kotlin/JVM`, `Kotlin Multiplatform` and mixed `Kotlin-Java` sources with zero additional configuration
* `Kotlin Android` support without dividing them into build types and flavours
* Customizable filters for instrumented classes

## Quickstart
### Apply plugin to a single-project build
#### Applying plugins with the plugins DSL
In top level build file

<details open>
<summary>Kotlin</summary>

```kotlin
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.5.0-RC"
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
plugins {
    id 'org.jetbrains.kotlinx.kover' version '0.5.0-RC'
}
```
</details>

#### Legacy Plugin Application: applying plugins with the buildscript block
In top level build file

<details open>
<summary>Kotlin</summary>

```kotlin
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlinx:kover:0.5.0-RC")
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
        classpath 'org.jetbrains.kotlinx:kover:0.5.0-RC'
    }
}
  
apply plugin: 'kover'    
```
</details>

### Apply plugin to a multi-project build
To apply the plugin to all Gradle projects, you just need to apply the plugin only to the root project, as shown [above](#apply-plugin-to-a-single-project-build).
Applying the plugin to subprojects if you have already applied it to the root project will cause configuration error.

## Configuration

Once applied, the plugin can be used out of the box without additional configuration. 

However, in some cases, custom settings are needed - this can be done by configuring special extensions and tasks.


### Configuring JVM test task
If you need to disable or filter instrumentation for a some test task, you may configure the Kover extension for it.

For example, to configure a standard test task for Kotlin/JVM named `test`, you need to add the following code to the build script of the project where this task is declared

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.test {
    extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
        isDisabled = false
        binaryReportFile.set(file("$buildDir/custom/result.bin"))
        includes = listOf("com.example.*")
        excludes = listOf("com.example.subpackage.*")
    }
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
tasks.test {
    kover {
        disabled = false
        binaryReportFile.set(file("$buildDir/custom/result.bin"))
        includes = ['com.example.*']
        excludes = ['com.example.subpackage.*']
    }
}
```
</details>

**For other platforms (Android, Kotlin-Multiplatform) the name may differ, you may also have several test tasks, so you first need to determine the name of the required task.**

Example of configuring test task for build type `debug` in Android:
<details open>
<summary>Kotlin</summary>

```kotlin
android {
    // other Android declarations

    testOptions {
        unitTests.all {
            if (it.name == "testDebugUnitTest") {
                extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
                    isDisabled = false
                    binaryReportFile.set(file("$buildDir/custom/debug-report.bin"))
                    includes = listOf("com.example.*")
                    excludes = listOf("com.example.subpackage.*")
                }
            }
        }
    }
}
```
    
</details>

<details>
<summary>Groovy</summary>

```groovy
android {
    // other Android declarations

    testOptions {
        unitTests.all {
            if (name == "testDebugUnitTest") {
                kover {
                    disabled = false
                    binaryReportFile.set(file("$buildDir/custom/debug-report.bin"))
                    includes = ['com.example.*']
                    excludes = ['com.example.subpackage.*']
                }
            }
        }
    }
}
```
</details>


### Configuring aggregated reports
Aggregated report provides report using combined classpath and coverage stats from the project in which plugin is applied and all its subprojects.

If you need to change the name of the XML report file or HTML directory, you may configure the corresponding tasks in 
the project in which the plugin is applied (usually this is the root project).

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.koverHtmlReport {
    isEnabled = true                        // false to disable report generation
    htmlReportDir.set(layout.buildDirectory.dir("my-agg-report/html-result"))
}

tasks.koverXmlReport {
    isEnabled = true                        // false to disable report generation
    xmlReportFile.set(layout.buildDirectory.file("my-agg-report/result.xml"))
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
tasks.koverHtmlReport {
    enabled = true                          // false to disable report generation
    htmlReportDir.set(layout.buildDirectory.dir("my-reports/html-result"))
}

tasks.koverXmlReport {
    enabled = true                          // false to disable report generation
    xmlReportFile.set(layout.buildDirectory.file("my-reports/result.xml"))
}
```
</details>

### Configuring project reports
If you need to change the name of the XML report file or HTML directory for a specific project, you may configure 
the corresponding tasks in this project.


<details open>
<summary>Kotlin</summary>

```kotlin
tasks.koverHtmlProjectReport {
    isEnabled = true                        // false to disable report generation
    htmlReportDir.set(layout.buildDirectory.dir("my-project-report/html-result"))
}

tasks.koverXmlProjectReport {
    isEnabled = true                        // false to disable report generation
    xmlReportFile.set(layout.buildDirectory.file("my-project-report/result.xml"))
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
tasks.koverHtmlProjectReport {
    enabled = true                          // false to disable report generation
    htmlReportDir.set(layout.buildDirectory.dir("my-project-report/html-result"))
}

tasks.koverXmlProjectReport {
    enabled = true                          // false to disable report generation
    xmlReportFile.set(layout.buildDirectory.file("my-project-report/result.xml"))
}
```
</details>

You may collect all projects reports into one directory using `koverCollectProjectsReports` task.
Also, you may specify custom directory to collect projects reports in the build file of the project in which the plugin 
is applied (usually this is the root project):

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.koverCollectProjectsReports {
  outputDir.set(layout.buildDirectory.dir("all-projects-reports") )
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
tasks.koverCollectProjectsReports {
  outputDir.set(layout.buildDirectory.dir("all-projects-reports") )
}
```
</details>

### Configuring entire plugin
In the project in which the plugin is applied, you need to add code:

<details open>
<summary>Kotlin</summary>

```kotlin
kover {
    isDisabled = false                      // true to disable instrumentation of all test tasks in all projects
    coverageEngine.set(kotlinx.kover.api.CoverageEngine.INTELLIJ) // change instrumentation agent and reporter
    intellijEngineVersion.set("1.0.640")    // change version of IntelliJ agent and reporter
    jacocoEngineVersion.set("0.8.7")        // change version of JaCoCo agent and reporter
    generateReportOnCheck.set(true)         // false to do not execute `koverReport` task before `check` task
    disabledProjects = setOf()              // setOf("project-name") to disable coverage for project with name `project-name`
    instrumentAndroidPackage = false        // true to instrument packages `android.*` and `com.android.*`
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
kover {
    disabled = false                        // true to disable instrumentation of all test tasks in all projects
    coverageEngine.set(kotlinx.kover.api.CoverageEngine.INTELLIJ) // change instrumentation agent and reporter
    intellijEngineVersion.set('1.0.640')    // change version of IntelliJ agent and reporter
    jacocoEngineVersion.set('0.8.7')        // change version of JaCoCo agent and reporter
    generateReportOnCheck.set(true)         // false to do not execute `koverReport` task before `check` task
    disabledProjects = []                   // ["project-name"] to disable coverage for project with name `project-name`
    instrumentAndroidPackage = false        // true to instrument packages `android.*` and `com.android.*`
}
```
</details>

## Verification
You may specify one or more rules that check the values of the code coverage counters.

Validation rules work for both types of agents.

*The plugin currently only supports line counter values.*


To add a rule check to cover the code of all projects, you need to add configuration to the project in which the plugin
is applied (usually this is the root project):

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.koverVerify {
    rule {
        name = "Minimum number of lines covered"
        bound {
            minValue = 100000
            valueType = kotlinx.kover.api.VerificationValueType.COVERED_LINES_COUNT
        }
    }
    rule {
        // rule without a custom name
        bound {
            minValue = 1
            maxValue = 1000
            valueType = kotlinx.kover.api.VerificationValueType.MISSED_LINES_COUNT
        }
    }
    rule {
        name = "Minimal line coverage rate in percent"
        bound {
            minValue = 50
            // valueType is kotlinx.kover.api.VerificationValueType.COVERED_LINES_PERCENTAGE by default
       }
    }
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
tasks.koverVerify {
    rule {
        name = "Minimum number of lines covered"
        bound {
            minValue = 100000
            valueType = 'COVERED_LINES_COUNT'
        }
    }
    rule {
        // rule without a custom name
        bound {
            minValue = 1
            maxValue = 1000
            valueType = 'MISSED_LINES_COUNT'
        }
    }
    rule {
        name = "Minimal line coverage rate in percent"
        bound {
            minValue = 50
            // valueType is 'COVERED_LINES_PERCENTAGE' by default
        }
    }
}
```
</details>

To add rules for code coverage checks for one specific project, you need to add a configuration to this project:

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.koverProjectVerify {
    rule {
        name = "Minimal line coverage rate in percent"
        bound {
            minValue = 75
       }
    }
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
tasks.koverProjectVerify {
    rule {
        name = "Minimal line coverage rate in percent"
        bound {
            minValue = 75
        }
    }
}
```
</details>


## Tasks
The plugin, when applied, automatically creates tasks for the project in which it is applied (usually this is the root project):
- `koverHtmlReport` - Generates code coverage HTML report for all enabled test tasks in all projects.
- `koverXmlReport` - Generates code coverage XML report for all enabled test tasks in all projects. 
- `koverReport` - Executes both `koverXmlReport` and `koverHtmlReport` tasks.  Executes before `check` task if property `generateReportOnCheck` for `KoverExtension` is `true` ([see](#configuring-entire-plugin)).
- `koverVerify` - Verifies code coverage metrics of all projects based on specified rules. Always executes before `check` task.
- `koverCollectProjectsReports` - Collects all projects reports into one directory. Default directory is `$buildDir/reports/kover/projects`, names for XML reports and dirs for HTML are projects names. Executing this task does not run `koverXmlReport` or `koverHtmlReport`, it only copies previously created reports if they exist to the output directory.

Tasks that are created for all projects:
- `koverHtmlProjectReport` - Generates code coverage HTML report for all enabled test tasks in one project.
- `koverXmlProjectReport` - Generates code coverage XML report for all enabled test tasks in one project.
- `koverProjectReport` - Executes both `koverXmlProjectReport` and `koverHtmlProjectReport` tasks.
- `koverProjectVerify` - Verifies code coverage metrics of one project based on specified rules. Always executes before `check` task.


## Implicit plugin dependencies
During the applying of the plugin, the artifacts of the JaCoCo or IntelliJ toolkit are dynamically loaded. They are downloaded from the `mavenCentral` repository.

For the plugin to work correctly, you need to make sure that the `mavenCentral` or its mirror is added to the list by the repository of the project in which the plugin is applied (usually this is the root project) and add it if necessary.

<details open>
<summary>Kotlin</summary>

```kotlin
repositories {
    mavenCentral()
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
repositories {
  mavenCentral()
}
```
</details>
