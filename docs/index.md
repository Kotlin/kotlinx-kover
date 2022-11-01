## Table of contents

- [Features](#features)
- [Quickstart](#quickstart)
  - [Single-project tasks](#single-project-tasks)
  - [Cross-project coverage](#cross-project-coverage)
- [Configuration](#configuration)
  - [Configuring project](#configuring-project)
  - [Configuring merged reports](#configuring-merged-reports)
  - [Configuring JVM test task](#configuring-jvm-test-tasks)
  - [Specifying Coverage Engine](#specifying-coverage-engine)
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

*Kotlin*

```kotlin
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.6.1"
}
```

*Groovy*

```groovy
plugins {
    id 'org.jetbrains.kotlinx.kover' version '0.6.1'
}
```

#### Legacy Plugin Application

[Legacy method](https://docs.gradle.org/current/userguide/plugins.html#sec:old_plugin_application) of applying plugins
can be used if you cannot use the plugins DSL for some reason.

*Kotlin*

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

*Groovy*

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

*Kotlin*

```kotlin
tasks.test {
    extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
        // set to true to disable instrumentation of this task, 
        // Kover reports will not depend on the results of its execution 
        isDisabled.set(false)
      
        // set file name of binary report
        binaryReportFile.set(file("$buildDir/custom/result.bin"))

        // for details, see "Instrumentation inclusion rules" below
        includes += listOf("com.example.*")

        // for details, see "Instrumentation exclusion rules" below
        excludes += listOf("com.example.subpackage.*")
    }
}
```

*Groovy*

```groovy
tasks.test {
    kover {
        // set to true to disable instrumentation of this task, 
        // Kover reports will not depend on the results of its execution   
        disabled = false
      
        // set file name of binary report
        binaryReportFile.set(file("$buildDir/custom/result.bin"))
      
        // for details, see "Instrumentation inclusion rules" below
        includes.addAll("com.example.*")
      
        // for details, see "Instrumentation exclusion rules" below
        excludes.addAll("com.example.subpackage.*")
    }
}
```


**For other platforms, like Android or Kotlin-Multiplatform, the names may differ and you may also have several
test tasks instead of one, so you first need to determine the name of the required task.**

An example of configuring a test task for build type `debug` in Android:

*Kotlin*

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
                    includes += listOf("com.example.*") 
                  
                    // for details, see "Instrumentation exclusion rules" below
                    excludes += listOf("com.example.subpackage.*")
                }
            }
        }
    }
}
```

*Groovy*

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
                    includes.addAll("com.example.*")

                    // for details, see "Instrumentation exclusion rules" below
                    excludes.addAll("com.example.subpackage.*") 
                }
            }
        }
    }
}
```


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

*Kotlin*

```kotlin
kover {
    // true to disable instrumentation and all Kover tasks in this project
    isDisabled.set(false)

    // to change engine, use kotlinx.kover.api.IntellijEngine("xxx") or kotlinx.kover.api.JacocoEngine("xxx")
    engine.set(DefaultIntellijEngine)

    // common filters for all default Kover tasks
    filters {
        // common class filter for all default Kover tasks in this project
        classes {
            // class inclusion rules
            includes += "com.example.*"
            // class exclusion rules
            excludes += listOf("com.example.subpackage.*")
        }

        // common annotation filter for all default Kover tasks in this project
        annotations {
            // exclude declarations marked by specified annotations
            excludes += listOf("com.example.Annotation", "*Generated")
        }
    }

    instrumentation {
        // set of test tasks names to exclude from instrumentation. The results of their execution will not be presented in the report
        excludeTasks += "dummy-tests"
    }

    xmlReport {
        // set to true to run koverXmlReport task during the execution of the check task (if it exists) of the current project
        onCheck.set(false)
      
        // change report file name
        reportFile.set(layout.buildDirectory.file("my-project-report/result.xml"))
        overrideFilters {
            // override common class filter
            classes {
                // override class inclusion rules
                includes += "com.example2.*"
                
                // override class exclusion rules
                excludes += listOf("com.example2.subpackage.*") 
            }

            // override common annotation filter for XML report (filtering will take place only by the annotations specified here)
            annotations {
                excludes += listOf("com.example2.Annotation")
            }
        }
    }

    htmlReport {
        // set to true to run koverHtmlReport task during the execution of the check task (if it exists) of the current project
        onCheck.set(false)

        // change report directory
        reportDir.set(layout.buildDirectory.dir("my-project-report/html-result"))
        overrideFilters {
            // override common class filter  
            classes {
                // class inclusion rules
                includes += "com.example2.*"
                // override class exclusion rules
                excludes += listOf("com.example2.subpackage.*")
            }
            // override common annotation filter for HTML report (filtering will take place only by the annotations specified here)
            annotations {
                excludes += listOf("com.example2.Annotation")
            }
        }
    }

    verify {
        // set to true to run koverVerify task during the execution of the check task (if it exists) of the current project
        onCheck.set(true)

        // add verification rule
        rule {
            // set to false to disable rule checking
            isEnabled = true

            // custom name for the rule
            name = null

            // specify by which entity the code for separate coverage evaluation will be grouped
            target = kotlinx.kover.api.VerificationTarget.ALL

            // override common class filter
            overrideClassFilter {
                // override class inclusion rules
                includes += "com.example.verify.*"

                // override class exclusion rules
                excludes += listOf("com.example.verify.subpackage.*")
            }
          
            // override common annotation filter (filtering will take place only by the annotations specified here)
            overrideAnnotationFilter {
                // declarations marked only by these annotations will be excluded from this rule
                excludes += "*verify.*Generated"
            }

            // add rule bound
            bound {
                minValue = 10
                maxValue = 20

                // change coverage metric to evaluate (LINE, INSTRUCTION, BRANCH)
                counter = kotlinx.kover.api.CounterType.LINE

                // change counter value (COVERED_COUNT, MISSED_COUNT, COVERED_PERCENTAGE, MISSED_PERCENTAGE)
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE
            }
        }
    }
}
```

*Groovy*

```groovy
kover {
    // true to disable instrumentation and all Kover tasks in this project
    isDisabled.set(false)

    // to change engine, use kotlinx.kover.api.IntellijEngine("xxx") or kotlinx.kover.api.JacocoEngine("xxx")
    engine = kotlinx.kover.api.DefaultIntellijEngine.INSTANCE

    // common filters for all default Kover tasks
    filters {
      
        // common class filter for all default Kover tasks in this project
        classes {
          
          // class inclusion rules
          includes.add("com.example.*")

          // class exclusion rules
          excludes.addAll("com.example.subpackage.*")
        }
        // common annotation filter for all default Kover tasks in this project
        annotations {
          
            // exclude declarations marked by specified annotations
            excludes.addAll("com.example.Annotation", "*Generated")
        }
    }

    instrumentation {
        // set of test tasks names to exclude from instrumentation. The results of their execution will not be presented in the report
        excludeTasks.add("dummy-tests")
    }

    xmlReport {
        // set to true to run koverXmlReport task during the execution of the check task (if it exists) of the current project
        onCheck.set(false)

        // change report file name
        reportFile.set(layout.buildDirectory.file("my-project-report/result.xml"))
        overrideFilters {
            // override common class filter
            classes {

                // override class inclusion rules
                includes.add("com.example2.*")

                // override class exclusion rules
                excludes.addAll("com.example2.subpackage.*")
            }

            // override common annotation filter for XML report (filtering will take place only by the annotations specified here)
            annotations {
                excludes.addAll("com.example2.Annotation") 
            }
        }
    }

    htmlReport {
        // set to true to run koverHtmlReport task during the execution of the check task (if it exists) of the current project
        onCheck.set(false)

        // change report directory
        reportDir.set(layout.buildDirectory.dir("my-project-report/html-result"))
        overrideFilters {
            // override common class filter
            classes {

              // class inclusion rules
              includes.add("com.example2.*")

              // override class exclusion rules
              excludes.addAll("com.example2.subpackage.*")
            }

            // override common annotation filter for HTML report (filtering will take place only by the annotations specified here)
            annotations {
                excludes.addAll("com.example2.Annotation")
            }
        }
    }

    verify {
      // set to true to run koverVerify task during the execution of the check task (if it exists) of the current project
      onCheck.set(true)

      // add verification rule
      rule {
        
          // set to false to disable rule checking
          enabled = true

          // custom name for the rule
          name = null

          // specify by which entity the code for separate coverage evaluation will be grouped
          target = 'ALL'

          // override common class filter
          overrideClassFilter {

              // override class inclusion rules
              includes.add("com.example.verify.*")

              // override class exclusion rules
              excludes.addAll("com.example.verify.subpackage.*")
          }
          // override common annotation filter (filtering will take place only by the annotations specified here)
          overrideAnnotationFilter {

            // declarations marked only by these annotations will be excluded from this rule
            excludes += "*verify.*Generated"
          }
        
          // add rule bound
          bound { 
              minValue = 10
              maxValue = 20

              // change coverage metric to evaluate (LINE, INSTRUCTION, BRANCH)
              counter = 'LINE'

              // change counter value (COVERED_COUNT, MISSED_COUNT, COVERED_PERCENTAGE, MISSED_PERCENTAGE)
              valueType = 'COVERED_PERCENTAGE' 
          }
      }
    }
}
```

Additionally, you can specify Engine version. See [specifying coverage engine](#specifying-coverage-engine) section for
details.

### Configuring merged reports

Merged reports can be configured in a similar manner to average reports. Each participating project can have its own
configuration of instrumentation and special, non-class, filters.

All Gradle projects that participate in merged reports must have the same version of Kover and
[Coverage engine](#specifying-coverage-engine).

*Kotlin*

```kotlin
koverMerged {
    // create Kover merged report tasks from this project and subprojects with enabled Kover plugin
    enable()
  
    // common filters for all default Kover merged tasks
    filters {

        // common class filter for all default Kover merged tasks 
        classes {

          // class inclusion rules
          includes += "com.example.*"

          // class exclusion rules
          excludes += listOf("com.example.subpackage.*") 
        }
      
        // common annotation filter for all default Kover merged tasks
        annotations {
          
            // exclude declarations marked by specified annotations
            excludes += listOf("com.example.Annotation", "*Generated") 
        }

        // common projects filter for all default Kover merged tasks
        projects {

            // Specifies the projects excluded from the merged tasks
            excludes += listOf("project1", ":child:project")
        }
    }


    xmlReport {
        // set to true to run koverMergedXmlReport task during the execution of the check task (if it exists) of the current project
        onCheck.set(false)

        // change report file name
        reportFile.set(layout.buildDirectory.file("my-merged-report/result.xml"))

        // override common class filter
        overrideClassFilter {

            // override class inclusion rules
            includes += "com.example2.*"

            // override class exclusion rules 
            excludes += listOf("com.example2.subpackage.*") 
        }

        // override common annotation filter (filtering will take place only by the annotations specified here)
        overrideAnnotationFilter {

            // declarations marked only by specified annotations will be excluded from merged XML report
            excludes += "*OverrideGenerated" 
        }
    }

    htmlReport {
        // set to true to run koverMergedHtmlReport task during the execution of the check task (if it exists) of the current project
        onCheck.set(false)

        // change report directory
        reportDir.set(layout.buildDirectory.dir("my-merged-report/html-result"))

        // override common class filter
        overrideClassFilter {

            // override class inclusion rules
            includes += "com.example2.*"

            // override class exclusion rules 
            excludes += listOf("com.example2.subpackage.*") 
        }
      
        // override common annotation filter (filtering will take place only by the annotations specified here)
        overrideAnnotationFilter {

            // declarations marked only by specified annotations will be excluded from merged HTML report
            excludes += "*OverrideGenerated" 
        }
    }

    verify {
        // set to true to run koverMergedVerify task during the execution of the check task (if it exists) of the current project 
        onCheck.set(true)

        // add verification rule
        rule {
          
            // set to false to disable rule checking
            isEnabled = true

            // custom name for the rule
            name = null

            // specify by which entity the code for separate coverage evaluation will be grouped
            target = kotlinx.kover.api.VerificationTarget.ALL
          
            // override common class filter
            overrideClassFilter {

                // override class inclusion rules
                includes += "com.example.verify.*"

                // override class exclusion rules
                excludes += listOf("com.example.verify.subpackage.*") 
            }

            // override common annotation filter (filtering will take place only by the annotations specified here)
            overrideAnnotationFilter {

                // declarations marked only by these annotations will be excluded from this rule
                excludes += "*verify.*Generated" 
            }

            // add rule bound
            bound { 
                minValue = 10
                maxValue = 20
              
                // change coverage metric to evaluate (LINE, INSTRUCTION, BRANCH)
                counter = kotlinx.kover.api.CounterType.LINE

                // change counter value (COVERED_COUNT, MISSED_COUNT, COVERED_PERCENTAGE, MISSED_PERCENTAGE)
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE 
            }
        }
    }
}
```

*Groovy*

```groovy
koverMerged {
    // create Kover merged report tasks from this project and subprojects with enabled Kover plugin
    enable()

    // common filters for all default Kover merged tasks
    filters {

        // common class filter for all default Kover merged tasks 
        classes {

            // class inclusion rules
            includes.add("com.example.*")

            // class exclusion rules
            excludes.addAll("com.example.subpackage.*") 
        }
      
        // common annotation filter for all default Kover merged tasks
        annotations { 
          
            // exclude declarations marked by specified annotations
            excludes.addAll("com.example.Annotation", "*Generated") 
        }

        // common projects filter for all default Kover merged tasks
        projects {

            // Specifies the projects excluded in the merged tasks
            excludes.addAll("project1", ":child:project") 
        }
    }
  
  
    xmlReport {
        // set to true to run koverMergedXmlReport task during the execution of the check task (if it exists) of the current project
        onCheck.set(false)

        // change report file name
        reportFile.set(layout.buildDirectory.file("my-merged-report/result.xml"))

        // override common class filter
        overrideClassFilter {

            // override class inclusion rules
            includes.add("com.example2.*")

            // override class exclusion rules 
            excludes.addAll("com.example2.subpackage.*") 
        }

        // override common annotation filter (filtering will take place only by the annotations specified here)
        overrideAnnotationFilter { 
          
            // declarations marked only by specified annotations will be excluded from merged XML report
            excludes.addAll("*OverrideGenerated") 
        }
    }
  
    htmlReport {
        // set to true to run koverMergedHtmlReport task during the execution of the check task (if it exists) of the current project
        onCheck.set(false)

        // change report directory
        reportDir.set(layout.buildDirectory.dir("my-merged-report/html-result"))
      
        // override common class filter
        overrideClassFilter {

            // override class inclusion rules
            includes.add("com.example2.*") 
          
            // override class exclusion rules 
            excludes.addAll("com.example2.subpackage.*") 
        }

        // override common annotation filter (filtering will take place only by the annotations specified here)
        overrideAnnotationFilter {

            // declarations marked only by specified annotations will be excluded from merged HTML report
            excludes.addAll("*OverrideGenerated") 
        }
    }
  
    verify {
        // set to true to run koverMergedVerify task during the execution of the check task (if it exists) of the current project
        onCheck.set(true)

        // add verification rule
        rule {

            // set to false to disable rule checking
            isEnabled = true

            // custom name for the rule
            name = null

            // specify by which entity the code for separate coverage evaluation will be grouped
            target = 'ALL'

            // override common class filter
            overrideClassFilter {

                // override class inclusion rules
                includes.add("com.example.verify.*")

                // override class exclusion rules
                excludes.addAll("com.example.verify.subpackage.*") 
            }

            // override common annotation filter (filtering will take place only by the annotations specified here)
            overrideAnnotationFilter {

                // declarations marked only by these annotations will be excluded from this rule
                excludes.addAll("*verify.*Generated") 
            }
          
            // add rule bound
            bound { 
                minValue = 10
                maxValue = 20

                // change coverage metric to evaluate (LINE, INSTRUCTION, BRANCH)
                counter = 'LINE'

                // change counter value (COVERED_COUNT, MISSED_COUNT, COVERED_PERCENTAGE, MISSED_PERCENTAGE)
                valueType = 'COVERED_PERCENTAGE' 
            }
        }
    }
}
```


### Specifying Coverage Engine

You can choose which Coverage Engine and which version to use when configuring Kover.

#### IntelliJ Coverage Engine with default version

*Kotlin*

```kotlin
kotlinx.kover.api.DefaultIntellijEngine
```

*Groovy*

```groovy
kotlinx.kover.api.DefaultIntellijEngine.INSTANCE
```

#### IntelliJ Coverage Engine with custom version

```
kotlinx.kover.api.IntellijEngine("1.0.683")
```

#### JaCoCo Coverage Engine with default version

*Kotlin*

```kotlin
kotlinx.kover.api.DefaultJacocoEngine
```

*Groovy*

```groovy
kotlinx.kover.api.DefaultJacocoEngine.INSTANCE
```

#### JaCoCo Coverage Engine with custom version
```
kotlinx.kover.api.JacocoEngine("0.8.8")
```

### Example of configuring Android application

Example of configuring test task for build type `debug` in Android:

*Kotlin*

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
            // exclude classes by mask
            excludes += "*.databinding.*" 
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
        // exclude testReleaseUnitTest from instrumentation
        excludeTasks += "testReleaseUnitTest" 
    }
}
```

An example is available [here](https://github.com/Kotlin/kotlinx-kover/tree/release/examples/android_kts)

*Groovy*

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
            // exclude classes by mask
            excludes.add "*.databinding.*" 
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
        // exclude testReleaseUnitTest from instrumentation
        excludeTasks.add "testReleaseUnitTest" 
    }
}
```

An example is available [here](https://github.com/Kotlin/kotlinx-kover/tree/release/examples/android_groovy)

## Implicit plugin dependencies

The artifacts of the JaCoCo or IntelliJ toolkit are loaded during the running of the build. They are
downloaded from the `mavenCentral` repository.

For Kover to work correctly, you need to make sure that `mavenCentral` (or any of its mirrors) is present in
the repository list of the project in which the plugin is applied. Usually you can find it in the root project:

*Kotlin*

```kotlin
repositories {
    mavenCentral()
}
```

*Groovy*

```groovy
repositories {
  mavenCentral()
}
```

## Building locally and Contributing

See [Contributing Guidelines](https://github.com/Kotlin/kotlinx-kover/tree/main/CONTRIBUTING.md).
