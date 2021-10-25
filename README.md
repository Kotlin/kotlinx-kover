# Kotlinx-Kover

**Kover** - Gradle plugin for Kotlin code coverage agents: [IntelliJ](https://github.com/JetBrains/intellij-coverage)
and [JaCoCo](https://github.com/jacoco/jacoco).

## Table of content
- [Features](#features)
- [Quickstart](#quickstart)
    - [Apply plugin to project](#apply-plugin-to-project)
        - [Applying plugins with the plugins DSL](#applying-plugins-with-the-plugins-dsl)
        - [Legacy Plugin Application: applying plugins with the buildscript block](#legacy-plugin-application-applying-plugins-with-the-buildscript-block)
    - [Multi-module projects](#multi-module-projects)
        - [Apply plugin for all modules](#apply-plugin-for-all-modules)
        - [Apply plugin only for submodules](#apply-plugin-only-for-submodules)
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
### Apply plugin to project
#### Applying plugins with the plugins DSL
In top level build file

<details open>
<summary>Kotlin</summary>

```kotlin
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.3.0"
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
plugins {
    id 'org.jetbrains.kotlinx.kover' version '0.3.0'
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
        classpath("org.jetbrains.kotlinx:kover:0.3.0")
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
        classpath 'org.jetbrains.kotlinx:kover:0.3.0'
    }
}
  
apply plugin: 'kover'    
```
</details>

The plugin automatically inserted into `check` tasks pipeline and collects coverage during test run,
verifying set validation rules and optionally producing `XML` or `HTML` reports.

### Multi-module projects
There is currently no full support for multi-module projects, you need to apply a plugin for each module.
You can add the plugin to the `build.gradle` or `build.gradle.kts` files in each module, or add this code to the root module

*Cross-module tests are not supported in reports and validation yet. For each test, only the classpath belonging to the current module is taken.*

#### Apply plugin for all modules 
In top level build file

<details open>
<summary>Kotlin</summary>

```kotlin
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.3.0"
}


allprojects {
    apply(plugin = "kover")
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
plugins {
    id 'org.jetbrains.kotlinx.kover' version '0.3.0'
}


allprojects {
    apply plugin: 'kover'
}
```
</details>

#### Apply plugin only for submodules
In top level build file

<details open>
<summary>Kotlin</summary>

```kotlin
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.3.0" apply false
}

subprojects {
    apply(plugin = "kover")
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
plugins {
    id 'org.jetbrains.kotlinx.kover' version '0.3.0' apply(false)
}

subprojects {
    apply plugin: 'kover'
}
```
</details>

## Plugin configuration
In top level build file

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
        generateXml = true
        generateHtml = false
        coverageEngine = 'INTELLIJ'
        xmlReportFile.set(file("$buildDir/custom/report.xml"))
        htmlReportDir.set(file("$buildDir/custom/html"))
        binaryReportFile.set(file("$buildDir/custom/result.bin"))
        includes = ['com\\.example\\..*']
        excludes = ['com\\.example\\.subpackage\\..*']
    }
}
```
</details>

### Explicit version of coverage agent
In top level build file

<details open>
<summary>Kotlin</summary>

```kotlin
extensions.configure<kotlinx.kover.api.KoverExtension>{
    intellijEngineVersion.set("1.0.611")
    jacocoEngineVersion.set("0.8.7")
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
kover {
    intellijEngineVersion.set("1.0.611")
    jacocoEngineVersion.set("0.8.7")
}
```
</details>

###### Verification
For each test task, you can specify one or more rules that check the values of the code coverage counters.

Validation rules work for both types of agents.

*The plugin currently only supports line counter values.*

In top level build file

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
