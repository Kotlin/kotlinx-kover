/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import kotlinx.kover.gradle.aggregation.commons.artifacts.KoverUsageAttr
import kotlinx.kover.gradle.plugin.appliers.tasks.VariantReportsSet
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import kotlinx.kover.gradle.plugin.tasks.services.KoverAgentJarTask
import kotlinx.kover.gradle.plugin.tools.CoverageToolFactory
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

/**
 * The first stage of applying the Kover plugin.
 * Objects are created that will be available in user build scripts during the evaluation step.
 */
internal fun prepare(project: Project): KoverContext {
    val koverBucketConfiguration = project.configurations.create(KOVER_DEPENDENCY_NAME) {
        asBucket()
    }

    // Project always consumes its own artifacts
    project.dependencies.add(KOVER_DEPENDENCY_NAME, project)

    val projectExtension = project.extensions.create<KoverProjectExtensionImpl>(
        KOVER_PROJECT_EXTENSION_NAME,
        project.objects,
        project.layout,
        project.path
    )

    val toolProvider = CoverageToolFactory.get(projectExtension)

    // DEPS
    val agentClasspath = project.configurations.create(JVM_AGENT_CONFIGURATION_NAME) {
        asTransitiveDependencies()
    }
    project.dependencies.add(JVM_AGENT_CONFIGURATION_NAME, toolProvider.map { tool -> tool.jvmAgentDependency })

    project.configurations.register("koverEmptyArtifact") {
        // disable generation of Kover artifacts on `assemble`, fix of https://github.com/Kotlin/kotlinx-kover/issues/353
        isVisible = false
        asProducer()
        attributes {
            // common Kover artifact attributes
            attribute(VariantNameAttr.ATTRIBUTE, project.objects.named("!kover##__empty__##"))
            attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(KoverUsageAttr.VALUE))
            attribute(ProjectPathAttr.ATTRIBUTE, project.objects.named(project.path))
        }
    }

    /*
    * Uses lazy jar search for the agent, because an eager search will cause a resolution at the configuration stage,
    * which may affect performance.
    * See https://github.com/Kotlin/kotlinx-kover/issues/235
    */
    val findAgentJarTask = project.tasks.register<KoverAgentJarTask>(FIND_JAR_TASK)
    findAgentJarTask.configure {
        // depends on agent classpath to resolve it in execute-time
        dependsOn(agentClasspath)

        this.tool.convention(toolProvider)
        this.koverDisabled.convention(projectExtension.koverDisabled)
        this.agentJar.set(project.layout.buildDirectory.map { dir -> dir.file(agentFilePath(toolProvider.get().variant)) })
        this.agentClasspath.from(agentClasspath)
    }

    val reporterClasspath = project.configurations.create(JVM_REPORTER_CONFIGURATION_NAME) {
        asTransitiveDependencies()
    }
    project.dependencies.add(
        JVM_REPORTER_CONFIGURATION_NAME,
        toolProvider.map { tool -> tool.jvmReporterDependency })
    project.dependencies.add(
        JVM_REPORTER_CONFIGURATION_NAME,
        toolProvider.map { tool -> tool.jvmReporterExtraDependency })

    val totalReports = VariantReportsSet(
        project,
        TOTAL_VARIANT_NAME,
        ReportVariantType.TOTAL,
        toolProvider,
        projectExtension.reports.total,
        reporterClasspath,
        projectExtension.koverDisabled
    )

    return KoverContext(
        project,
        projectExtension,
        toolProvider,
        findAgentJarTask,
        koverBucketConfiguration,
        agentClasspath,
        reporterClasspath,
        totalReports
    )
}


