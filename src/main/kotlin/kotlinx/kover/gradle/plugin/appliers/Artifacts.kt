/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import kotlinx.kover.gradle.plugin.commons.AppliedKotlinPlugin
import kotlinx.kover.gradle.plugin.commons.ArtifactNameAttr
import kotlinx.kover.gradle.plugin.commons.CompilationUnit
import kotlinx.kover.gradle.plugin.commons.KotlinPluginAttr
import kotlinx.kover.gradle.plugin.commons.ProjectPathAttr
import kotlinx.kover.gradle.plugin.commons.artifactFilePath
import kotlinx.kover.gradle.plugin.commons.artifactGenerationTaskName
import kotlinx.kover.gradle.plugin.commons.asConsumer
import kotlinx.kover.gradle.plugin.commons.asProducer
import kotlinx.kover.gradle.plugin.commons.externalArtifactConfigurationName
import kotlinx.kover.gradle.plugin.commons.localArtifactConfigurationName
import kotlinx.kover.gradle.plugin.commons.rawReportName
import kotlinx.kover.gradle.plugin.commons.rawReportsRootPath
import kotlinx.kover.gradle.plugin.dsl.KoverNames.DEPENDENCY_CONFIGURATION_NAME
import kotlinx.kover.gradle.plugin.tasks.services.KoverArtifactGenerationTask
import kotlinx.kover.gradle.plugin.tools.CoverageToolVariant
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

/**
 * Create a task to generate an artifact and Gradle Configuration by which the artifact will be published.
 */
internal fun Project.createArtifactGenerationTask(
    variantName: String,
    compilations: List<Provider<Collection<CompilationUnit>>>,
    tests: List<TaskCollection<Test>>,
    toolVariant: CoverageToolVariant,
    kotlinPlugin: AppliedKotlinPlugin
): Variant {
    // local files and compile tasks
    val compileTasks = compilations.map { provider ->
        provider.map { unit -> unit.flatMap { it.compileTasks } }
    }
    val outputs = compilations.map { provider ->
        provider.map { unit -> unit.flatMap { it.outputs } }
    }
    val sources = compilations.map { provider ->
        provider.map { unit -> unit.flatMap { it.sources } }
    }
    val rawReportFiles = project.layout.buildDirectory.dir(rawReportsRootPath())
        .map { dir -> tests.flatten().map { dir.file(rawReportName(it.name, toolVariant.vendor)) } }


    val localArtifactFile = project.layout.buildDirectory.file(artifactFilePath(variantName))

    val artifactGenTask = tasks.register<KoverArtifactGenerationTask>(artifactGenerationTaskName(variantName)) {
        // to generate an artifact, need to compile the entire project and perform all test tasks
        dependsOn(tests)
        dependsOn(compileTasks)

        this.sources.from(sources)
        this.outputDirs.from(outputs)
        this.reports.from(rawReportFiles)
        this.artifactFile.set(localArtifactFile)
    }

    val local = configurations.register(localArtifactConfigurationName(variantName)) {
        // disable generation of Kover artifacts on `assemble`, fix of https://github.com/Kotlin/kotlinx-kover/issues/353
        isVisible = false

        outgoing.artifact(localArtifactFile) {
            asProducer()
            attributes {
                // common Kover artifact attributes
                attribute(ArtifactNameAttr.ATTRIBUTE, project.objects.named(variantName))
                attribute(ProjectPathAttr.ATTRIBUTE, project.objects.named(project.path))
                attribute(KotlinPluginAttr.ATTRIBUTE, project.objects.named(kotlinPlugin.type?.name ?: "NONE"))
            }

            // Before resolving this configuration, it is necessary to execute the task of generating an artifact
            builtBy(artifactGenTask)
        }
    }

    val dependencies = project.configurations.register(externalArtifactConfigurationName(variantName)) {
        asConsumer()

        extendsFrom(project.configurations.getByName(DEPENDENCY_CONFIGURATION_NAME))
    }

    return Variant(variantName, localArtifactFile, artifactGenTask, local, dependencies)
}

/**
 * Comprehensive information sufficient to generate a variant of the report.
 */
internal class Variant(
    val name: String,
    val localArtifact: Provider<RegularFile>,
    val localArtifactGenerationTask: TaskProvider<KoverArtifactGenerationTask>,
    val localArtifactConfiguration: NamedDomainObjectProvider<Configuration>,
    val dependentArtifactsConfiguration: NamedDomainObjectProvider<Configuration>
)