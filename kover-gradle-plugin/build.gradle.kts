import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlinx.binaryCompatibilityValidator)
    alias(libs.plugins.kotlinx.dokka)

    `kotlin-dsl`
    `java-gradle-plugin`

    id("kover-publishing-conventions")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

val localRepositoryUri = uri("build/.m2")
val junitParallelism = findProperty("kover.test.junit.parallelism")?.toString()

sourceSets {
    create("functionalTest") {
        compileClasspath += files(sourceSets.main.get().output, configurations.testRuntimeClasspath)
        runtimeClasspath += output + compileClasspath
    }
}

// adding the ability to use internal classes inside functional tests
kotlin.target.compilations.run {
    getByName("functionalTest").associateWith(getByName(KotlinCompilation.MAIN_COMPILATION_NAME))
}

dependencies {
    // exclude transitive dependency on stdlib, the Gradle version should be used
    compileOnly(kotlin("stdlib"))

    compileOnly(libs.gradlePlugin.kotlin)

    compileOnly(libs.intellij.reporter)

    testImplementation(kotlin("test"))

    "functionalTestImplementation"(libs.junit.jupiter)
    "functionalTestImplementation"(libs.junit.params)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}


val functionalTest by tasks.registering(Test::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = sourceSets["functionalTest"].output.classesDirs
    classpath = sourceSets["functionalTest"].runtimeClasspath

    // use JUnit 5
    useJUnitPlatform()

    dependsOn(tasks.named("publishAllPublicationsToLocalRepository"))
    doFirst {
        // basic build properties
        setSystemPropertyFromProject("kover.test.kotlin.version")

        systemProperties["kotlinVersion"] = embeddedKotlinVersion
        systemProperties["koverVersion"] = version
        systemProperties["localRepositoryPath"] = localRepositoryUri.path

        // parallel execution
        systemProperties["junit.jupiter.execution.parallel.mode.default"] = "concurrent"
        systemProperties["junit.jupiter.execution.parallel.mode.classes.default"] = "concurrent"
        systemProperties["junit.jupiter.execution.parallel.config.strategy"] = "fixed"
        systemProperties["junit.jupiter.execution.parallel.config.fixed.parallelism"] =
            junitParallelism?.toIntOrNull()?.toString() ?: "2"
        // this is necessary if tests are run for debugging, in this case it is more difficult to stop at the test you need when they are executed in parallel and you are not sure on which test the execution will pause
        systemProperties["junit.jupiter.execution.parallel.enabled"] = if (junitParallelism == "no") "false" else "true"


        // customizing functional tests
        setSystemPropertyFromProject("kover.release.version")
        setSystemPropertyFromProject("kover.test.gradle.version")
        setSystemPropertyFromProject("kover.test.android.sdk")
        setBooleanSystemPropertyFromProject("kover.test.android.disable")
        setBooleanSystemPropertyFromProject("kover.test.junit.logs.info", "testLogsEnabled")
        setBooleanSystemPropertyFromProject("debug", "isDebugEnabled")
    }
}

fun Test.setSystemPropertyFromProject(name: String) {
    if (project.hasProperty(name)) systemProperties[name] = project.property(name)
}

fun Test.setBooleanSystemPropertyFromProject(
    projectPropertyName: String,
    systemPropertyName: String = projectPropertyName
) {
    if (project.hasProperty(projectPropertyName)) systemProperties[systemPropertyName] = true.toString()
}

tasks.check { dependsOn(functionalTest) }


tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        allWarningsAsErrors = true
        jvmTarget = "1.8"

        // Kover works with the stdlib of at least version `1.4.x`
        languageVersion = "1.4"
        apiVersion = "1.4"
        // Kotlin compiler 1.7 issues a warning if `languageVersion` or `apiVersion` 1.4 is used - suppress it
        freeCompilerArgs = freeCompilerArgs + "-Xsuppress-version-warnings"
    }
}

