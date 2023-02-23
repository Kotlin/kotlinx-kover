import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.13.0"

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

// override version in deploy
properties["release"]?.let { version = it }

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
    compileOnly("com.android.tools.build:gradle:4.2.2")

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
        // used in build scripts of functional tests
        systemProperties["kotlinVersion"] = kotlinVersion
        systemProperties["koverVersion"] = version
        systemProperties["localRepositoryPath"] = localRepositoryUri.path
        setSystemPropertyFromProject("releaseVersion")
        setSystemPropertyFromProject("gradleVersion")
        setSystemPropertyFromProject("androidSdk")
        setBooleanSystemPropertyFromProject("disableAndroidTests")
        setBooleanSystemPropertyFromProject("debug", "isDebugEnabled")
        setBooleanSystemPropertyFromProject("testLogs", "testLogsEnabled")
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
            implementationClass = "kotlinx.kover.KoverPlugin"
            displayName = "Kotlin Code Coverage Plugin"
            description = "Evaluate code coverage for projects written in Kotlin"
        }
    }
}
