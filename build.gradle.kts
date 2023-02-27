import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")

    `kotlin-dsl`

    `java-gradle-plugin`
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

val kotlinVersion = property("kotlinVersion")
val localRepositoryUri = uri("build/.m2")
val junitParallelism = findProperty("kover.test.junit.parallelism")?.toString()

// override version in deploy
properties["DeployVersion"]?.let { version = it }

sourceSets {
    create("functionalTest") {
        compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath
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

    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")

    testImplementation(kotlin("test"))

    "functionalTestImplementation"("org.junit.jupiter:junit-jupiter:5.9.0")
    "functionalTestImplementation"("org.junit.jupiter:junit-jupiter-params:5.9.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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

        systemProperties["kotlinVersion"] = kotlinVersion
        systemProperties["koverVersion"] = version
        systemProperties["localRepositoryPath"] = localRepositoryUri.path

        // parallel execution
        systemProperties["junit.jupiter.execution.parallel.mode.default"] = "concurrent"
        systemProperties["junit.jupiter.execution.parallel.mode.classes.default"] = "concurrent"
        systemProperties["junit.jupiter.execution.parallel.config.strategy"] = "fixed"
        systemProperties["junit.jupiter.execution.parallel.config.fixed.parallelism"] = junitParallelism?.toIntOrNull()?.toString() ?: "2"
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

fun Test.setBooleanSystemPropertyFromProject(projectPropertyName: String, systemPropertyName: String = projectPropertyName) {
    if (project.hasProperty(projectPropertyName)) systemProperties[systemPropertyName] = true.toString()
}

tasks.check { dependsOn(functionalTest) }


tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        allWarningsAsErrors = true

        // Kover works with the stdlib of at least version `1.4.x`
        languageVersion = "1.4"
        apiVersion = "1.4"
        // Kotlin compiler 1.7 issues a warning if `languageVersion` or `apiVersion` 1.4 is used - suppress it
        freeCompilerArgs = freeCompilerArgs + "-Xsuppress-version-warnings"
    }
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

    publications {
        // `pluginMaven` - standard name for the publication task of the `java-gradle-plugin`
        create<MavenPublication>("pluginMaven") {
            // `java` component will be added by the `java-gradle-plugin` later
            addExtraMavenArtifacts(project, project.sourceSets.main.get().allSource)
        }
    }

    addMavenRepository(project)
    addMavenMetadata()
    publications.withType<MavenPublication>().configureEach {
        signPublicationIfKeyPresent(project)
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
