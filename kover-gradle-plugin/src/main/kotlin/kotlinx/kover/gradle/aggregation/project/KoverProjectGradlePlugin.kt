/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.project

import kotlinx.kover.features.jvm.KoverFeatures
import kotlinx.kover.gradle.aggregation.commons.artifacts.CompilationInfo
import kotlinx.kover.gradle.aggregation.commons.artifacts.KoverContentAttr
import kotlinx.kover.gradle.aggregation.commons.artifacts.asConsumer
import kotlinx.kover.gradle.aggregation.commons.artifacts.asProducer
import kotlinx.kover.gradle.aggregation.commons.names.KoverPaths.binReportName
import kotlinx.kover.gradle.aggregation.commons.names.KoverPaths.binReportsRootPath
import kotlinx.kover.gradle.aggregation.commons.names.PluginId.KOTLIN_JVM_PLUGIN_ID
import kotlinx.kover.gradle.aggregation.commons.names.PluginId.KOTLIN_MULTIPLATFORM_PLUGIN_ID
import kotlinx.kover.gradle.aggregation.commons.names.SettingsNames
import kotlinx.kover.gradle.aggregation.commons.utils.bean
import kotlinx.kover.gradle.aggregation.commons.utils.hasSuper
import kotlinx.kover.gradle.aggregation.project.instrumentation.InstrumentationFilter
import kotlinx.kover.gradle.aggregation.project.instrumentation.JvmOnFlyInstrumenter
import kotlinx.kover.gradle.aggregation.project.tasks.ArtifactGenerationTask
import kotlinx.kover.gradle.aggregation.project.tasks.KoverAgentSearchTask
import kotlinx.kover.gradle.plugin.commons.KoverCriticalException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.io.File

internal class KoverProjectGradlePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        if (target.path == Project.PATH_SEPARATOR) {
            target.configureAgentSearch()
        }

        target.configureInstrumentation()
        target.configureArtifactGeneration()
    }

    private fun Project.configureInstrumentation() {
        val koverJarDependency = configurations.getByName(SettingsNames.DEPENDENCY_AGENT)
        val jarConfig = configurations.create("agentJarSource") {
            asConsumer()
            attributes {
                attribute(KoverContentAttr.ATTRIBUTE, KoverContentAttr.AGENT_JAR)
            }
            extendsFrom(koverJarDependency)
        }
        JvmOnFlyInstrumenter.instrument(tasks.withType<Test>(), jarConfig, InstrumentationFilter(setOf(), setOf()))
    }

    private fun Project.configureArtifactGeneration() {
        val taskGraph = gradle.taskGraph

        val artifactFile = layout.buildDirectory.file("kover/kover.artifact")

        // we create task immediately because of mustRunAfter
        val generateArtifactTask = tasks.create<ArtifactGenerationTask>("koverGenerateArtifact")
        generateArtifactTask.outputFile.set(artifactFile)

        // add tests
        val testTasks = tasks.withType<Test>().matching { task ->
            taskGraph.hasTask(task.path)
        }

        val binReportFiles = project.layout.buildDirectory.dir(binReportsRootPath())
            .map { dir -> testTasks.map { dir.file(binReportName(it.name)) } }

        val exts = extensions
        val pluginManager = pluginManager
        val projectPath = path

        val compilations = project.layout.buildDirectory.map {
            val compilations = when {
                pluginManager.hasPlugin(KOTLIN_JVM_PLUGIN_ID) -> {
                    val kotlin = exts.findByName("kotlin")?.bean()
                        ?: throw KoverCriticalException("Kotlin JVM extension not found")
                    kotlin["target"]["compilations"].sequence()
                }

                pluginManager.hasPlugin(KOTLIN_MULTIPLATFORM_PLUGIN_ID) -> {
                    val kotlin = exts.findByName("kotlin")?.bean()
                        ?: throw KoverCriticalException("Kotlin JVM multiplatform not found")
                    kotlin["targets"].sequence()
                        .filter {
                            val platformType = it["platformType"]["name"].value<String>()
                            platformType == "jvm" || platformType == "androidJvm"
                        }.flatMap {
                            it["compilations"].sequence()
                        }
                }
                else -> emptySequence()
            }

            compilations.filter { compilation ->
                val compilationName = compilation["name"].value<String>()
                if (compilationName == "test") return@filter false

                val taskPath = projectPath + (if (projectPath == Project.PATH_SEPARATOR) "" else Project.PATH_SEPARATOR) + compilation["compileTaskProvider"]["name"].value<String>()
                taskGraph.hasTask(taskPath)
            }
        }

        val compilationMap = compilations.map { allCompilations ->
            allCompilations.associate { compilation ->
                val sourceDirs = compilation["allKotlinSourceSets"].sequence()
                    .flatMap { sourceSet -> sourceSet["kotlin"]["srcDirs"].sequence().map { it.value<File>() } }
                    .toSet()
                val outputDirs = compilation["output"]["classesDirs"].value<ConfigurableFileCollection>().files

                compilation["name"].value<String>() to CompilationInfo(sourceDirs, outputDirs)
            }
        }

        // TODO describe the trick
        tasks.withType<Test>().configureEach {
            generateArtifactTask.mustRunAfter(this)
        }
        tasks.withType<Task>().configureEach {
            if (this.hasSuper("KotlinCompilationTask")) {
                generateArtifactTask.mustRunAfter(this)
            }
        }
        tasks.withType<JavaCompile>().configureEach {
            generateArtifactTask.mustRunAfter(this)
        }

        generateArtifactTask.compilations.putAll(compilationMap)
        generateArtifactTask.reportFiles.from(binReportFiles)

        configurations.register("KoverArtifactProducer") {
            asProducer()
            attributes {
                attribute(KoverContentAttr.ATTRIBUTE, KoverContentAttr.LOCAL_ARTIFACT)
            }

            outgoing.artifact(artifactFile) {
                // Before resolving this configuration, it is necessary to execute the task of generating an artifact
                builtBy(generateArtifactTask)
            }
        }
    }


    private fun Project.configureAgentSearch() {
        val agentConfiguration = configurations.create("AgentConfiguration")
        dependencies.add(agentConfiguration.name, "org.jetbrains.kotlinx:kover-jvm-agent:${KoverFeatures.version}")

        val agentJar = layout.buildDirectory.file("kover/kover-jvm-agent-${KoverFeatures.version}.jar")

        val findAgentTask = tasks.register<KoverAgentSearchTask>("koverAgentSearch")
        findAgentTask.configure {
            this@configure.agentJar.set(agentJar)
            dependsOn(agentConfiguration)
            agentClasspath.from(agentConfiguration)
        }

        configurations.register("AgentJar") {
            asProducer()
            attributes {
                attribute(KoverContentAttr.ATTRIBUTE, KoverContentAttr.AGENT_JAR)
            }

            outgoing.artifact(agentJar) {
                // Before resolving this configuration, it is necessary to execute the task of generating an artifact
                builtBy(findAgentTask)
            }
        }
    }

}


