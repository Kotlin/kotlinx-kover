# Kotlinx-Kover
[![JetBrains incubator project](https://jb.gg/badges/incubator.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

**Kover** - Gradle plugin for Kotlin code coverage agents: [IntelliJ](https://github.com/JetBrains/intellij-coverage)
and [JaCoCo](https://github.com/jacoco/jacoco).

Minimal supported `Gradle` version: `6.4`. 

## Table of contents
- [Features](#features)
- [Quickstart](#quickstart)
  - [Apply plugin to a single-project build](#apply-plugin-to-a-single-project-build)
    - [Applying plugins with the plugins DSL](#applying-plugins-with-the-plugins-dsl)
    - [Legacy Plugin Application: applying plugins with the buildscript block](#legacy-plugin-application-applying-plugins-with-the-buildscript-block)
  - [Apply plugin to a multi-project build](#apply-plugin-to-a-multi-project-build)
- [Configuration](#configuration)
  - [Configuring JVM test task](#configuring-jvm-test-task)
  - [Configuring merged reports](#configuring-merged-reports)
  - [Configuring project reports](#configuring-project-reports)
  - [Configuring entire plugin](#configuring-entire-plugin)
- [Verification](#verification)
- [Tasks](#tasks)
- [Implicit plugin dependencies](#implicit-plugin-dependencies)

## Features

* Collecting code coverage for `JVM` test tasks
* `XML` and `HTML` report generation
* Support of `Kotlin/JVM`, `Kotlin Multiplatform` and mixed `Kotlin-Java` sources with zero additional configuration
* `Kotlin Android` support without the need to divide into build types and flavours
* Customizable filters for instrumented classes

## Quickstart
### Apply plugin to a single-project build
#### Applying plugins with the plugins DSL
In top-level build file:

<details open>
<summary>Kotlin</summary>

```kotlin
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.5.0"
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
plugins {
    id 'org.jetbrains.kotlinx.kover' version '0.5.0'
}
```
</details>

#### Legacy Plugin Application: applying plugins with the buildscript block
In top-level build file:

<details open>
<summary>Kotlin</summary>

```kotlin
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlinx:kover:0.5.0")
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
        classpath 'org.jetbrains.kotlinx:kover:0.5.0'
    }
}
  
apply plugin: 'kover'    
```
</details>

### Apply plugin to a multi-project build
To apply the plugin to all Gradle projects, you only need to apply the plugin to the top-level build file as shown [above](#apply-plugin-to-a-single-project-build).
Applying the plugin to subprojects if you have already applied it to the root project will cause configuration errors.

## Configuration

Once applied, the Kover plugin can be used out of the box without additional configuration. 

However, in some cases, custom settings are needed - this can be done by configuring special extensions and tasks.


### Configuring JVM test task
If you need to disable or filter instrumentation for a test task, you may configure the Kover extension for it.

For example, to configure a standard test task for Kotlin/JVM named `test`, you need to add the following code to the build script of the project where this task is declared:

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

**For other platforms (Android, Kotlin-Multiplatform) the names may differ and you may also have several test tasks, so you first need to determine the name of the required task.**

Example of configuring test task for build type `debug` in Android:
<details open>
<summary>Kotlin</summary>

```kotlin
android {
    // other Android declarations

    testOptions {
        unitTests.all {
            if (it.name == "testDebugUnitTest") {
                it.extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
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


### Configuring merged reports
Merged reports combine classpath and coverage stats from the project in which the plugin is applied and all of its subprojects.

If you need to change the name of the XML report file or HTML directory, you may configure the corresponding tasks in 
the project in which the plugin is applied (usually this is the root project):

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.koverMergedHtmlReport {
    isEnabled = true                        // false to disable report generation
    htmlReportDir.set(layout.buildDirectory.dir("my-merged-report/html-result"))

    includes = listOf("com.example.*")            // inclusion rules for classes
    excludes = listOf("com.example.subpackage.*") // exclusion rules for classes
}

tasks.koverMergedXmlReport {
    isEnabled = true                        // false to disable report generation
    xmlReportFile.set(layout.buildDirectory.file("my-merged-report/result.xml"))

    includes = listOf("com.example.*")            // inclusion rules for classes
    excludes = listOf("com.example.subpackage.*") // exclusion rules for classes  
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
tasks.koverMergedHtmlReport {
    enabled = true                          // false to disable report generation
    htmlReportDir.set(layout.buildDirectory.dir("my-merged-report/html-result"))

    includes = ['com.example.*']             // inclusion rules for classes
    excludes = ['com.example.subpackage.*']  // exclusion rules for classes
}

tasks.koverMergedXmlReport {
    enabled = true                          // false to disable report generation
    xmlReportFile.set(layout.buildDirectory.file("my-merged-report/result.xml"))

    includes = ['com.example.*']             // inclusion rules for classes
    excludes = ['com.example.subpackage.*']  // exclusion rules for classes
}
```
</details>

### Configuring project reports
If you need to change the name of the XML report file or HTML directory for a specific project, you may configure 
the corresponding tasks in this project:


<details open>
<summary>Kotlin</summary>

```kotlin
tasks.koverHtmlReport {
    isEnabled = true                        // false to disable report generation
    htmlReportDir.set(layout.buildDirectory.dir("my-project-report/html-result"))

    includes = listOf("com.example.*")            // inclusion rules for classes
    excludes = listOf("com.example.subpackage.*") // exclusion rules for classes  
}

tasks.koverXmlReport {
    isEnabled = true                        // false to disable report generation
    xmlReportFile.set(layout.buildDirectory.file("my-project-report/result.xml"))

    includes = listOf("com.example.*")            // inclusion rules for classes
    excludes = listOf("com.example.subpackage.*") // exclusion rules for classes 
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
tasks.koverHtmlReport {
    enabled = true                          // false to disable report generation
    htmlReportDir.set(layout.buildDirectory.dir("my-project-report/html-result"))

    includes = ['com.example.*']             // inclusion rules for classes
    excludes = ['com.example.subpackage.*']  // exclusion rules for classes  
}

tasks.koverXmlReport {
    enabled = true                          // false to disable report generation
    xmlReportFile.set(layout.buildDirectory.file("my-project-report/result.xml"))
    includes = ['com.example.*']             // inclusion rules for classes
    excludes = ['com.example.subpackage.*']  // exclusion rules for classes  
}
```
</details>

By default, for tasks `koverHtmlReport` and `koverXmlReport` coverage is calculated only for the tests of the one project.
If classes or functions are called from tests of another module, then you need to set a flag `runAllTestsForProjectTask` for `KoverExtension` to `true` ([see](#configuring-entire-plugin)).

**In this case, then running tasks `koverHtmlReport` or `koverXmlReport` will trigger the execution of all active tests from all projects!**


You may collect all project reports into one directory using the `koverCollectReports` task.
Also, you may specify a custom directory to collect project reports in the build directory of the project in which the plugin 
is applied (usually this is the root project):

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.koverCollectReports {
  outputDir.set(layout.buildDirectory.dir("all-projects-reports") )
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
tasks.koverCollectReports {
  outputDir.set(layout.buildDirectory.dir("all-projects-reports") )
}
```
</details>

### Configuring entire plugin
In the project in which the plugin is applied, you can configure the following properties:

<details open>
<summary>Kotlin</summary>

```kotlin
kover {
    isDisabled = false                      // true to disable instrumentation of all test tasks in all projects
    coverageEngine.set(kotlinx.kover.api.CoverageEngine.INTELLIJ) // change instrumentation agent and reporter
    intellijEngineVersion.set("1.0.656")    // change version of IntelliJ agent and reporter
    jacocoEngineVersion.set("0.8.7")        // change version of JaCoCo agent and reporter
    generateReportOnCheck = true            // false to do not execute `koverMergedReport` task before `check` task
    disabledProjects = setOf()              // setOf("project-name") to disable coverage for project with name `project-name`
    instrumentAndroidPackage = false        // true to instrument packages `android.*` and `com.android.*`
    runAllTestsForProjectTask = false       // true to run all tests in all projects if `koverHtmlReport`, `koverXmlReport`, `koverReport`, `koverVerify` or `check` tasks executed on some project
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
kover {
    disabled = false                        // true to disable instrumentation of all test tasks in all projects
    coverageEngine.set(kotlinx.kover.api.CoverageEngine.INTELLIJ) // change instrumentation agent and reporter
    intellijEngineVersion.set('1.0.656')    // change version of IntelliJ agent and reporter
    jacocoEngineVersion.set('0.8.7')        // change version of JaCoCo agent and reporter
    generateReportOnCheck = true            // false to do not execute `koverMergedReport` task before `check` task
    disabledProjects = []                   // ["project-name"] to disable coverage for project with name `project-name`
    instrumentAndroidPackage = false        // true to instrument packages `android.*` and `com.android.*`
    runAllTestsForProjectTask = false       // true to run all tests in all projects if `koverHtmlReport`, `koverXmlReport`, `koverReport`, `koverVerify` or `check` tasks executed on some project
}
```
</details>

## Verification
You may specify one or more rules that check the values of the code coverage counters.

Validation rules work for both types of agents.

*The plugin currently only supports line counter values.*


To add a rule to check coverage of the code of all projects, you need to add configuration to the project in which the plugin
is applied (usually this is the root project):

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.koverMergedVerify {
    includes = listOf("com.example.*")            // inclusion rules for classes
    excludes = listOf("com.example.subpackage.*") // exclusion rules for classes
  
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
tasks.koverMergedVerify {
    includes = ['com.example.*']             // inclusion rules for classes
    excludes = ['com.example.subpackage.*']  // exclusion rules for classes
  
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

To add rules for code coverage checks for the code of one specific project, you need to add a configuration to this project:

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.koverVerify {
    includes = listOf("com.example.*")            // inclusion rules for classes
    excludes = listOf("com.example.subpackage.*") // exclusion rules for classes

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
tasks.koverVerify {
    includes = ['com.example.*']             // inclusion rules for classes
    excludes = ['com.example.subpackage.*']  // exclusion rules for classes
  
    rule {
        name = "Minimal line coverage rate in percent"
        bound {
            minValue = 75
        }
    }
}
```
</details>

By default, for the task `koverVerify` coverage is calculated only for the tests of the one project. 
If classes or functions are called from tests of another module, then you need to set a flag `runAllTestsForProjectTask` for `KoverExtension` to `true` ([see](#configuring-entire-plugin)). 

**In this case, if verification rules are added, then running tasks `koverVerify` or `check` will trigger the execution of all active tests from all projects!**

## Tasks
The plugin, when applied, automatically creates tasks for the project in which it is applied (usually this is the root project):
- `koverMergedHtmlReport` - Generates code coverage HTML report for all enabled test tasks in all projects.
- `koverMergedXmlReport` - Generates code coverage XML report for all enabled test tasks in all projects. 
- `koverMergedReport` - Executes both `koverMergedXmlReport` and `koverMergedHtmlReport` tasks.  Executes before `check` task if property `generateReportOnCheck` for `KoverExtension` is `true` ([see](#configuring-entire-plugin)).
- `koverMergedVerify` - Verifies code coverage metrics of all projects based on specified rules. Always executes before `check` task.
- `koverCollectReports` - Collects all projects reports into one directory. Default directory is `$buildDir/reports/kover/projects`, names for XML reports and dirs for HTML are projects names. Executing this task does not run `koverMergedXmlReport` or `koverMergedHtmlReport`, it only copies previously created reports if they exist to the output directory.

Tasks that are created for all projects:
- `koverHtmlReport` - Generates code coverage HTML report for all enabled test tasks in one project.
- `koverXmlReport` - Generates code coverage XML report for all enabled test tasks in one project.
- `koverReport` - Executes both `koverXmlReport` and `koverHtmlReport` tasks.
- `koverVerify` - Verifies code coverage metrics of one project based on specified rules. Always executes before `check` task.


## Implicit plugin dependencies
While the plugin is being applied, the artifacts of the JaCoCo or IntelliJ toolkit are dynamically loaded. They are downloaded from the `mavenCentral` repository.

For the plugin to work correctly, you need to make sure that the `mavenCentral` (or its mirror) is added to the repository list of the project in which the plugin is applied, if it doesn't already exist (usually this is the root project):

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
