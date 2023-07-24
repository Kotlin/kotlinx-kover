# Offline instrumentation

## Description

To collect code coverage for JVM applications, Kover uses instrumentation -- modification of the bytecode in order to place entry counters in certain blocks of code.

Offline instrumentation is a transformation of the bytecode in compiled class files located somewhere in a file system.
Offline instrumentation is suitable when using runtime environments that do not support Java agents.

## Working steps

### Class instrumentation

For instrumentation, you must first build the application, then the root directories for the class files 
must be passed to Kover CLI as arguments, see [Kover CLI](../cli#offline-instrumentation) for the technical detils.

### Dump coverage result

To run classes instrumented offline, you'll need to add `org.jetbrains.kotlinx:kover-offline` artifact to the application's classpath.

#### Binary report file

You'll also need to pass the system property `kover.offline.report.path` to the application with the path where you want a binary report to be saved.
This binary file can be used to generate human-readable reports using [Kover CLI](../cli#generating-reports).

#### Application classes

Inside the same JVM process in which the tests were run, call Java static method `kotlinx.kover.offline.runtime.api.KoverRuntime.collectByDirs` or `kotlinx.kover.offline.runtime.api.KoverRuntime.collect`.

For correct generation of the report, it is necessary to pass the bytecode of the non-instrumented classes.
This can be done by specifying the directories where the class-files are stored, or a byte array with the bytecode of the application non-instrumented classes.

## Examples

### Gradle example for binary report

Example of a custom binary report production using Kover tool CLI in Gradle
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
    runtimeOnly("org.jetbrains.kotlinx:kover-offline-runtime:0.7.2")
    add("koverCli", "org.jetbrains.kotlinx:kover-cli:0.7.2")

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
