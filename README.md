# Kotlinx-Kover
[![JetBrains incubator project](https://jb.gg/badges/incubator.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

**Kover** - Gradle plugin for Kotlin code coverage agents: [IntelliJ](https://github.com/JetBrains/intellij-coverage)
and [JaCoCo](https://github.com/jacoco/jacoco).

Minimal supported `Gradle` version: `6.4`. 

## Table of content
- [Features](#features)
- [Quickstart](#quickstart)
  - [Apply plugin to a single-module build](#apply-plugin-to-a-single-module-build)
    - [Applying plugins with the plugins DSL](#applying-plugins-with-the-plugins-dsl)
    - [Legacy Plugin Application: applying plugins with the buildscript block](#legacy-plugin-application-applying-plugins-with-the-buildscript-block)
  - [Apply plugin to a multi-module build](#apply-plugin-to-a-multi-module-build)
- [Configuration](#configuration)
  - [Configuring JVM test task](#configuring-jvm-test-task)
  - [Configuring aggregated reports](#configuring-aggregated-reports)
  - [Configuring module reports](#configuring-module-reports)
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
### Apply plugin to a single-module build
#### Applying plugins with the plugins DSL
In top level build file

<details open>
<summary>Kotlin</summary>

```kotlin
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.4.4"
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
plugins {
    id 'org.jetbrains.kotlinx.kover' version '0.4.4'
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
        classpath("org.jetbrains.kotlinx:kover:0.4.4")
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
        classpath 'org.jetbrains.kotlinx:kover:0.4.4'
    }
}
  
apply plugin: 'kover'    
```
</details>

### Apply plugin to a multi-module build
To apply the plugin to all Gradle modules, you just need to apply the plugin only to the root module, as shown [above](#apply-plugin-to-a-single-module-build).
Applying the plugin to submodules if you have already applied it to the root module will cause configuration error.

## Configuration

Once applied, the plugin can be used out of the box without additional configuration. 

However, in some cases, custom settings are needed - this can be done by configuring special extensions and tasks.


### Configuring JVM test task
If you need to disable or filter instrumentation for a some test task, you may configure the Kover extension for it.

For example, to configure a standard test task for Kotlin/JVM named `test`, you need to add the following code to the build script of the module where this task is declared

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.test {
    extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
        isEnabled = true
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
        enabled = true
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
                    isEnabled = true
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
                    enabled = true
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
Aggregated report provides report using combined classpath and coverage stats from the module in which plugin is applied and all its submodules.

If you need to change the name of the XML report file or HTML directory, you may configure the corresponding tasks in 
the module in which the plugin is applied (usually this is the root module).

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

### Configuring module reports
If you need to change the name of the XML report file or HTML directory for a specific module, you may configure 
the corresponding tasks in this module.


<details open>
<summary>Kotlin</summary>

```kotlin
tasks.koverHtmlModuleReport {
    isEnabled = true                        // false to disable report generation
    htmlReportDir.set(layout.buildDirectory.dir("my-module-report/html-result"))
}

tasks.koverXmlModuleReport {
    isEnabled = true                        // false to disable report generation
    xmlReportFile.set(layout.buildDirectory.file("my-module-report/result.xml"))
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
tasks.koverHtmlModuleReport {
    enabled = true                          // false to disable report generation
    htmlReportDir.set(layout.buildDirectory.dir("my-module-report/html-result"))
}

tasks.koverXmlModuleReport {
    enabled = true                          // false to disable report generation
    xmlReportFile.set(layout.buildDirectory.file("my-module-report/result.xml"))
}
```
</details>

You may collect all modules reports into one directory using `koverCollectModuleReports` task.
Also, you may specify custom directory to collect modules reports in the build file of the module in which the plugin 
is applied (usually this is the root module):

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.koverCollectModuleReports {
  outputDir.set(layout.buildDirectory.dir("all-modules-reports") )
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
tasks.koverCollectModuleReports {
  outputDir.set(layout.buildDirectory.dir("all-modules-reports") )
}
```
</details>

### Configuring entire plugin
In the module in which the plugin is applied, you need to add code:

<details open>
<summary>Kotlin</summary>

```kotlin
kover {
    isEnabled = true                        // false to disable instrumentation of all test tasks in all modules
    coverageEngine.set(kotlinx.kover.api.CoverageEngine.INTELLIJ) // change instrumentation agent and reporter
    intellijEngineVersion.set("1.0.640")    // change version of IntelliJ agent and reporter
    jacocoEngineVersion.set("0.8.7")        // change version of JaCoCo agent and reporter
    generateReportOnCheck.set(true)         // false to do not execute `koverReport` task before `check` task
    disabledModules = setOf()               // setOf("module-name") to disable coverage for module with name `module-name`
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
kover {
    enabled = true                          // false to disable instrumentation of all test tasks in all modules
    coverageEngine.set(kotlinx.kover.api.CoverageEngine.INTELLIJ) // change instrumentation agent and reporter
    intellijEngineVersion.set('1.0.640')    // change version of IntelliJ agent and reporter
    jacocoEngineVersion.set('0.8.7')        // change version of JaCoCo agent and reporter
    generateReportOnCheck.set(true)         // false to do not execute `koverReport` task before `check` task
    disabledModules = []                    // ["module-name"] to disable coverage for module with name `module-name`
}
```
</details>

## Verification
You may specify one or more rules that check the values of the code coverage counters.

Validation rules work for both types of agents.

*The plugin currently only supports line counter values.*


To add a rule check to cover the code of all modules, you need to add configuration to the module in which the plugin
is applied (usually this is the root module):

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

To add rules for code coverage checks for one specific module, you need to add a configuration to this module:

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.koverModuleVerify {
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
tasks.koverModuleVerify {
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
The plugin, when applied, automatically creates tasks for the module in which it is applied (usually this is the root module):
- `koverHtmlReport` - Generates code coverage HTML report for all enabled test tasks in all modules.
- `koverXmlReport` - Generates code coverage XML report for all enabled test tasks in all modules. 
- `koverReport` - Executes both `koverXmlReport` and `koverHtmlReport` tasks.  Executes before `check` task if property `generateReportOnCheck` for `KoverExtension` is `true` ([see](#configuring-entire-plugin)).
- `koverVerify` - Verifies code coverage metrics of all modules based on specified rules. Always executes before `check` task.
- `koverCollectModuleReports` - Collects all modules reports into one directory. Default directory is `$buildDir/reports/kover/modules`, names for XML reports and dirs for HTML are modules names. Executing this task does not run `koverXmlReport` or `koverHtmlReport`, it only copies previously created reports if they exist to the output directory.

Tasks that are created for all modules:
- `koverHtmlModuleReport` - Generates code coverage HTML report for all enabled test tasks in one module.
- `koverXmlModuleReport` - Generates code coverage XML report for all enabled test tasks in one module.
- `koverModuleReport` - Executes both `koverXmlModuleReport` and `koverHtmlModuleReport` tasks.
- `koverModuleVerify` - Verifies code coverage metrics of one module based on specified rules. Always executes before `check` task.


## Implicit plugin dependencies
During the applying of the plugin, the artifacts of the JaCoCo or IntelliJ toolkit are dynamically loaded. They are downloaded from the `mavenCentral` repository.

For the plugin to work correctly, you need to make sure that the `mavenCentral` or its mirror is added to the list by the repository of the module in which the plugin is applied (usually this is the root module) and add it if necessary.

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
