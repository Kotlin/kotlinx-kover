**Kover** - Gradle plugin for Kotlin code coverage agents

## Table of content
- [Main features of the plugin](#main-features-of-the-plugin)
  - [Features of the IntelliJ Coverage](#features-of-the-intellij-coverage)
- [Basic Gradle Setup](#basic-gradle-setup)
  - [Add repository](#add-repository)
  - [Apply plugin to project](#apply-plugin-to-project)
- [Customize settings](#customize-settings)

# Main features of the plugin
* Uses IntelliJ or `JaCoCo` agent to collect the code coverage for JVM test tasks
* Uses IntelliJ or `JaCoCo` reporter to generate XML and HTML reports
* Allows to specify the version of the IntelliJ and `JaCoCo` agents
* Works with `Kotlin/JVM` and `Kotlin Multiplatform` sources
* Supports the work with `Kotlin` `Android` sources without dividing them into build types and flavours
* Supports custom filtering instrumented classes

### Features of the IntelliJ Coverage
* Supports `Kotlin/JVM` projects
* Supports `Kotlin Multiplatform` projects
* Supports `inline` functions, including those declared in multiplatform sources or called from tests
* Generates test coverage `HTML` report
* Generates test coverage `XML` report compatible with `JaCoCo`'s `XML`
* Supports custom filtering instrumented classes by `RegExp`


# Basic Gradle Setup

### Add repository
in `settings.gradle.kts` or `settings.gradle`

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
in `build.gradle.kts` or `build.gradle`

<details open>
<summary>Kotlin</summary>
    
```kotlin
plugins {
    // ... other plugins
    id("kotlinx-kover") version "0.2.2"
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
plugins {
    // ... other plugins
    id 'kotlinx-kover' version '0.2.2'
}
```
</details>
   
# Customize settings
in `build.gradle.kts` or `build.gradle`

<details open>
<summary>Kotlin</summary>
    
```kotlin
tasks.test {
    extensions.configure(kotlinx.kover.KoverTaskExtension::class) {
        useJacoco = false
        xmlReport = true
        htmlReport = false
        xmlReportFile.set(file("$buildDir/custom/report.xml"))
        htmlReportDir.set(file("$buildDir/custom/html"))
        binaryFile.set(file("$buildDir/custom/result.bin"))
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
        useJacoco = false
        xmlReport = true
        htmlReport = false
        xmlReportFile.set(file("$buildDir/custom/report.xml"))
        htmlReportDir.set(file("$buildDir/custom/html"))
        binaryFile.set(file("$buildDir/custom/result.bin"))
        includes = ['com\\.example\\..*']
        excludes = ['com\\.example\\.subpackage\\..*']
    }
}
```
</details>

### Change version of agents
in `build.gradle.kts` or `build.gradle`

<details open>
<summary>Kotlin</summary>
    
```kotlin
kover {
    intellijAgentVersion.set("1.0.608")
    jacocoAgentVersion.set("0.8.7")
}
```
</details>

<details>
<summary>Groovy</summary>
    
```groovy
kover {
    intellijAgentVersion.set("1.0.608")
    jacocoAgentVersion.set("0.8.7")
}
```
</details>
