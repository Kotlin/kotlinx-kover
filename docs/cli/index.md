# Kover Command Line Interface

This single jar artifact allows using some of the functionality of Kover Toolset through command-line calls.

Java 1.6 or higher is required for execution.

## Commands

### Offline instrumentation

For information about  offline instrumentation, [see](#offline-instrumentation-1).

`java -jar kover-cli.jar instrument [<class-file-path> ...] --dest <dir> [--exclude <class-name>] [--excludeAnnotation <annotation-name>] [--hits] [--include <class-name>]`

| Option                                | Description                                                                                                                | Required | Multiple |
|---------------------------------------|----------------------------------------------------------------------------------------------------------------------------|:--------:|:--------:|
| `<class-file-path>`                   | list of the compiled class-files roots                                                                                     |    +     |    +     |
| --dest <dir>                          | path to write instrumented Java classes to                                                                                 |    +     |          |
| --exclude <class-name>                | filter to exclude classes from instrumentation, wildcards `*` and `?` are acceptable. Excludes have priority over includes |          |    +     |
| --excludeAnnotation <annotation-name> | filter to exclude annotated classes from instrumentation, wildcards `*` and `?` are acceptable                             |          |    +     |
| --hits                                | a flag to enable line hits counting                                                                                        |          |          |
| --include <class-name>                | instrument only specified classes, wildcards `*` and `?` are acceptable                                                    |          |    +     |

### Generating reports
Allows you to generate HTML and XML reports from the existing binary report.

`java -jar kover-cli.jar report [<binary-report-path> ...] --classfiles <class-file-path> [--exclude <class-name>] [--excludeAnnotation <annotation-name>] [--html <html-dir>] [--include <class-name>] --src <sources-path> [--title <html-title>] [--xml <xml-file-path>]`

| Option                                | Description                                                                                             | Required | Multiple |
|---------------------------------------|---------------------------------------------------------------------------------------------------------|:--------:|:--------:|
| `<binary-report-path>`                | list of binary reports files                                                                            |          |    +     |
| --classfiles <class-file-path>        | location of the compiled class-files root (must be original and not instrumented)                       |    +     |    +     |
| --exclude <class-name>                | filter to exclude classes, wildcards `*` and `?` are acceptable                                         |          |    +     |
| --excludeAnnotation <annotation-name> | filter to exclude classes and functions marked by this annotation, wildcards `*` and `?` are acceptable |          |    +     |
| --html <html-dir>                     | generate a HTML report in the specified path                                                            |          |          |
| --include <class-name>                | filter to include classes, wildcards `*` and `?` are acceptable                                         |          |    +     |
| --src <sources-path>                  | location of the source files root                                                                       |    +     |    +     |
| --title <html-title>                  | title in the HTML report                                                                                |          |          |
| --xml <xml-file-path>                 | generate a XML report in the specified path                                                             |          |          |

## Offline instrumentation

Offline instrumentation is suitable when using runtime environments that do not support Java agents.
It instruments the files located in the file system and saves the result to the specified directory.

To run classes instrumented offline, you need to add `org.jetbrains.kotlinx:kover-offline` artifact to the application's classpath.

You also need to pass the system property `kover.offline.report.path` to the application with the path where you want binary report to be saved.

Also see [Gradle example](#gradle-example)

## Examples

### Gradle example
Example of custom using Kover tool CLI in Gradle
```
plugins {
    kotlin("jvm") version "1.8.0"
    application
}

repositories {
    mavenCentral()
}

configurations.register("koverCli") {
    isVisible = false
    isCanBeConsumed = false
    isTransitive = true
    isCanBeResolved = true
}

dependencies {
    runtimeOnly("org.jetbrains.kotlinx:kover-offline-runtime:0.7.0-Beta")
    add("koverCli", "org.jetbrains.kotlinx:kover-cli:0.7.0-Beta")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

fun cliJar(): File {
    val cliConfig = configurations.getByName("koverCli")
    return cliConfig.filter {it.name.startsWith("kover-cli")}.singleFile
}

tasks.compileKotlin {
    doLast {
        val outputDir = destinationDirectory.get().asFile

        exec {
            commandLine(
                "java",
                "-jar",
                cliJar().canonicalPath,
                "instrument",
                outputDir,
                "--dest",
                outputDir,
                "--hits",
            )
        }
    }
}

val binaryReport = layout.buildDirectory.file("kover/report.ic").get().asFile

tasks.test {
    // set system property for binary report path 
    systemProperty("kover.offline.report.path", binaryReport.absolutePath)
}

tasks.register("koverReport") {
    dependsOn(tasks.test)

    doLast {
        val args = mutableListOf<String>()

        args += "java"
        args += "-jar"
        args += cliJar().canonicalPath
        args += "report"
        args += binaryReport.absolutePath
        args += "--classfiles"
        args += tasks.compileKotlin.get().destinationDirectory.get().asFile.absolutePath
        args += "--classfiles"
        args += tasks.compileJava.get().destinationDirectory.get().asFile.absolutePath
        args += "--xml"
        args += layout.buildDirectory.file("reports/kover/report.xml").get().asFile.absolutePath
        args += "--html"
        args += layout.buildDirectory.file("reports/kover/html").get().asFile.absolutePath

        sourceSets.main.get().kotlin.sourceDirectories.files.forEach { src ->
            args += "--src"
            args += src.canonicalPath
        }

        exec { commandLine(args) }
    }
}

```
