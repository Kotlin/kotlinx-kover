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
val koverVersion = property("version")

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
    implementation(gradleApi())
    // exclude transitive dependency on stdlib, the Gradle version should be used
    compileOnly(kotlin("stdlib"))

    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    compileOnly("com.android.tools.build:gradle:4.2.2")

    testImplementation(kotlin("test"))

    "functionalTestImplementation"(gradleTestKit())
    "functionalTestImplementation"("org.junit.jupiter:junit-jupiter:5.9.0")
    "functionalTestImplementation"("org.junit.jupiter:junit-jupiter-params:5.9.0")

    // dependencies only for plugin's classpath to work with Kotlin Multi-Platform and Android plugins
    "functionalTestCompileOnly"("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    "functionalTestCompileOnly"("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
    "functionalTestCompileOnly"("org.jetbrains.kotlin:kotlin-compiler-runner:$kotlinVersion")
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

    // While gradle testkit supports injection of the plugin classpath it doesn't allow using dependency notation
    // to determine the actual runtime classpath for the plugin. It uses isolation, so plugins applied by the build
    // script are not visible in the plugin classloader. This means optional dependencies (dependent on applied plugins -
    // for example kotlin multiplatform) are not visible even if they are in regular gradle use. This hack will allow
    // extending the classpath. It is based upon: https://docs.gradle.org/6.0/userguide/test_kit.html#sub:test-kit-classpath-injection
    // Create a configuration to register the dependencies against
    doFirst {
        val file = File(temporaryDir, "plugin-classpath.txt")
        file.writeText(sourceSets["functionalTest"].compileClasspath.joinToString("\n"))
        systemProperties["plugin-classpath"] = file.absolutePath

        // used in build scripts of functional tests
        systemProperties["kotlinVersion"] = kotlinVersion
        systemProperties["koverVersion"] = koverVersion
        systemProperties["infoLogsEnabled"] = project.logger.isInfoEnabled
    }
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

// override version in deploy
properties["DeployVersion"]?.let { version = it }


publishing {
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
