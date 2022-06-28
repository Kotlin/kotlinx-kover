# Kotlinx-Kover

[![Kotlin Alpha](https://kotl.in/badges/alpha.svg)](https://kotlinlang.org/docs/components-stability.html)
[![JetBrains incubator project](https://jb.gg/badges/incubator.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

**Kover** - Gradle plugin for Kotlin code coverage agents: [IntelliJ](https://github.com/JetBrains/intellij-coverage)
and [JaCoCo](https://github.com/jacoco/jacoco).

Minimal supported `Gradle` version: `6.6`.

## Table of contents
- [Features](#features)
- [Quickstart](#quickstart)
  - [Apply plugin to a project](#apply-plugin)
    - [Applying plugins with the plugins DSL](#applying-plugins-with-the-plugins-dsl)
    - [Legacy Plugin Application: applying plugins with the buildscript block](#legacy-plugin-application-applying-plugins-with-the-buildscript-block)
  - [Merged reports](#merged-reports)
- [Configuration](#configuration)
  - [Configuring project](#configuring-project)
  - [Configuring merged reports](#configuring-merged-reports)
  - [Configuring JVM test task](#configuring-jvm-test-task)
  - [Specifying Coverage Engine](#specifying-coverage-engine)
- [Tasks](#kover-default-tasks)
- [Implicit plugin dependencies](#implicit-plugin-dependencies)

## Features

* Collecting code coverage for `JVM` test tasks
* `XML` and `HTML` report generation
* Support of `Kotlin/JVM`, `Kotlin Multiplatform` and mixed `Kotlin-Java` sources with zero additional configuration
* `Kotlin Android` support without the need to divide into build types and flavours
* Customizable filters for instrumented classes

## Quickstart
### Apply plugin
#### Applying plugins with the plugins DSL
In top-level build file:

<details open>
<summary>Kotlin</summary>

```kotlin
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.6.0-RC"
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
plugins {
    id 'org.jetbrains.kotlinx.kover' version '0.6.0-RC'
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
        classpath("org.jetbrains.kotlinx:kover:0.6.0-RC")
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
        classpath 'org.jetbrains.kotlinx:kover:0.6.0-RC'
    }
}
  
apply plugin: 'kover'    
```
</details>

### Merged reports
Merged reports are reports that combine statistics of code coverage by test tasks from several projects.

At the same time, for each project, its configuration of instrumentation and special filters (non-class filters) is applied.

In all projects used for merged reports, a Kover plugin must be applied, as well as all Coverage Engines must match.

See how to enable merge reports in [this section](#configuring-merged-reports).

## Configuration

Once applied, the Kover plugin can be used out of the box without additional configuration. 

However, in some cases, custom settings are needed - this can be done by configuring special extensions and tasks.


### Configuring JVM test task
In rare cases, you may need to disable instrumentation for certain classes if it causes execution errors.
It may also be convenient to ignore the test task when calculating coverage.
You may configure the Kover extension for it.

For example, to configure a standard test task for Kotlin/JVM named `test`, you need to add the following code to the build script of the project where this task is declared:

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.test {
    extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
        isDisabled.set(false) // true to disable instrumentation tests of this task, Kover reports will not depend on the results of their execution 
        binaryReportFile.set(file("$buildDir/custom/result.bin")) // set file name of binary report
        includes = listOf("com.example.*") // see "Instrumentation inclusion rules" below
        excludes = listOf("com.example.subpackage.*") // see "Instrumentation exclusion rules" below
    }
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
tasks.test {
    kover {
        disabled = false // true to disable instrumentation tests of this task, Kover reports will not depend on the results of their execution 
        binaryReportFile.set(file("$buildDir/custom/result.bin")) // set file name of binary report
        includes = ['com.example.*'] // see "Instrumentation inclusion rules" below
        excludes = ['com.example.subpackage.*'] // see "Instrumentation exclusion rules" below
    }
}
```
</details>

**For other platforms (Android, Kotlin-Multiplatform) the names may differ, and you may also have several test tasks, so you first need to determine the name of the required task.**

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
                    isDisabled = false // true to disable instrumentation tests of this task, Kover reports will not depend on the results of their execution 
                    binaryReportFile.set(file("$buildDir/custom/debug-report.bin")) // set file name of binary report
                    includes = listOf("com.example.*") // see "Instrumentation inclusion rules" below
                    excludes = listOf("com.example.subpackage.*") // see "Instrumentation exclusion rules" below
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
                    disabled = false // true to disable instrumentation tests of this task, Kover reports will not depend on the results of their execution 
                    binaryReportFile.set(file("$buildDir/custom/debug-report.bin")) // set file name of binary report
                    includes = ['com.example.*'] // see "Instrumentation inclusion rules" below
                    excludes = ['com.example.subpackage.*'] // see "Instrumentation exclusion rules" below
                }
            }
        }
    }
}
```
</details>


**Instrumentation inclusion rules**

Only the specified classes may be instrumented, the remaining classes will still be present in the report, but for them the coverage will be zero.

**Instrumentation exclusion rules**

The specified classes will not be instrumented and there will be zero coverage for them.

Instrumentation inclusion/exclusion rule represented as a fully-qualified name of the class (several classes if wildcards are used).
File or directory names are not allowed.
It's possible to use `*` (zero or several of any chars) and `?` (one any char) wildcards. Wildcard `**`  is similar to the `*`.

Examples `my.package.ClassName` or `my.*.*Name` but not the `my/package/ClassName.kt` or  `src/my.**.ClassName`

Exclusion rules have priority over inclusion ones.


### Configuring project
In the project in which the plugin is applied, you can configure instrumentation and default Kover tasks:

<details open>
<summary>Kotlin</summary>

```kotlin
kover {
    isDisabled.set(false) // true to disable instrumentation and all Kover tasks in this project
    engine.set(DefaultIntellijEngine) // change Coverage Engine
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
        onCheck.set(false) // true to run koverXmlReport task during the execution of the check task
        reportFile.set(layout.buildDirectory.file("my-project-report/result.xml")) // change report file name
        overrideFilters { 
            classes { // override common class filter
                includes += "com.example2.*" // override class inclusion rules
                excludes += listOf("com.example2.subpackage.*") // override class exclusion rules
            }
        }
    }

    htmlReport {
        onCheck.set(false) // true to run koverHtmlReport task during the execution of the check task
        reportDir.set(layout.buildDirectory.dir("my-project-report/html-result")) // change report directory
        overrideFilters { 
            classes { // override common class filter
                includes += "com.example2.*" // class inclusion rules
                excludes += listOf("com.example2.subpackage.*") // override class exclusion rules
            }
        }
    }

    verify {
        onCheck.set(true) // true to run koverVerify task during the execution of the check task 
        rule { // add verification rule
            isEnabled = true // false to disable rule checking
            name = null // custom name for the rule
            target = kotlinx.kover.api.VerificationTarget.ALL // specify by which entity the code for separate coverage evaluation will be grouped
            
            overrideClassFilter { // override common class filter
                includes += "com.example.verify.*" // override class inclusion rules
                excludes += listOf("com.example.verify.subpackage.*") // override class exclusion rules
            }

            bound { // add rule bound
                minValue = 10
                maxValue = 20
                counter = kotlinx.kover.api.CounterType.LINE // change coverage metric to evaluate (LINE, INSTRUCTION, BRANCH)
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE // change counter value (COVERED_COUNT, MISSED_COUNT, COVERED_PERCENTAGE, MISSED_PERCENTAGE)
            }
        }
    }
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
kover {
    isDisabled.set(false) // true to disable instrumentation and all Kover tasks in this project
    engine = kotlinx.kover.api.DefaultIntellijEngine.INSTANCE // change Coverage Engine
    filters { // common filters for all default Kover tasks
        classes { // common class filter for all default Kover tasks 
          includes.add("com.example.*") // class inclusion rules
          excludes.addAll("com.example.subpackage.*") // class exclusion rules
        }
    }

    instrumentation {
        excludeTasks.add("dummy-tests") // set of test tasks names to exclude from instrumentation. The results of their execution will not be presented in the report
    }

    xmlReport {
        onCheck.set(false) // true to run koverXmlReport task during the execution of the check task
        reportFile.set(layout.buildDirectory.file("my-project-report/result.xml")) // change report file name
        overrideFilters {
            classes { // override common class filter
                includes.add("com.example2.*") // override class inclusion rules
                excludes.addAll("com.example2.subpackage.*") // override class exclusion rules
            }
        }
    }

    htmlReport {
        onCheck.set(false) // true to run koverHtmlReport task during the execution of the check task
        reportDir.set(layout.buildDirectory.dir("my-project-report/html-result")) // change report directory
        overrideFilters {
            classes { // override common class filter
              includes.add("com.example2.*") // class inclusion rules
              excludes.addAll("com.example2.subpackage.*") // override class exclusion rules
            }
        }
    }

    verify {
      onCheck.set(true) // true to run koverVerify task during the execution of the check task 
      rule { // add verification rule
          enabled = true // false to disable rule checking
          name = null // custom name for the rule
          target = 'ALL' // specify by which entity the code for separate coverage evaluation will be grouped
  
          overrideClassFilter { // override common class filter
              includes.add("com.example.verify.*") // override class inclusion rules
              excludes.addAll("com.example.verify.subpackage.*") // override class exclusion rules
          }
  
          bound { // add rule bound
              minValue = 10
              maxValue = 20
              counter = 'LINE' // change coverage metric to evaluate (LINE, INSTRUCTION, BRANCH)
              valueType = 'COVERED_PERCENTAGE' // change counter value (COVERED_COUNT, MISSED_COUNT, COVERED_PERCENTAGE, MISSED_PERCENTAGE)
          }
      }
    }
}
```
</details>

How to specify the engine version, see the [section](#specifying-coverage-engine)

### Configuring merged reports
To create default merged reports, you need to write `koverMerged.enable()` or 
```
koverMerged {
    enable()
}
```
in the containing project. By default, the merged reports will include this project along with all its subprojects.


Merged reports can be configured. To do this, you need to configure the extension in the containing project (where `koverMerged.enable()` is called)

<details open>
<summary>Kotlin</summary>

```kotlin
koverMerged {
    enable()  // create Kover merged reports
  
    filters { // common filters for all default Kover merged tasks
        classes { // common class filter for all default Kover merged tasks 
          includes += "com.example.*" // class inclusion rules
          excludes += listOf("com.example.subpackage.*") // class exclusion rules
        }

        projects { // common projects filter for all default Kover merged tasks
            includes += listOf("project1", ":child:project") // Specifies the projects involved in the merged tasks
        }
    }


    xmlReport {
        onCheck.set(false) // true to run koverMergedXmlReport task during the execution of the check task
        reportFile.set(layout.buildDirectory.file("my-merged-report/result.xml")) // change report file name
        overrideClassFilter { // override common class filter
            includes += "com.example2.*" // override class inclusion rules
            excludes += listOf("com.example2.subpackage.*") // override class exclusion rules 
        }
    }

    htmlReport {
        onCheck.set(false) // true to run koverMergedHtmlReport task during the execution of the check task
        reportDir.set(layout.buildDirectory.dir("my-merged-report/html-result")) // change report directory
        overrideClassFilter { // override common class filter
            includes += "com.example2.*" // override class inclusion rules
            excludes += listOf("com.example2.subpackage.*") // override class exclusion rules 
        }
    }

    verify {
        onCheck.set(true) // true to run koverMergedVerify task during the execution of the check task 
        rule { // add verification rule
            isEnabled = true // false to disable rule checking
            name = null // custom name for the rule
            target = kotlinx.kover.api.VerificationTarget.ALL // specify by which entity the code for separate coverage evaluation will be grouped
      
            overrideClassFilter { // override common class filter
                includes += "com.example.verify.*" // override class inclusion rules
                excludes += listOf("com.example.verify.subpackage.*") // override class exclusion rules
            }
      
            bound { // add rule bound
                minValue = 10
                maxValue = 20
                counter = kotlinx.kover.api.CounterType.LINE // change coverage metric to evaluate (LINE, INSTRUCTION, BRANCH)
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE // change counter value (COVERED_COUNT, MISSED_COUNT, COVERED_PERCENTAGE, MISSED_PERCENTAGE)
            }
        }
    }
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
koverMerged {
    enable()  // create Kover merged reports
  
    filters { // common filters for all default Kover merged tasks
        classes { // common class filter for all default Kover merged tasks 
            includes.add("com.example.*") // class inclusion rules
            excludes.addAll("com.example.subpackage.*") // class exclusion rules
        }
    
        projects { // common projects filter for all default Kover merged tasks
            includes.addAll("project1", ":child:project") // Specifies the projects involved in the merged tasks
        }
    }
  
  
    xmlReport {
        onCheck.set(false) // true to run koverMergedXmlReport task during the execution of the check task
        reportFile.set(layout.buildDirectory.file("my-merged-report/result.xml")) // change report file name
        overrideClassFilter { // override common class filter
            includes.add("com.example2.*") // override class inclusion rules
            excludes.addAll("com.example2.subpackage.*") // override class exclusion rules 
        }
    }
  
    htmlReport {
        onCheck.set(false) // true to run koverMergedHtmlReport task during the execution of the check task
        reportDir.set(layout.buildDirectory.dir("my-merged-report/html-result")) // change report directory
        overrideClassFilter { // override common class filter
            includes.add("com.example2.*") // override class inclusion rules
            excludes.addAll("com.example2.subpackage.*") // override class exclusion rules 
        }
    }
  
    verify {
        onCheck.set(true) // true to run koverMergedVerify task during the execution of the check task 
        rule { // add verification rule
            isEnabled = true // false to disable rule checking
            name = null // custom name for the rule
            target = 'ALL' // specify by which entity the code for separate coverage evaluation will be grouped
      
            overrideClassFilter { // override common class filter
                includes.add("com.example.verify.*") // override class inclusion rules
                excludes.addAll("com.example.verify.subpackage.*") // override class exclusion rules
            }
      
            bound { // add rule bound
                minValue = 10
                maxValue = 20
                counter = 'LINE' // change coverage metric to evaluate (LINE, INSTRUCTION, BRANCH)
                valueType = 'COVERED_PERCENTAGE' // change counter value (COVERED_COUNT, MISSED_COUNT, COVERED_PERCENTAGE, MISSED_PERCENTAGE)
            }
        }
    }
}
```
</details>


### Specifying Coverage Engine
#### IntelliJ Coverage Engine with default version
<details open>
<summary>Kotlin</summary>

```kotlin
kotlinx.kover.api.DefaultIntellijEngine
```
</details>

<details>
<summary>Groovy</summary>

```groovy
kotlinx.kover.api.DefaultIntellijEngine.INSTANCE
```
</details>

#### IntelliJ Coverage Engine with custom version
```
kotlinx.kover.api.IntellijEngine("1.0.668")
```

#### JaCoCo Coverage Engine with default version
<details open>
<summary>Kotlin</summary>

```kotlin
kotlinx.kover.api.DefaultJacocoEngine
```
</details>

<details>
<summary>Groovy</summary>

```groovy
kotlinx.kover.api.DefaultJacocoEngine.INSTANCE
```
</details>

#### JaCoCo Coverage Engine with custom version
```
kotlinx.kover.api.JacocoEngine("0.8.8")
```

## Kover default tasks
Tasks that are created for project where the Kover plugin is applied:
- `koverHtmlReport` - Generates code coverage HTML report for all enabled test tasks in one project.
- `koverXmlReport` - Generates code coverage XML report for all enabled test tasks in one project.
- `koverReport` - Executes both `koverXmlReport` and `koverHtmlReport` tasks.
- `koverVerify` - Verifies code coverage metrics of one project based on specified rules. Always executes before `check` task.

Tasks that are created for project where the Kover plugin is applied and merged reports are enabled:
- `koverMergedHtmlReport` - Generates code coverage HTML report for all enabled test tasks in all projects.
- `koverMergedXmlReport` - Generates code coverage XML report for all enabled test tasks in all projects.
- `koverMergedReport` - Executes both `koverMergedXmlReport` and `koverMergedHtmlReport` tasks.
- `koverMergedVerify` - Verifies code coverage metrics of all projects based on specified rules. Always executes before `check` task.


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
