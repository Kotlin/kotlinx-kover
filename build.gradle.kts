plugins {
    kotlin("jvm") version "1.5.30"

    `java-gradle-plugin`
    `maven-publish`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

dependencies {
    implementation(gradleApi())

    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
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

publishing {
    repositories {
        maven {
            url = uri(properties["mavenUri"]?:"default")
            credentials {
                username = properties["mavenUsername"]?.toString()
                password = properties["mavenPassword"]?.toString()
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}


gradlePlugin {
    plugins {
        create("kotlinx-kover") {
            id = "kotlinx-kover"
            implementationClass = "kotlinx.kover.KoverPlugin"
            displayName = "Kotlin Code Coverage Plugin"
            description = "Evaluate code coverage for projects written in Kotlin"
        }
    }
}
