# Kover Gradle Plugin

[![Kotlin Alpha](https://kotl.in/badges/alpha.svg)](https://kotlinlang.org/docs/components-stability.html)
[![JetBrains incubator project](https://jb.gg/badges/incubator.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

**Kover** - Gradle plugin for Kotlin code coverage tools: [Kover](https://github.com/JetBrains/intellij-coverage)
and [JaCoCo](https://github.com/jacoco/jacoco).

Minimum supported version of `Gradle` is `6.8`.

For more information, please refer to the [documentation of the latest release](https://Kotlin.github.io/kotlinx-kover)

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
