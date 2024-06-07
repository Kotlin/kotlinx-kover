/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers.artifacts

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.internal.KoverVariantConfigImpl
import kotlinx.kover.gradle.plugin.appliers.origin.VariantOrigin
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import kotlinx.kover.gradle.plugin.tasks.services.KoverArtifactGenerationTask
import kotlinx.kover.gradle.plugin.tools.CoverageTool
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

internal sealed class AbstractVariantArtifacts(
    protected val project: Project,
    val variantName: String,
    private val toolProvider: Provider<CoverageTool>,
    private val koverBucketConfiguration: Configuration?,
    private val variantConfig: KoverVariantConfigImpl,
    private val projectExtension: KoverProjectExtensionImpl
) {
    internal val artifactGenTask: TaskProvider<KoverArtifactGenerationTask>
    protected val producerConfiguration: NamedDomainObjectProvider<Configuration>

    internal val consumerConfiguration: NamedDomainObjectProvider<Configuration>

    init {
        artifactGenTask = project.tasks.register<KoverArtifactGenerationTask>(artifactGenerationTaskName(variantName))

        val buildDirectory = project.layout.buildDirectory

        val koverDisabled = projectExtension.koverDisabled
        artifactGenTask.configure {
            artifactFile.set(buildDirectory.file(artifactFilePath(variantName)))
            onlyIf { !koverDisabled.get() }
        }

        val artifactProperty = artifactGenTask.flatMap { task -> task.artifactFile }
        producerConfiguration = project.configurations.register(artifactConfigurationName(variantName)) {
            // disable generation of Kover artifacts on `assemble`, fix of https://github.com/Kotlin/kotlinx-kover/issues/353
            isVisible = false
            asProducer()
            attributes {
                // common Kover artifact attributes
                attribute(VariantNameAttr.ATTRIBUTE, project.objects.named(variantName))
                attribute(ProjectPathAttr.ATTRIBUTE, project.objects.named(project.path))
            }

            outgoing.artifact(artifactProperty) {
                // Before resolving this configuration, it is necessary to execute the task of generating an artifact
                builtBy(artifactGenTask)
            }
        }

        consumerConfiguration = project.configurations.register(externalArtifactConfigurationName(variantName)) {
            asConsumer()
            if (koverBucketConfiguration != null) {
                extendsFrom(koverBucketConfiguration)
            }
        }
    }

    protected fun fromOrigin(origin: VariantOrigin, compilationFilter: (String) -> Boolean = { true }) {
        val excludedTasks = projectExtension.currentProject.instrumentation.disabledForTestTasks
        val disabledInstrumentation = projectExtension.currentProject.instrumentation.disabledForAll

        val tests = origin.tests.matching {
            !disabledInstrumentation.get()
                    // skip this test if it disabled by name
                    && it.name !in excludedTasks.get()
        }

        // filter some compilation, e.g. JVM source sets
        val compilations = origin.compilations.map { it.filterKeys(compilationFilter).values }

        // local files and compile tasks
        val kotlinCompileTasks = compilations.map { compilation -> compilation.mapNotNull { it.kotlin.compileTask } }
        val kotlinOutputs = compilations.map { compilation -> compilation.flatMap { it.kotlin.outputs } }

        val javaCompileTasks =
            compilations.map { compilation -> if (variantConfig.sources.excludeJava.get()) emptyList() else compilation.mapNotNull { it.java.compileTask } }
        val javaOutputs =
            compilations.map { compilation -> if (variantConfig.sources.excludeJava.get()) emptyList() else compilation.flatMap { it.java.outputs } }


        val sources = compilations.map { unit -> unit.flatMap { it.sources } }
        val binReportFiles = project.layout.buildDirectory.dir(binReportsRootPath())
            .map { dir -> tests.map { dir.file(binReportName(it.name, toolProvider.get().variant.vendor)) } }

        artifactGenTask.configure {
            // to generate an artifact, need to compile the entire project and perform all test tasks
            dependsOn(tests)
            dependsOn(kotlinCompileTasks)
            dependsOn(javaCompileTasks)

            this.sources.from(sources)
            this.outputDirs.from(kotlinOutputs)
            this.outputDirs.from(javaOutputs)
            this.reports.from(binReportFiles)
        }

    }

}