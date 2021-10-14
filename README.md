# Kotlinx-Kover

**Kover** - Gradle plugin for Kotlin code coverage agents: [IntelliJ](https://github.com/JetBrains/intellij-coverage)
and [JaCoCo](https://github.com/jacoco/jacoco).

## Table of content
- [Features](#features)
- [Quickstart](#quickstart)
    - [Add repository](#add-repository)
    - [Apply plugin to project](#apply-plugin-to-project)
- [Plugin configuration](#plugin-configuration)
  - [Explicit version of coverage agent](#explicit-version-of-coverage-agent)
    - [Verification](#verification)

## Features

* Collecting the code coverage for `JVM` test tasks
* `XML` and `HTML` reports generation
* Support of `Kotlin/JVM`, `Kotlin Multiplatform` and mixed `Kotlin-Java` sources with zero additional configuration
* `Kotlin Android` support without dividing them into build types and flavours
* Customizable filters for instrumented classes

## Quickstart

### Add repository

For `settings.gradle.kts`

<details open>
<summary>Kotlin</summary>

```kotlin
pluginManagement {
    repositories {
        // ... other dependencies
        maven("https://maven.pkg.jetbrains.space/public/p/jb-coverage/maven")
    }
}
```
</details>

For `settings.gradle`

<details>
<summary>Groovy</summary>

```groovy
pluginManagement {
    repositories {
        // ... other dependencies
        maven { url 'https://maven.pkg.jetbrains.space/public/p/jb-coverage/maven' }
    }
}
```
</details>

### Apply plugin to project

For `build.gradle.kts`

<details open>
<summary>Kotlin</summary>

```kotlin
plugins {
    id("kotlinx-kover") version "0.2.2"
}
```
</details>

For `build.gradle`

<details>
<summary>Groovy</summary>

```groovy
plugins {
    id 'kotlinx-kover' version '0.2.2'
}
```
</details>

The plugin automatically inserted into `check` tasks pipeline and collects coverage during test run,
verifying set validation rules and optionally producing `XML` or `HTML` reports.

## Plugin configuration

For `build.gradle.kts`

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.test {
    extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
        generateXml = true
        generateHtml = false
        coverageEngine = CoverageEngine.INTELLIJ
        xmlReportFile.set(file("$buildDir/custom/report.xml"))
        htmlReportDir.set(file("$buildDir/custom/html"))
        binaryFile.set(file("$buildDir/custom/result.bin"))
        includes = listOf("com\\.example\\..*")
        excludes = listOf("com\\.example\\.subpackage\\..*")
    }
}
```
</details>

For `build.gradle`

<details>
<summary>Groovy</summary>

```groovy
tasks.test {
    kover {
        generateXml = true
        generateHtml = false
        coverageEngine = CoverageEngine.INTELLIJ
        xmlReportFile.set(file("$buildDir/custom/report.xml"))
        htmlReportDir.set(file("$buildDir/custom/html"))
        binaryFile.set(file("$buildDir/custom/result.bin"))
        includes = ['com\\.example\\..*']
        excludes = ['com\\.example\\.subpackage\\..*']
    }
}
```
</details>

### Explicit version of coverage agent

For `build.gradle.kts`

<details open>
<summary>Kotlin</summary>

```kotlin
kover {
    intellijAgentVersion.set("1.0.611")
    jacocoAgentVersion.set("0.8.7")
}
```
</details>

For `build.gradle`

<details>
<summary>Groovy</summary>

```groovy
kover {
    intellijAgentVersion.set("1.0.611")
    jacocoAgentVersion.set("0.8.7")
}
```
</details>

###### Verification
For each test task, you can specify one or more rules that check the values of the code coverage counters.

Validation rules work for both types of agents.

*The plugin currently only supports line counter values.*

For `build.gradle.kts`

<details open>
<summary>Kotlin</summary>

```kotlin
tasks.test {
    extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
        verificationRule {
            name = "The project doesn't have to be big"
            maxValue = 100000
            valueType = kotlinx.kover.api.VerificationValueType.COVERED_LINES_COUNT
        }
        verificationRule {
            // rule without custom name
            minValue = 1
            maxValue = 1000
            valueType = kotlinx.kover.api.VerificationValueType.MISSED_LINES_COUNT
        }
        verificationRule {
            name = "Minimal line coverage rate in percents"
            minValue = 50
            // valueType is kotlinx.kover.api.VerificationValueType.COVERED_LINES_PERCENTAGE by default
        }
    }
}
```
</details>

For `build.gradle`

<details>
<summary>Groovy</summary>

```groovy
tasks.test {
    kover {
        verificationRule {
            name = "The project doesn't have to be big"
            maxValue = 100000
            valueType = 'COVERED_LINES_COUNT'
        }
        verificationRule {
            // rule without custom name
            minValue = 1
            maxValue = 1000
            valueType = 'MISSED_LINES_COUNT'
        }
        verificationRule {
            name = "Minimal line coverage rate in percents"
            minValue = 50
            // valueType is 'COVERED_LINES_PERCENTAGE' by default
        }
    }
}
```
</details>
