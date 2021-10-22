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
### Apply plugin to project
#### Applying plugins with the plugins DSL
For `build.gradle.kts`
```
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.3.0"
}
```
For `build.gradle`
```
plugins {
    id 'org.jetbrains.kotlinx.kover' version '0.3.0'
}
```

#### Legacy Plugin Application: applying plugins with the buildscript block
For `build.gradle.kts`
```
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

For `build.gradle`
```
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

The plugin automatically inserted into `check` tasks pipeline and collects coverage during test run,
verifying set validation rules and optionally producing XML or HTML reports.

### Multi-module projects
There is currently no full support for multi-module projects, you need to apply a plugin for each module.
You can add the plugin to the `build.gradle` or `build.gradle.kts` files in each module, or add this code to the root module

*Cross-module tests are not supported in reports and validation yet. For each test, only the classpath belonging to the current module is taken.*

#### apply plugin for all modules 
For `build.gradle.kts`
```
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.3.0"
}


allprojects {
    apply(plugin = "kover")
}
```

For `build.gradle`
```
plugins {
    id 'org.jetbrains.kotlinx.kover' version '0.3.0'
}


allprojects {
    apply plugin: 'kover'
}
```

#### apply plugin only for submodules
For `build.gradle.kts`
```
plugins {
     id("org.jetbrains.kotlinx.kover") version "0.3.0" apply false
}

subprojects {
    apply(plugin = "kover")
}
```
For `build.gradle`
```
plugins {
    id 'org.jetbrains.kotlinx.kover' version '0.3.0' apply(false)
}

subprojects {
    apply plugin: 'kover'
}
```

## Plugin configuration

For `build.gradle.kts`
```
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

For `build.gradle`
```
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

### Explicit version of coverage agent

For both `build.gradle.kts` and `build.gradle`
```
kover {
    intellijEngineVersion.set("1.0.611")
    jacocoEngineVersion.set("0.8.7")
}
```

If you are using `build.gradle.kts` file and applying plugins with the buildscript block the code above won't work, it can be rewritten like this:
```
extensions.configure<kotlinx.kover.api.KoverExtension>{
    intellijEngineVersion.set("1.0.611")
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
