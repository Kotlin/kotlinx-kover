# Kover

[![Kotlin Alpha](https://kotl.in/badges/alpha.svg)](https://kotlinlang.org/docs/components-stability.html)
[![JetBrains incubator project](https://jb.gg/badges/incubator.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

Kover is a set of solutions for collecting test coverage of Kotlin code compiled for JVM and Android platforms.

Kover Toolset:
- [Kover Gradle Plugin](#kover-gradle-plugin)
- [Kover CLI](#kover-cli)
- [Kover offline instrumentation](#kover-offline-instrumentation)
- [Kover JVM agent](#kover-jvm-agent)
- [Kover features artifact](#kover-features-artifact)

## Kover Gradle Plugin
For full information about latest stable release of Kover Gradle Plugin, please refer to the [documentation](https://kotlin.github.io/kotlinx-kover/gradle-plugin).

### Features

* Collection of code coverage through `JVM` tests (JS and native targets are not supported yet).
* Generating `HTML` and `XML` reports.
* Support for `Kotlin JVM`, `Kotlin Multiplatform` projects.
* Support for `Kotlin Android` projects with build variants (instrumentation tests executing on the Android device are not supported yet).
* Support mixed `Kotlin` and `Java` sources
* Verification rules with bounds in the Gradle plugin to keep track of coverage.
* Using JaCoCo library in Gradle plugin as an alternative for coverage measuring and report generation.

The recommended way of applying Kover is with the
[plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block).

Minimum supported version of `Gradle` is `6.8`.

Add the following to your top-level build file:

<details open>
<summary>Kotlin</summary>

```kotlin
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.8.0"
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
plugins {
    id 'org.jetbrains.kotlinx.kover' version '0.8.0'
}
```
</details>

After you applied Kover Gradle plugin, Kover tasks will be created for generating reports and verification. 
E.g. to generate HTML report run `./gradlew koverHtmlReport` - this will automatically start code compilation, execution of instrumented tests, and an HTML report will be generated with measurement results in the build folder.

It is also important that after applying Kover Gradle plugin, during the running tests, the classes are modified (instrumented) when loaded into the JVM which may lead to some performance degradation, or affect concurrent tests.

### Legacy Plugin Application

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
        classpath("org.jetbrains.kotlinx:kover-gradle-plugin:0.8.0")
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
        classpath 'org.jetbrains.kotlinx:kover-gradle-plugin:0.8.0'
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

## Kover CLI
Standalone JVM application used for offline instrumentation and generation of human-readable reports.

[Documentation of the Kover CLI](https://kotlin.github.io/kotlinx-kover/cli).

## Kover offline instrumentation
Offline instrumentation is the modification of class-files stored on disk to measure their coverage.

The ways of offline instrumentation and running of the instrumented applications are described in the [documentation](https://kotlin.github.io/kotlinx-kover/offline-instrumentation).

## Kover JVM agent
JVM agent is a jar file that modifies the bytecode of loaded into the JVM classes in order to measure coverage.
[Documentations](https://kotlin.github.io/kotlinx-kover/jvm-agent).

## Kover features artifact
A JVM dependency that allows to programmatically instrument class-files on a disk.

[Documentation of Kover features artifact](https://kotlin.github.io/kotlinx-kover/offline-instrumentation/#instrumentation-by-kover-features)

## Building locally and Contributing

See [Contributing Guidelines](https://github.com/Kotlin/kotlinx-kover/tree/main/CONTRIBUTING.md).

