## Table of contents

- [Features](#features)
- [Quickstart](#quickstart)
- [Using Kover tasks](#using-kover-tasks)
  - [Kotlin JVM project](#kotlin-jvm-project)
  - [Kotlin Android project](#kotlin-android-project)
  - [Kotlin multiplatform project](#kotlin-multiplatform-project)
- [Multiproject build](#multiproject-build)
  - [Single report over several projects](#single-report-over-several-projects)
- [Filtering reports](#filtering-reports)
- [Kover configuration](configuring)
- [Kover Tasks](#kover-tasks)
  - [Kover default tasks](#kover-default-tasks)
  - [Kover Android tasks](#kover-android-tasks)
- [Instrumentation](#instrumentation)
- [Using JaCoCo](#using-jacoco)
- [Implicit plugin dependencies](#implicit-plugin-dependencies)
- [Gradle Plugin DSL docs](dokka)

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

```kotlin
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.7.0"
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
        classpath("org.jetbrains.kotlinx:kover-gradle-plugin:0.7.0")
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
        classpath 'org.jetbrains.kotlinx:kover-gradle-plugin:0.7.0'
    }
}
  
apply plugin: 'kover'    
```

## Using Kover tasks
Using JVM projects and Android projects differs, use the recommendations below according to your project type.

### Kotlin JVM project
These are projects that use Kotlin Gradle plugin.

For such projects, only the [Kover default tasks](#kover-default-tasks) for generating reports are used: `koverHtmlReport`, `koverXmlReport`, `koverVerify`.

To configure reports, the [default reports settings block](configuring#configuring-default-reports) is used:
```kotlin
koverReport {
    filters {
        // filters for all reports
    }

    defaults {
        xml { /* default XML report config */ }
        html { /* default HTML report config */ }
        verify { /* default verification config */ }
    }
}
```

### Kotlin Android project
These are projects that use Kotlin Android Gradle plugin.

In such projects, a [variant of Kover tasks](#kover-android-tasks) is created for each Android build variant, 
e.g. `koverHtmlReportRelease`, `koverXmlReportRelease`, `koverVerifyRelease` for `release` build variant.

Calling default report tasks (like `koverHtmlReport`) will result in a message `Task 'koverHtmlReport' will be skipped because no tests were executed` in the build log, and the report will not be generated.

To configure reports, the [android reports settings block](configuring#configuring-android-reports) is used:
```kotlin
koverReport {
    filters {
        // filters for reports of all build variants
    }

    androidReports("release") {
        filters {
            // override report filters for all reports for `release` build variant
            // all filters specified by the level above cease to work
        }
        
        xml { /* XML report config for `release` build variant */ }
        html { /* HTML report config for `release` build variant */ }
        verify { /* verification config for `release` build variant */ }
    }
}
```

### Kotlin multiplatform project
These are projects that use Kotlin Multiplatfrom Gradle plugin (Kotlin MPP).
Working with such projects is identical to working with JVM and Android projects simultaneously, which are described above.

Both JVM and Android targets may be present in such projects, depending on the declared targets, it is necessary to use the appropriate Kover tasks.

If only targets with the JVM type are declared, then the [Kover default tasks](#kover-default-tasks) should be used.

If only a target with the Android type is declared then the [Kover android tasks](#kover-android-tasks) should be used.
In this case, starting default task like `koverHtmlReport` will result in a message `Task 'koverHtmlReport' will be skipped because no tests were executed` in the build log, and the report will not be generated.

A special case is when JVM and Android targets are present in the project at the same time -
in this case, Kover default tasks (e.g. `koverHtmlReport`) will generate reports for all JVM targets, and Kover Android tasks (e.g. `koverHtmlReportRelease`) generate reports for specific build variant.
However, if there is a need for a single report to contain measurements for both JVM targets and for any Android build variant, [reports merging](configuring#merging-reports) can be used.
```kotlin
koverReport {
    defaults {
        // adds the contents of the reports of `release` Android build variant to default reports
        mergeWith("release")
    }
}
```

## Multiproject build
[Multi-project build](https://docs.gradle.org/current/userguide/multi_project_builds.html#sec:creating_multi_project_builds) (sometimes called multimodule project) 
- this is a Gradle build in which there are several Gradle projects (most often each of which has its own `build.gradle` or `build.gradle.kts` file)

In this case, it is necessary to [apply Kover plugin](#quickstart) in each subproject for which coverage needs to be measured.
Thus, for each subproject, it will be possible to generate a report showing the coverage of only those classes that are declared in this subproject.

### Single report over several projects
If it is necessary to use coverage from several subprojects in one report, then a special dependency should be added to the subproject.
```kotlin
dependency {
    kover(project(":core"))
}
```
This dependency must be specified at the root level in the build script file (`build.gradle` or `build.gradle.kts`).

If such a dependency is specified, for example, in the `:app` project, the `:app:koverHtmlReport` task call will generate one HTML report containing the `:app` and `:core` classes.

Running `:core:koverHtmlReport` generates an HTML report only for classes of `:core` subproject.

It is important that the settings from the `koverReport { ... }` extension only affect the project in which the report is generated.
For the example above, you need to configure `koverReport` only in the `:app` project, settings from dependencies (`:core`) are not inherited during the execution of `:app:koverHtmlReport`.

However, the settings specified in the `kover { ... }` extension affect all reports, even those generated using dependencies.

## Filtering reports
For a full description of working with filters, see the [extended manual](configuring#reports-filtering).

The simplest way to filter reports is to add common filters for all reports:
```kotlin
koverReport {
    filters {
        excludes {
            // exclusion rules - classes to exclude from report
            classes("com.example.Class1", "com.example.Class2")
        }
        includes {
            // inclusion rules - classes only those that will be present in reports
            classes("com.example.Class1", "com.example.Class3")
        }
    }
}
```
If inclusion and exclusion rules are specified at the same time, then excludes have priority over includes.
This means that even if a class is specified in both the inclusion and exclusion rules, it will be excluded from the report (e.g. class `com.example.Class1` above).

[Wildcards](configuring#class-name-with-wildcards) `*` and `?` are allowed in class names.

## Kover configuration


## Kover Tasks
### Kover default tasks
Default Kover tasks - are tasks that are always created when applying the Kover Gradle plugin.

These tasks generate a report for Kotlin/JVM or Java sources, Android source codes are not included in these reports by default.

Running Kover default tasks cause compilation of all Kotlin JVM and Java classes, and also run the corresponding tests for JVM targets.

Kover default task list:
- `koverHtmlReport` - Generate HTML report for Kotlin/JVM or Java classes
- `koverXmlReport` - Generate XML report for Kotlin/JVM or Java classes
- `koverVerify` - Verifies code coverage metrics of Kotlin/JVM or Java classes based on configured rules

Example:
```
gradlew koverHtmlReport
```

### Kover Android tasks
The Android project has several build variants, each of which can have its own sources, test sets, as well as classes with the same names can be duplicated in different variants.
For these reasons, their own variants of Kover tasks are created for each build variant.

Each named variant of the Kover task is tied to one specific Android build variant with the same name.

Running Kover Android tasks cause compilation of the code of the corresponding variant, and also run unit tests for this build variant.

Kover Android task list:
- `koverHtmlReport<Name>` - Generate HTML report for classes of `<Name>` build variant
- `koverXmlReport<Name>` - Generate XML report for classes of `<Name>` build variant
- `koverVerify<Name>` - Verifies code coverage metrics of classes of `<Name>` build variant based on configured rules

Example:
```
gradlew koverHtmlReportRelease
```


## Instrumentation
To collect code coverage for JVM applications, Kover uses instrumentation - modification of the bytecode in order to place entry counters in certain blocks of the code.

Instrumentation can be performed by a special JVM agent that transforms the loaded classes right while the application is running.
This approach is called on the fly instrumentation.
This is the approach used in the Kover Gradle Plugin.

In some cases, using JVM instrumentation agent can lead to the generation of invalid bytecode for some classes, as a result, the application may not work correctly.
In this case, it is necessary to disable the instrumentation of the problem class. As a side effect, the coverage of such a class will not be measured, and if the excluded class was declared in the project, then its coverage in the report will always be 0.
```kotlin
kover {
    excludeInstrumentation {
        classes("com.example.ExcludedClass")
    }
}
```

[Wildcards](configuring#class-name-with-wildcards) `*` and `?` are allowed in class names.

Typical error messages encountered with instrumentation problems:
```
No instrumentation registered! Must run under a registering instrumentation.
```

```
java.lang.VerifyError
	at java.instrument/sun.instrument.InstrumentationImpl.retransformClasses0(Native Method)
```


## Using JaCoCo
Kover Gradle plugin provides the ability to use the [JaCoCo Coverage Library](https://github.com/jacoco/jacoco) to collect class coverage and generate reports.
However, full feature compatibility is not guaranteed.

To use JaCoCo with the default version in the project, specify
```kotlin
kover {
    useJacoco()
}
```
for the custom version
```kotlin
kover {
    useJacoco("0.8.8")
}
```

**It is important that if [dependencies in multi-project builds](#single-report-over-several-projects) are specified, only one type of coverage library (embedded Kover or JaCoCo) is used across these projects**


## Implicit plugin dependencies

Kover and JaCoCo dependencies are loaded during the running of the build. They are
downloaded from the `mavenCentral` repository.

For Kover to work correctly, you need to make sure that `mavenCentral` (or any of its mirrors) is present in
the repository list of the project in which the plugin is applied. Usually you can find it in the root project:

```kotlin
repositories {
    mavenCentral()
}
```
