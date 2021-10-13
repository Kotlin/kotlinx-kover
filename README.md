# Kotlinx-Kover

**Kover** - Gradle plugin for Kotlin code coverage agents: [IntelliJ](https://github.com/JetBrains/intellij-coverage)
and [JaCoCo](https://github.com/jacoco/jacoco).

## Features

* Collecting the code coverage for JVM test tasks
* XML and HTML reports generation
* Support of Kotlin/JVM, Kotlin Multiplatform and mixed Kotlin-Java sources with zero additional configuration
* Kotlin Android support without dividing them into build types and flavours
* Customizable filters for instrumented classes

## Quickstart

### Add repository

For `settings.gradle`
```
pluginManagement {
    repositories {
        // ... other dependencies
        maven { url 'https://maven.pkg.jetbrains.space/public/p/jb-coverage/maven' }
    }
}
```
### Apply plugin to project

For `build.gradle.kts`
```
plugins {
     id("kotlinx-kover") version "0.2.2"
}
```
For `build.gradle`
```
plugins {
    id 'kotlinx-kover' version '0.2.2'
}
```

The plugin automatically inserted into `check` tasks pipeline and collects coverage during test run,
verifying set validation rules and optionally producing XML or HTML reports.

## Plugin configuration

For `build.gradle.kts`
```
tasks.test {
    extensions.configure(kotlinx.kover.KoverTaskExtension::class) {
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

For `build.gradle`
```
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

### Explicit version of coverage agent

For both `build.gradle.kts` and `build.gradle`
```
kover {
    intellijEngineVersion.set("1.0.608")
    jacocoEngineVersion.set("0.8.7")
}
```

###### Verification
For each test task, you can specify one or more rules that check the values of the code coverage counters.

Validation rules work for both types of agents.

*The plugin currently only supports line counter values.*

for `build.gradle.kts`
```
tasks.test {
    extensions.configure(kotlinx.kover.KoverTaskExtension::class) {
        verificationRule {
            name = "The project doesn't have to be big"
            maxValue = 100000
            valueType = kotlinx.kover.VerificationValueType.COVERED_LINES_COUNT
        }
        verificationRule {
            // rule without custom name
            minValue = 1
            maxValue = 1000
            valueType = kotlinx.kover.VerificationValueType.MISSED_LINES_COUNT
        }
        verificationRule {
            name = "Minimal line coverage rate in percents"
            minValue = 50
            // valueType is kotlinx.kover.VerificationValueType.COVERED_LINES_PERCENTAGE by default
        }
    }
}
```

for `build.gradle`
```
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

