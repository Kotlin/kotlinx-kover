# Offline instrumentation

## Description

To collect code coverage for JVM applications, Kover uses instrumentation -- modification of the bytecode in order to place entry counters in certain blocks of code.

Offline instrumentation is a transformation of the bytecode in compiled class files located somewhere in a file system.
Offline instrumentation is suitable when using runtime environments that do not support Java agents.

## Working steps

### Class instrumentation

#### Instrumentation by Kover CLI
The Kover CLI is a fat jar that needs to be called and passed certain commands through arguments.

For instrumentation, you must first build the application, then the root directories for the class files
must be passed to Kover CLI as arguments, see [Kover CLI](../cli#offline-instrumentation) for the technical details.

#### Instrumentation by Kover Features
Kover Features is a library that provides capabilities similar to Kover CLI and Kover Gradle plugin.

You can declare a dependency on Kover Features using following coordinates: `org.jetbrains.kotlinx:kover-features-jvm:0.9.2`.

Then you can use the Kover Features classes to instrument the bytecode of each class:
```kotlin
import kotlinx.kover.features.jvm.KoverFeatures
  // ...

  val instrumenter = KoverFeatures.createOfflineInstrumenter()
  
  // read class-file with name `fileName` bytes to `classBytes`
  val instrumentedBytes = instrumenter.instrument(classBytes, fileName)
  // save `instrumentedBytes` to file
```

### Dump coverage result

To run classes instrumented offline (with CLI) or programmatically (with Kover Features), you'll need to add `org.jetbrains.kotlinx:kover-offline-runtime` artifact to the application's classpath.

There are several ways to get coverage:

- [Save binary report file when the JVM is shut down](#save-binary-report-on-shut-down)
- [Save binary report in runtime by Kover API](#save-binary-report-in-runtime)
- [Get binary report in runtime by Kover API](#get-binary-report-in-runtime)
- [Get coverage details in runtime by Kover API](#get-coverage-details-in-runtime)

Binary reports are presented in `ic` format, and can later be used in the [Kover CLI](../cli#generating-reports) to generate HTML or XML reports.

#### Save binary report on shut down

You'll need to pass the system property `kover.offline.report.path` to the application with the path where you want a binary report to be saved.

If this property is specified, then at the end of the JVM process,
the binary coverage report will be saved to a file at the path passed in the parameter value.

If the file does not exist, it will be created. If a file with that name already exists, it will be overwritten.

#### Save binary report in runtime

Inside the same JVM process in which the tests were run, call Java static method `kotlinx.kover.offline.runtime.api.KoverRuntime.saveReport`.

If the file does not exist, it will be created. If a file already exists, it will be overwritten.

Calling this method is allowed only after all tests are completed. If the method is called in parallel with the execution of the measured code, the coverage value is unpredictable.

#### Get binary report in runtime

Inside the same JVM process in which the tests were run, call Java static method `kotlinx.kover.offline.runtime.api.KoverRuntime.getReport`.
This method will return byte array with a binary coverage report, which can be saved to a file later.
It is important that this byte array cannot be appended to an already existing file, and must be saved to a separate file.

Calling this method is allowed only after all tests are completed. If the method is called in parallel with the execution of the measured code, the coverage value is unpredictable.

#### Get coverage details in runtime

Inside the same JVM process in which the tests were run, call Java static method `kotlinx.kover.offline.runtime.api.KoverRuntime.collectByDirs` or `kotlinx.kover.offline.runtime.api.KoverRuntime.collect`.

For correct generation of the report, it is necessary to pass the bytecode of the non-instrumented classes.
This can be done by specifying the directories where the class-files are stored, or a byte array with the bytecode of the application non-instrumented classes.

Calling these methods is allowed only after all tests are completed. If the method is called in parallel with the execution of the measured code, the coverage value is unpredictable.

See [example](#example-of-using-the-api).

## Logging
`org.jetbrains.kotlinx:kover-offline-runtime` has its own logging system.

By default, warning and error messages are printed to standard error stream. 

It is also possible to save all log messages to a file, to do this, you need to pass the system property `kover.offline.log.file.path` with path to the log file.

## Examples

### Gradle example for binary report

Example of a custom binary report production using Kover tool CLI in Gradle
```kotlin
plugins {
    kotlin("jvm") version ("2.2.0")
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
    runtimeOnly("org.jetbrains.kotlinx:kover-offline-runtime:0.9.2")
    add("koverCli", "org.jetbrains.kotlinx:kover-cli:0.9.2")

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

### Example of using the API
```kotlin
    val reportFile = Files.createTempFile("kover-report-", ".ic").toFile()

    // save binary report to file
    KoverRuntime.saveReport(reportFile)
    
    // get binary report as byte array
    val bytes = KoverRuntime.getReport()
    
    // check reports are same
    val bytesFromFile = reportFile.readBytes()
    assertContentEquals(bytesFromFile, bytes)


    // the directory with class files can be transferred using the system property, any other methods are possible
    val outputDir = File(System.getProperty("output.dir"))
    val coverage = KoverRuntime.collectByDirs(listOf(outputDir))

    // check coverage of `readState` method
    assertEquals(3, coverage.size)
    val coverageByClass = coverage.associateBy { cov -> cov.className }

    val mainClassCoverage = coverageByClass.getValue("org.jetbrains.kotlinx.kover.MainClass")
    assertEquals("Main.kt", mainClassCoverage.fileName)
    assertEquals(4, mainClassCoverage.methods.size)

    val coverageBySignature = mainClassCoverage.methods.associateBy { meth -> meth.signature }
    val readStateCoverage = coverageBySignature.getValue("readState()Lorg/jetbrains/kotlinx/kover/DataClass;")

    assertEquals(1, readStateCoverage.hit)
    assertEquals(1, readStateCoverage.lines.size)
    assertEquals(6, readStateCoverage.lines[0].lineNumber)
    assertEquals(1, readStateCoverage.lines[0].hit)
    assertEquals(0, readStateCoverage.lines[0].branches.size)
```

see [full example](https://github.com/Kotlin/kotlinx-kover/tree/main/kover-offline-runtime/examples/runtime-api)
