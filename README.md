# kotlinx-kover

[![Kotlin Alpha](https://kotl.in/badges/alpha.svg)](https://kotlinlang.org/docs/components-stability.html)
[![JetBrains incubator project](https://jb.gg/badges/incubator.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

**Kover** - Gradle plugin for Kotlin code coverage tools: [Kover](https://github.com/JetBrains/intellij-coverage)
and [JaCoCo](https://github.com/jacoco/jacoco).

Minimum supported version of `Gradle` is `6.6`.

## Table of contents

- [Features](#features)
- [Quickstart](#quickstart)
  - [Single-project tasks](#single-project-tasks)
  - [Cross-project coverage](#cross-project-coverage)
- [Configuration](#configuration)
  - [Configuring project](#configuring-project)
  - [Configuring merged reports](#configuring-merged-reports)
  - [Configuring JVM test task](#configuring-jvm-test-tasks)
  - [Specifying Coverage Tool](#specifying-coverage-tool)
- [Example of configuring Android application](#example-of-configuring-android-application) 
- [Implicit plugin dependencies](#implicit-plugin-dependencies)
- [Building locally and contributing](#building-locally-and-contributing)

## Features

* Collection of code coverage through `JVM` test tasks.
* `HTML` and `XML` reports.
* Support for `Kotlin/JVM`, `Kotlin Multiplatform` and mixed `Kotlin-Java` sources with zero additional configuration.
* Support for `Kotlin Android` without the need to divide it into build types and flavours.
* Verification rules with bounds to keep track of coverage.
* Customizable filters for instrumented classes.

## Quickstart

The recommended way of applying Kover is with the 
[plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block).

Add the following to your top-level build file:

<details open>
<summary>Kotlin</summary>

```kotlin
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.6.1"
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
plugins {
    id 'org.jetbrains.kotlinx.kover' version '0.6.1'
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
        classpath("org.jetbrains.kotlinx:kover:0.6.1")
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
        classpath 'org.jetbrains.kotlinx:kover:0.6.1'
    }
}
  
apply plugin: 'kover'    
```

</details>

### Single-project tasks

Tasks that are created for projects where Kover plugin is applied:

- `koverHtmlReport` - Generates code coverage HTML report for all enabled test tasks in the project.
- `koverXmlReport` - Generates code coverage XML report for all enabled test tasks in the project.
- `koverReport` - Executes both `koverXmlReport` and `koverHtmlReport` tasks.
- `koverVerify` - Verifies code coverage metrics of the project based on configured rules. Always is executed before `check` task.

### Cross-project coverage

Kover's [single project tasks](#single-project-tasks) are designed to collect project coverage by executing 
tests located within the same project.

If you want to collect coverage of code the tests for which are in another project, or coverage of all code in a 
[multi-project build](https://docs.gradle.org/current/userguide/multi_project_builds.html#sec:creating_multi_project_builds),
you need to enable merged reports and then use [merged report tasks](#merged-report-tasks):

```
koverMerged {
    enable()
}
```

#### Merged report tasks

Merged report tasks are created for projects in which Kover plugin is applied and for which merged reports are enabled.

These tasks merge statistics of code coverage collected from running test tasks of several projects. By default,
they include containing project along with all its subprojects.

- `koverMergedHtmlReport` - Generates code coverage HTML report for all enabled test tasks in all projects.
- `koverMergedXmlReport` - Generates code coverage XML report for all enabled test tasks in all projects.
- `koverMergedReport` - Executes both `koverMergedXmlReport` and `koverMergedHtmlReport` tasks.
- `koverMergedVerify` - Verifies code coverage metrics of all projects based on specified rules. 
  Always executes before `check` task.

You can learn how to additionally configure merged reports in [configuring merged reports](#configuring-merged-reports)
section.

## Configuration

Once you've applied Kover, you can run it without additional configuration. 

For cases when configuration is needed, Kover provides special extensions and tasks.

### Configuring JVM test tasks

In some cases you may want to disable instrumentation of certain classes - either voluntarily or if it causes execution 
errors like `No instrumentation registered! Must run under a registering instrumentation.`

To achieve that, you need to configure a _Kover extension_ for it.

For example, to configure a standard test task for Kotlin/JVM named `test`, you need to add the following code to 
the build script of the project where this task is declared:

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.test {
    extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
        // set to true to disable instrumentation of this task, 
        // Kover reports will not depend on the results of its execution 
        isDisabled.set(false)
      
        // set file name of binary report
        binaryReportFile.set(file("$buildDir/custom/result.bin"))

        // for details, see "Instrumentation inclusion rules" below
        includes = listOf("com.example.*")

        // for details, see "Instrumentation exclusion rules" below
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
        // set to true to disable instrumentation of this task, 
        // Kover reports will not depend on the results of its execution   
        disabled = false
      
        // set file name of binary report
        binaryReportFile.set(file("$buildDir/custom/result.bin"))
      
        // for details, see "Instrumentation inclusion rules" below
        includes = ['com.example.*']
      
        // for details, see "Instrumentation exclusion rules" below
        excludes = ['com.example.subpackage.*']
    }
}
```

</details>

**For other platforms, like Android or Kotlin-Multiplatform, the names may differ and you may also have several 
test tasks instead of one, so you first need to determine the name of the required task.**

An example of configuring a test task for build type `debug` in Android:

<details open>
<summary>Kotlin</summary>

```kotlin
android {
    // other Android declarations

    testOptions {
        unitTests.all {
            if (it.name == "testDebugUnitTest") {
                it.extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
                    // set to true to disable instrumentation of this task, 
                    // Kover reports will not depend on the results of its execution 
                    isDisabled.set(false)

                    // set file name of binary report 
                    binaryReportFile.set(file("$buildDir/custom/debug-report.bin"))

                    // for details, see "Instrumentation inclusion rules" below
                    includes = listOf("com.example.*") 
                  
                    // for details, see "Instrumentation exclusion rules" below
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
                    // set to true to disable instrumentation of this task, 
                    // Kover reports will not depend on the results of its execution 
                    disabled = false

                    // set file name of binary report 
                    binaryReportFile.set(file("$buildDir/custom/debug-report.bin"))
                    
                    // for details, see "Instrumentation inclusion rules" below
                    includes = ['com.example.*']

                    // for details, see "Instrumentation exclusion rules" below
                    excludes = ['com.example.subpackage.*'] 
                }
            }
        }
    }
}
```
</details>


**Instrumentation inclusion rules**

Only the specified classes will be instrumented. Remaining (non-included) classes will still be present in the report,
but their coverage will be zero.

**Instrumentation exclusion rules**

Specified classes will not be instrumented and their coverage will be zero.

Inclusion/exclusion value rules:

* Can be a fully-qualified class name.
* Can contain wildcards:
    * `*` for zero or several of any char.
    * `**` is the same as `*`.
    * `?` for one of any char.
* File and directory names are not allowed.

Examples: 

* (good) `my.package.ClassName`
* (good) `my.*.*Name`
* (bad) `my/package/ClassName.kt`
* (bad) `src/my.**.ClassName`

Exclusion rules have priority over inclusion.

Exclusion and inclusion rules from [test tasks](#configuring-jvm-test-tasks) (if at least one of them is not empty)
take precedence over [common class filter](#configuring-project) rules.

### Configuring project

You can configure Kover, its tasks and instrumentation in any project for which Kover is applied.

<details open>
<summary>Kotlin</summary>

```kotlin
kover {
    isDisabled.set(false) // true to disable instrumentation and all Kover tasks in this project
    tool.set(KoverToolDefault) // to change the tool, use kotlinx.kover.api.KoverTool("xxx") or kotlinx.kover.api.JacocoTool("xxx")
    filters { // common filters for all default Kover tasks
        classes { // common class filter for all default Kover tasks in this project
            includes += "com.example.*" // class inclusion rules
            excludes += listOf("com.example.subpackage.*") // class exclusion rules
        }
        annotations { // common annotation filter for all default Kover tasks in this project
            excludes += listOf("com.example.Annotation", "*Generated") // exclude declarations marked by specified annotations
        }
    }

    instrumentation {
        excludeTasks += "dummy-tests" // set of test tasks names to exclude from instrumentation. The results of their execution will not be presented in the report
    }

    xmlReport {
        onCheck.set(false) // set to true to run koverXmlReport task during the execution of the check task (if it exists) of the current project
        reportFile.set(layout.buildDirectory.file("my-project-report/result.xml")) // change report file name
        overrideFilters { 
            classes { // override common class filter
                includes += "com.example2.*" // override class inclusion rules
                excludes += listOf("com.example2.subpackage.*") // override class exclusion rules
            }
            annotations { // override common annotation filter for XML report (filtering will take place only by the annotations specified here)
                excludes += listOf("com.example2.Annotation")
            }
        }
    }

    htmlReport {
        onCheck.set(false) // set to true to run koverHtmlReport task during the execution of the check task (if it exists) of the current project
        reportDir.set(layout.buildDirectory.dir("my-project-report/html-result")) // change report directory
        overrideFilters { 
            classes { // override common class filter
                includes += "com.example2.*" // class inclusion rules
                excludes += listOf("com.example2.subpackage.*") // override class exclusion rules
            }
            annotations { // override common annotation filter for HTML report (filtering will take place only by the annotations specified here)
                excludes += listOf("com.example2.Annotation")
            }
        }
    }

    verify {
        onCheck.set(true) // set to true to run koverVerify task during the execution of the check task (if it exists) of the current project 
        rule { // add verification rule
            isEnabled = true // set to false to disable rule checking
            name = null // custom name for the rule
            target = kotlinx.kover.api.VerificationTarget.ALL // specify by which entity the code for separate coverage evaluation will be grouped
            
            overrideClassFilter { // override common class filter
                includes += "com.example.verify.*" // override class inclusion rules
                excludes += listOf("com.example.verify.subpackage.*") // override class exclusion rules
            }
            overrideAnnotationFilter { // override common annotation filter (filtering will take place only by the annotations specified here)
                excludes += "*verify.*Generated" // declarations marked only by these annotations will be excluded from this rule
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
    tool = kotlinx.kover.api.KoverToolDefault.INSTANCE // // to change tool, use kotlinx.kover.api.KoverTool("xxx") or kotlinx.kover.api.JacocoTool("xxx")
    filters { // common filters for all default Kover tasks
        classes { // common class filter for all default Kover tasks in this project
          includes.add("com.example.*") // class inclusion rules
          excludes.addAll("com.example.subpackage.*") // class exclusion rules
        }
        annotations { // common annotation filter for all default Kover tasks in this project
            excludes.addAll("com.example.Annotation", "*Generated") // exclude declarations marked by specified annotations
        }
    }

    instrumentation {
        excludeTasks.add("dummy-tests") // set of test tasks names to exclude from instrumentation. The results of their execution will not be presented in the report
    }

    xmlReport {
        onCheck.set(false) // set to true to run koverXmlReport task during the execution of the check task (if it exists) of the current project
        reportFile.set(layout.buildDirectory.file("my-project-report/result.xml")) // change report file name
        overrideFilters {
            classes { // override common class filter
                includes.add("com.example2.*") // override class inclusion rules
                excludes.addAll("com.example2.subpackage.*") // override class exclusion rules
            }
            annotations { // override common annotation filter for XML report (filtering will take place only by the annotations specified here)
                excludes.addAll("com.example2.Annotation") 
            }
        }
    }

    htmlReport {
        onCheck.set(false) // set to true to run koverHtmlReport task during the execution of the check task (if it exists) of the current project
        reportDir.set(layout.buildDirectory.dir("my-project-report/html-result")) // change report directory
        overrideFilters {
            classes { // override common class filter
              includes.add("com.example2.*") // class inclusion rules
              excludes.addAll("com.example2.subpackage.*") // override class exclusion rules
            }
            annotations { // override common annotation filter for HTML report (filtering will take place only by the annotations specified here)
                excludes.addAll("com.example2.Annotation")
            }
        }
    }

    verify {
      onCheck.set(true) // set to true to run koverVerify task during the execution of the check task (if it exists) of the current project
      rule { // add verification rule
          enabled = true // set to false to disable rule checking
          name = null // custom name for the rule
          target = 'ALL' // specify by which entity the code for separate coverage evaluation will be grouped
  
          overrideClassFilter { // override common class filter
              includes.add("com.example.verify.*") // override class inclusion rules
              excludes.addAll("com.example.verify.subpackage.*") // override class exclusion rules
          }
          overrideAnnotationFilter { // override common annotation filter (filtering will take place only by the annotations specified here)
            excludes += "*verify.*Generated" // declarations marked only by these annotations will be excluded from this rule
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

Additionally, you can specify Tool version. See [specifying coverage tool](#specifying-coverage-tool) section for 
details. 

### Configuring merged reports

Merged reports can be configured in a similar manner to average reports. Each participating project can have its own
configuration of instrumentation and special, non-class, filters.

All Gradle projects that participate in merged reports must have the same version of Kover and
[Coverage tool](#specifying-coverage-tool).

<details open>
<summary>Kotlin</summary>

```kotlin
koverMerged {
    enable()  // create Kover merged report tasks from this project and subprojects with enabled Kover plugin
  
    filters { // common filters for all default Kover merged tasks
        classes { // common class filter for all default Kover merged tasks 
          includes += "com.example.*" // class inclusion rules
          excludes += listOf("com.example.subpackage.*") // class exclusion rules
        }
        annotations { // common annotation filter for all default Kover merged tasks
            excludes += listOf("com.example.Annotation", "*Generated") // exclude declarations marked by specified annotations
        }
        projects { // common projects filter for all default Kover merged tasks
            excludes += listOf("project1", ":child:project") // Specifies the projects excluded from the merged tasks
        }
    }


    xmlReport {
        onCheck.set(false) // set to true to run koverMergedXmlReport task during the execution of the check task (if it exists) of the current project
        reportFile.set(layout.buildDirectory.file("my-merged-report/result.xml")) // change report file name
        overrideClassFilter { // override common class filter
            includes += "com.example2.*" // override class inclusion rules
            excludes += listOf("com.example2.subpackage.*") // override class exclusion rules 
        }
        overrideAnnotationFilter { // override common annotation filter (filtering will take place only by the annotations specified here)
            excludes += "*OverrideGenerated" // declarations marked only by specified annotations will be excluded from merged XML report
        }
    }

    htmlReport {
        onCheck.set(false) // set to true to run koverMergedHtmlReport task during the execution of the check task (if it exists) of the current project
        reportDir.set(layout.buildDirectory.dir("my-merged-report/html-result")) // change report directory
        overrideClassFilter { // override common class filter
            includes += "com.example2.*" // override class inclusion rules
            excludes += listOf("com.example2.subpackage.*") // override class exclusion rules 
        }
        overrideAnnotationFilter { // override common annotation filter (filtering will take place only by the annotations specified here)
            excludes += "*OverrideGenerated" // declarations marked only by specified annotations will be excluded from merged HTML report
        }
    }

    verify {
        onCheck.set(true) // set to true to run koverMergedVerify task during the execution of the check task (if it exists) of the current project 
        rule { // add verification rule
            isEnabled = true // set to false to disable rule checking
            name = null // custom name for the rule
            target = kotlinx.kover.api.VerificationTarget.ALL // specify by which entity the code for separate coverage evaluation will be grouped
      
            overrideClassFilter { // override common class filter
                includes += "com.example.verify.*" // override class inclusion rules
                excludes += listOf("com.example.verify.subpackage.*") // override class exclusion rules
            }
            overrideAnnotationFilter { // override common annotation filter (filtering will take place only by the annotations specified here)
                excludes += "*verify.*Generated" // declarations marked only by these annotations will be excluded from this rule
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
    enable()  // create Kover merged report tasks from this project and subprojects with enabled Kover plugin
  
    filters { // common filters for all default Kover merged tasks
        classes { // common class filter for all default Kover merged tasks 
            includes.add("com.example.*") // class inclusion rules
            excludes.addAll("com.example.subpackage.*") // class exclusion rules
        }

        annotations { // common annotation filter for all default Kover merged tasks
            excludes.addAll("com.example.Annotation", "*Generated") // exclude declarations marked by specified annotations
        }
    
        projects { // common projects filter for all default Kover merged tasks
            excludes.addAll("project1", ":child:project") // Specifies the projects excluded in the merged tasks
        }
    }
  
  
    xmlReport {
        onCheck.set(false) // set to true to run koverMergedXmlReport task during the execution of the check task (if it exists) of the current project
        reportFile.set(layout.buildDirectory.file("my-merged-report/result.xml")) // change report file name
        overrideClassFilter { // override common class filter
            includes.add("com.example2.*") // override class inclusion rules
            excludes.addAll("com.example2.subpackage.*") // override class exclusion rules 
        }

        overrideAnnotationFilter { // override common annotation filter (filtering will take place only by the annotations specified here)
            excludes.addAll("*OverrideGenerated") // declarations marked only by specified annotations will be excluded from merged XML report
        }
    }
  
    htmlReport {
        onCheck.set(false) // set to true to run koverMergedHtmlReport task during the execution of the check task (if it exists) of the current project
        reportDir.set(layout.buildDirectory.dir("my-merged-report/html-result")) // change report directory
        overrideClassFilter { // override common class filter
            includes.add("com.example2.*") // override class inclusion rules
            excludes.addAll("com.example2.subpackage.*") // override class exclusion rules 
        }
        overrideAnnotationFilter { // override common annotation filter (filtering will take place only by the annotations specified here)
            excludes.addAll("*OverrideGenerated") // declarations marked only by specified annotations will be excluded from merged HTML report
        }
    }
  
    verify {
        onCheck.set(true) // set to true to run koverMergedVerify task during the execution of the check task (if it exists) of the current project
        rule { // add verification rule
            isEnabled = true // set to false to disable rule checking
            name = null // custom name for the rule
            target = 'ALL' // specify by which entity the code for separate coverage evaluation will be grouped
      
            overrideClassFilter { // override common class filter
                includes.add("com.example.verify.*") // override class inclusion rules
                excludes.addAll("com.example.verify.subpackage.*") // override class exclusion rules
            }
            overrideAnnotationFilter { // override common annotation filter (filtering will take place only by the annotations specified here)
                excludes.addAll("*verify.*Generated") // declarations marked only by these annotations will be excluded from this rule
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


### Specifying Coverage Tool

You can choose which Coverage Tool and which version to use when configuring Kover.

#### Kover Coverage Tool with default version

<details open>
<summary>Kotlin</summary>

```kotlin
kotlinx.kover.api.KoverToolDefault
```

</details>

<details>
<summary>Groovy</summary>

```groovy
kotlinx.kover.api.KoverToolDefault.INSTANCE
```

</details>

#### Kover Coverage Tool with custom version

```
kotlinx.kover.api.KoverTool("1.0.683")
```

#### JaCoCo Coverage Tool with default version

<details open>
<summary>Kotlin</summary>

```kotlin
kotlinx.kover.api.JacocoToolDefault
```

</details>

<details>
<summary>Groovy</summary>

```groovy
kotlinx.kover.api.JacocoToolDefault.INSTANCE
```

</details>

#### JaCoCo Coverage Tool with custom version
```
kotlinx.kover.api.JacocoTool("0.8.8")
```

### Example of configuring Android application

Example of configuring test task for build type `debug` in Android:

<details open>
<summary>Kotlin</summary>

`build.gradle.kts` (Project)

```kotlin
buildscript {
    // ...
    dependencies {
        // ...
        classpath("org.jetbrains.kotlinx:kover:0.6.1")
    }
}

plugins {
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
}

koverMerged {
    enable()

    filters {
        classes {
            excludes += "*.databinding.*" // exclude classes by mask
        }
    }
}
```

`build.gradle.kts` (Module)

```kotlin
plugins {
    // ...
    id("org.jetbrains.kotlinx.kover")
}

android {
    // ...
}

dependencies {
    // ...
}

kover {
    instrumentation {
        excludeTasks += "testReleaseUnitTest" // exclude testReleaseUnitTest from instrumentation
    }
}
```

An example is available [here](examples/android_kts)

</details>

<details>
<summary>Groovy</summary>

`build.gradle` (Project)

```groovy
plugin {
    // ...
    id 'org.jetbrains.kotlinx.kover' version "0.6.1"
}

koverMerged {
    enable()

    filters {
        classes {
            excludes.add "*.databinding.*" // exclude classes by mask
        }
    }
}
```

`build.gradle` (Module)

```groovy
plugins {
    // ...
    id 'org.jetbrains.kotlinx.kover'
}

android {
    // ...
}

dependencies {
    // ...
}

kover {
    instrumentation {
        excludeTasks.add "testReleaseUnitTest" // exclude testReleaseUnitTest from instrumentation
    }
}
```

An example is available [here](examples/android_groovy)

</details>

## Implicit plugin dependencies

While the plugin is being applied, the artifacts of the JaCoCo or Kover toolkit are dynamically loaded. They are 
downloaded from the `mavenCentral` repository.

For Kover to work correctly, you need to make sure that `mavenCentral` (or any of its mirrors) is present in 
the repository list of the project in which the plugin is applied. Usually you can find it in the root project:

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

## Building locally and Contributing

See [Contributing Guidelines](CONTRIBUTING.md).
