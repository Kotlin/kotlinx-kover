import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation

/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    kotlin("jvm")
    id("kover-publishing-conventions")
    id("kover-docs-conventions")
    id("kover-release-conventions")

    alias(libs.plugins.mavenPluginDevelopment)
}


repositories {
    mavenCentral()
}

sourceSets {
    create("functionalTest")
}

// name of configuration for functionalTest source set with implementation dependencies
val functionalTestImplementation = "functionalTestImplementation"


dependencies {
    implementation(project(":kover-features-jvm"))
    implementation(project(":kover-jvm-agent"))

    snapshotRelease(project(":kover-features-jvm"))
    snapshotRelease(project(":kover-jvm-agent"))

    compileOnly(libs.maven.plugin.annotations)
    compileOnly(libs.maven.core)
    implementation(libs.maven.reporting.api)

    functionalTestImplementation(libs.maven.embedder)
    functionalTestImplementation(libs.maven.compat)


    functionalTestImplementation(libs.maven.resolver.basic)
    functionalTestImplementation(libs.maven.resolver.file)
    functionalTestImplementation(libs.maven.resolver.http)
    functionalTestImplementation(libs.maven.slf4j.api)
    functionalTestImplementation(libs.maven.slf4j.provider)


    functionalTestImplementation(kotlin("test"))
    functionalTestImplementation(libs.junit.jupiter)
    functionalTestImplementation(libs.junit.params)
}

mavenPlugin {
    goalPrefix = "kover"
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

    dependsOn(tasks.collectRepository)

    val localRepository = layout.buildDirectory.dir("maven-collected")
    doFirst {
        systemProperties["kotlinVersion"] = embeddedKotlinVersion
        systemProperties["koverVersion"] = version

        val dir = localRepository.get().asFile
        dir.deleteRecursively()
        dir.mkdirs()

        tasks.collectRepository.get().repositories.forEach { repository ->
            repository.copyRecursively(dir)
        }

        systemProperties["snapshotRepository"] = dir.absolutePath
    }
}

tasks.check {
    dependsOn(functionalTest)
}

koverDocs {
    docsDirectory.set("maven-plugin")
    description.set("Kover Maven Plugin")
    callDokkaHtml.set(false)
}
