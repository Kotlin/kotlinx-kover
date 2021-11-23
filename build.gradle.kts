plugins {
    kotlin("jvm") version "1.5.31"

    `java-gradle-plugin`
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

dependencies {
    implementation(gradleApi())

    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
    compileOnly("com.android.tools.build:gradle:4.2.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        languageVersion = "1.5"

        allWarningsAsErrors = true
        // Suppress the warning about kotlin-reflect 1.3 and kotlin-stdlib 1.4 in the classpath.
        // It's incorrect in this case because we're limiting API version to 1.3 anyway.
        freeCompilerArgs = freeCompilerArgs + "-Xskip-runtime-version-check"
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
    publications.withType(MavenPublication::class).all {
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