tasks.dokkaHtml {
    moduleName.set("Kover Gradle Plugin")
    outputDirectory.set(layout.projectDirectory.dir("docs/gradle-plugin/dokka").asFile)

    if (project.hasProperty("releaseVersion")) {
        moduleVersion.set(project.property("releaseVersion") as String)
    }

    dokkaSourceSets.configureEach {
        // source set configuration section
        perPackageOption {
            skipDeprecated.set(true)
        }
    }
}

extensions.configure<Kover_publishing_conventions_gradle.KoverPublicationExtension> {
    description.set("Kover Gradle Plugin - Kotlin code coverage")
    //`java-gradle-plugin` plugin already creates publication with name `pluginMaven`
    addPublication.set(false)
}

publishing {
    repositories {
        /**
         * Maven repository in build directory to store artifacts for using in functional tests.
         */
        maven(localRepositoryUri) {
            name = "local"
        }
    }
}


gradlePlugin {
    plugins {
        create("Kover") {
            id = "org.jetbrains.kotlinx.kover"
            implementationClass = "kotlinx.kover.gradle.plugin.KoverGradlePlugin"
            displayName = "Gradle Plugin for Kotlin Code Coverage Tools"
            description = "Evaluate code coverage for projects written in Kotlin"
        }
    }
}


// ====================
// Release preparation
// ====================
tasks.register("prepareRelease") {
    dependsOn(tasks.named("dokkaHtml"))

    doLast {
        if (!project.hasProperty("releaseVersion")) {
            throw GradleException("Property 'releaseVersion' is required to run this task")
        }
        val releaseVersion = project.property("releaseVersion") as String
        val prevReleaseVersion = project.property("kover.release.version") as String

        val dir = layout.projectDirectory
        val rootDir = rootProject.layout.projectDirectory

        rootDir.file("gradle.properties").asFile.patchProperties(releaseVersion)
        rootDir.file("CHANGELOG.md").asFile.patchChangeLog(releaseVersion)

        rootDir.file("README.md").asFile.replaceInFile(prevReleaseVersion, releaseVersion)

        // replace versions in examples
        dir.dir("examples").asFileTree.matching {
            include("**/*gradle")
            include("**/*gradle.kts")
        }.files.forEach {
            it.replaceInFile(prevReleaseVersion, releaseVersion)
        }

        // replace versions in docs
        rootDir.dir("docs").asFileTree.files.forEach {
            it.replaceInFile(prevReleaseVersion, releaseVersion)
        }
    }
}

fun File.patchChangeLog(releaseVersion: String) {
    val oldContent = readText()
    writer().use {
        it.appendLine("$releaseVersion / ${LocalDate.now().format(DateTimeFormatter.ISO_DATE)}")
        it.appendLine("===================")
        it.appendLine("TODO add changelog!")
        it.appendLine()
        it.append(oldContent)
    }
}

fun File.patchProperties(releaseVersion: String) {
    val oldLines = readLines()
    writer().use { writer ->
        oldLines.forEach { line ->
            when {
                line.startsWith("version=") -> writer.append("version=").appendLine(increaseSnapshotVersion(releaseVersion))
                line.startsWith("kover.release.version=") -> writer.append("kover.release.version=").appendLine(releaseVersion)
                else -> writer.appendLine(line)
            }
        }
    }
}

// modify version '1.2.3' to '1.2.4' and '1.2.3-Beta' to '1.2.3-SNAPSHOT'
fun increaseSnapshotVersion(releaseVersion: String): String {
    // remove postfix like '-Alpha'
    val correctedVersion = releaseVersion.substringBefore('-')
    if (correctedVersion != releaseVersion) {
        return "$correctedVersion-SNAPSHOT"
    }

    // split version 0.0.0 to int parts
    val parts = correctedVersion.split('.')
    val newVersion = parts.mapIndexed { index, value ->
        if (index == parts.size - 1) {
            (value.toInt() + 1).toString()
        } else {
            value
        }
    }.joinToString(".")

    return "$newVersion-SNAPSHOT"
}

fun File.replaceInFile(old: String, new: String) {
    val newContent = readText().replace(old, new)
    writeText(newContent)
}
