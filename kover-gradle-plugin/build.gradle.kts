import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
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

// name of configuration for functionalTest source set with implementation dependencies
val functionalTestImplementation = "functionalTestImplementation"

dependencies {
    // exclude transitive dependency on stdlib, the Gradle version should be used
    compileOnly(kotlin("stdlib"))
    compileOnly(libs.gradlePlugin.kotlin)
    compileOnly(libs.intellij.reporter)

    functionalTestImplementation(kotlin("test"))
    functionalTestImplementation(libs.junit.jupiter)
    functionalTestImplementation(libs.junit.params)
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
        systemProperties["gradleVersion"] = gradle.gradleVersion
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
        setAndroidSdkDir()
        setSystemPropertyFromProject("kover.release.version")
        setSystemPropertyFromProject("kover.test.gradle.version")
        setSystemPropertyFromProject("kover.test.android.sdk")
        setBooleanSystemPropertyFromProject("kover.test.android.disable")
        setBooleanSystemPropertyFromProject("kover.test.junit.logs.info", "testLogsEnabled")
        setBooleanSystemPropertyFromProject("debug", "isDebugEnabled")
    }
}

fun Test.setAndroidSdkDir() {
    if (project.hasProperty("kover.test.android.disable")) {
        // do nothing if android tests are skipped
        return
    }
    val sdkDir = if (project.hasProperty("kover.test.android.sdk")) {
        project.property("kover.test.android.sdk")
    } else {
        environment["ANDROID_HOME"]
    }

    if (sdkDir == null) {
        throw GradleException("Android SDK directory not specified, specify environment variable ANDROID_HOME or parameter -Pkover.test.android.sdk. To skip Android tests pass parameter -Pkover.test.android.disable")
    }

    systemProperties[name] = sdkDir
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

afterEvaluate {
    // Workaround:
    // `kotlin-dsl` itself specifies the language version to ensure compatibility of the Kotlin DSL API
    // Since we ourselves guarantee and test compatibility with previous Gradle versions, we can override language version
    // The easiest way to do this now is to specify the version in the `afterEvaluate` block
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            allWarningsAsErrors.set(true)
            jvmTarget.set(JvmTarget.JVM_1_8)
            languageVersion.set(KotlinVersion.KOTLIN_1_5)
            apiVersion.set(KotlinVersion.KOTLIN_1_5)
            freeCompilerArgs.add("-Xsuppress-version-warnings")
        }
    }
}


tasks.dokkaHtml {
    moduleName.set("Kover Gradle Plugin")
    outputDirectory.set(rootProject.layout.projectDirectory.dir("docs/gradle-plugin/dokka").asFile)

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
