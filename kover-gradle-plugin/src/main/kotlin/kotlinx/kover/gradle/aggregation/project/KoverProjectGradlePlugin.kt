/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.project

import kotlinx.kover.gradle.aggregation.commons.android.convertAggregatedVariant
import kotlinx.kover.gradle.aggregation.commons.artifacts.*
import kotlinx.kover.gradle.aggregation.commons.names.KoverPaths.binReportName
import kotlinx.kover.gradle.aggregation.commons.names.KoverPaths.binReportsRootPath
import kotlinx.kover.gradle.aggregation.commons.names.PluginId.KOTLIN_JVM_PLUGIN_ID
import kotlinx.kover.gradle.aggregation.commons.names.PluginId.KOTLIN_MULTIPLATFORM_PLUGIN_ID
import kotlinx.kover.gradle.aggregation.commons.names.SettingsNames
import kotlinx.kover.gradle.aggregation.commons.utils.bean
import kotlinx.kover.gradle.aggregation.commons.utils.hasSuper
import kotlinx.kover.gradle.aggregation.project.instrumentation.JvmOnFlyInstrumenter
import kotlinx.kover.gradle.aggregation.project.tasks.ArtifactGenerationTask
import kotlinx.kover.gradle.aggregation.settings.dsl.intern.KoverProjectExtensionImpl
import kotlinx.kover.gradle.plugin.commons.ANDROID_BASE_PLUGIN_ID
import kotlinx.kover.gradle.plugin.commons.KOTLIN_ANDROID_PLUGIN_ID
import kotlinx.kover.gradle.plugin.commons.KoverCriticalException
import kotlinx.kover.gradle.plugin.commons.hasAndroid9WithKotlin
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.attributes.Usage
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.io.File

internal class KoverProjectGradlePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val projectExtension = target.configureExtension()
        target.configureInstrumentation(projectExtension)
        target.configureArtifactGeneration()
    }

    private fun Project.configureExtension(): KoverProjectExtensionImpl {
        val projectExtension = extensions.create<KoverProjectExtensionImpl>("kover")
        projectExtension.instrumentation.excludedClasses.convention(emptySet())
        projectExtension.instrumentation.includedClasses.convention(emptySet())
        return projectExtension
    }

    private fun Project.configureInstrumentation(projectExtension: KoverProjectExtensionImpl) {
        val koverJarDependency = configurations.getByName(SettingsNames.DEPENDENCY_AGENT)
        val jarConfig = configurations.create("agentJarSource") {
            asConsumer()
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(KoverUsageAttr.VALUE))
                attribute(KoverContentAttr.ATTRIBUTE, KoverContentAttr.AGENT_JAR)
            }
            extendsFrom(koverJarDependency)
        }
        JvmOnFlyInstrumenter.instrument(tasks.withType<Test>(), jarConfig, projectExtension.instrumentation)
    }

    private fun Project.configureArtifactGeneration() {
        val taskGraph = gradle.taskGraph

        val artifactFile = layout.buildDirectory.file("kover/kover.artifact")

        // we create task immediately because of mustRunAfter
        val generateArtifactTask = tasks.register<ArtifactGenerationTask>("koverGenerateArtifact").get()
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

        // Register onVariants callback only for the new Variants API (since AGP 9.0.0)
        pluginManager.withPlugin(ANDROID_BASE_PLUGIN_ID) {
            val androidComponents = project.extensions.findByName("androidComponents")?.bean()
            // a very old AGP (< 7.0.0)
                ?: return@withPlugin

            val majorVersion = androidComponents.beanOrNull("pluginVersion")?.valueOrNull<Int>("major") ?: 0
            // add onVariants callback only for new Variant API (since AGP 9.0.0)
            if (majorVersion < 9) return@withPlugin


            val action = Action<Any> {
                val variant = bean().convertAggregatedVariant()
                generateArtifactTask.android9Variants.add(variant)
            }

            val selector = androidComponents("selector").bean().invoke("all") ?: throw KoverCriticalException("Return value for selector().all() is null for project '${project.path}'")

            androidComponents("onVariants",  selector, action)
        }

        val compilations = project.layout.buildDirectory.map {
            val compilations = when {
                pluginManager.hasPlugin(KOTLIN_JVM_PLUGIN_ID) || pluginManager.hasPlugin(KOTLIN_ANDROID_PLUGIN_ID) || hasAndroid9WithKotlin() -> {
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
                if (compilationName == "test" || compilationName.endsWith("Test")) return@filter false

                val taskPath = projectPath + (if (projectPath == Project.PATH_SEPARATOR) "" else Project.PATH_SEPARATOR) + compilation["compileTaskProvider"]["name"].value<String>()
                taskGraph.hasTask(taskPath)
            }
        }

        val compilationMap = compilations.map { allCompilations ->
            allCompilations.associate { compilation ->
                // since AGP 9.0.0 srcDirs and classesDirs are empty
                val sourceDirs = compilation["allKotlinSourceSets"].sequence()
                    .flatMap { sourceSet -> sourceSet["kotlin"]["srcDirs"].sequence().map { it.value<File>() } }
                    .toSet()
                var outputDirs = compilation["output"]["classesDirs"].value<ConfigurableFileCollection>().files
                if (outputDirs.isEmpty()) {
                    // since AGP 9.0.0 classesDirs are empty, so we should get the compilation tasks output
                    val kotlinCompileTask = compilation.value<TaskProvider<Task>>("compileTaskProvider").get()
                    val javaCompileTask = compilation.value<TaskProvider<Task>>("compileJavaTaskProvider").get()
                    val kotlinOutputs = kotlinCompileTask.outputs.files.filter { file -> file.name == "classes" }.files
                    val javaOutputs = javaCompileTask.outputs.files.filter { file -> file.name == "classes" }.files

                    outputDirs = kotlinOutputs + javaOutputs
                }

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
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(KoverUsageAttr.VALUE))
                attribute(KoverContentAttr.ATTRIBUTE, KoverContentAttr.LOCAL_ARTIFACT)
            }

            outgoing.artifact(artifactFile) {
                // Before resolving this configuration, it is necessary to execute the task of generating an artifact
                builtBy(generateArtifactTask)
            }
        }
    }

}


