# Kotlinx-Kover

**Kover** - Gradle plugin for Kotlin code coverage agents: [IntelliJ](https://github.com/JetBrains/intellij-coverage)
and [JaCoCo](https://github.com/jacoco/jacoco).

Minimal supported `Gradle` version: `6.4`. 

## Table of content
- [Features](#features)
- [Quickstart](#quickstart)
  - [Apply plugin to single-module project](#apply-plugin-to-single-module-project)
    - [Applying plugins with the plugins DSL](#applying-plugins-with-the-plugins-dsl)
    - [Legacy Plugin Application: applying plugins with the buildscript block](#legacy-plugin-application-applying-plugins-with-the-buildscript-block)
  - [Apply plugin to multi-module project](#apply-plugin-to-multi-module-project)
- [Configuration](#configuration)
  - [Configuring JVM test task](#configuring-jvm-test-task)
  - [Configuring reports](#configuring-reports)
  - [Configuring reports collecting](#configuring-reports-collecting)
  - [Configuring entire plugin](#configuring-entire-plugin)
 - [Verification](#verification)
 - [Tasks](#tasks)

## Features

* Collecting the code coverage for `JVM` test tasks
* `XML` and `HTML` reports generation
* Support of `Kotlin/JVM`, `Kotlin Multiplatform` and mixed `Kotlin-Java` sources with zero additional configuration
* `Kotlin Android` support without dividing them into build types and flavours
* Customizable filters for instrumented classes

## Quickstart
### Apply plugin to single-module project
#### Applying plugins with the plugins DSL
In top level build file

<details open>
<summary>Kotlin</summary>

```kotlin
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.4.2"
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
plugins {
    id 'org.jetbrains.kotlinx.kover' version '0.4.2'
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
        classpath("org.jetbrains.kotlinx:kover:0.4.2")
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
        classpath 'org.jetbrains.kotlinx:kover:0.4.2'
    }
}
  
apply plugin: 'kover'    
```
</details>

### Apply plugin to multi-module project
To apply the plugin to all modules in the project, you need to apply the plugin only to the root module, as shown [above](#apply-plugin-to-single-module-project).

**There are no dependencies between tasks from different modules, they are executed independently.**

**Cross-module tests are not supported in reports and validation yet. For each test, only the classpath belonging to the current module is taken.**

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
        includes = listOf("com\\.example\\..*")
        excludes = listOf("com\\.example\\.subpackage\\..*")
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
        includes = ['com\\.example\\..*']
        excludes = ['com\\.example\\.subpackage\\..*']
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
                    includes = listOf("com\\.example\\..*")
                    excludes = listOf("com\\.example\\.subpackage\\..*")
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
                    includes = ['com\\.example\\..*']
                    excludes = ['com\\.example\\.subpackage\\..*']
                }
            }
        }
    }
}
```
</details>


### Configuring reports
If you need to change the name of the XML report file or HTML directory, you may configure the corresponding tasks

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.koverHtmlReport {
    isEnabled = true                        // false to disable report generation
    htmlReportDir.set(layout.buildDirectory.dir("my-reports/html-result"))
}

tasks.koverXmlReport {
    isEnabled = true                        // false to disable report generation
    xmlReportFile.set(layout.buildDirectory.file("my-reports/result.xml"))
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

### Configuring reports collecting
You may specify custom directory to collect reports from all modules in the build file of the root module:

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.koverCollectReports {
  outputDir.set(layout.buildDirectory.dir("my-reports-dir") )
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
tasks.koverCollectReports {
  outputDir.set(layout.buildDirectory.dir("my-reports-dir") )
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
    intellijEngineVersion.set("1.0.622")    // change version of IntelliJ agent and reporter
    jacocoEngineVersion.set("0.8.7")        // change version of JaCoCo agent and reporter
    generateReportOnCheck.set(true)         // false to do not execute `koverReport` task before `check` task
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
kover {
    enabled = true                          // false to disable instrumentation of all test tasks in all modules
    coverageEngine.set(kotlinx.kover.api.CoverageEngine.INTELLIJ) // change instrumentation agent and reporter
    intellijEngineVersion.set('1.0.622')    // change version of IntelliJ agent and reporter
    jacocoEngineVersion.set('0.8.7')        // change version of JaCoCo agent and reporter
    generateReportOnCheck.set(true)         // false to do not execute `koverReport` task before `check` task
}
```
</details>

###### Verification
For all test task of module, you can specify one or more rules that check the values of the code coverage counters.

Validation rules work for both types of agents.

*The plugin currently only supports line counter values.*

In the build file of the verified module:

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.koverVerify {
    rule {
        name = "The project has upper limit on lines covered"
        bound {
            maxValue = 100000
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
        name = "Minimal line coverage rate in percents"
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
        name = "The project doesn't has upper limit on lines covered"
        bound {
            maxValue = 100000
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
        name = "Minimal line coverage rate in percents"
        bound {
            minValue = 50
            // valueType is 'COVERED_LINES_PERCENTAGE' by default
        }
    }
}
```
</details>

###### Tasks
The plugin, when applied, automatically creates tasks for the module (all modules, if the project is multi-module, and it applied in root build script):
- `koverXmlReport` - Generates code coverage XML report for all module's test tasks.
- `koverHtmlReport` - Generates code coverage HTML report for all module's test tasks.
- `koverReport` - Executes both `koverXmlReport` and `koverHtmlReport` tasks.
- `koverCollectReports` - Collects reports from all submodules in one directory. Default directory is `$buildDir/reports/kover/all`, names for XML reports and dirs for HTML are projects names. Executing this task does not run `koverXmlReport` or `koverHtmlReport`, it only copies previously created reports if they exist to the output directory.
- `koverVerify` - Verifies code coverage metrics based on specified rules. Always executes before `check` task.
