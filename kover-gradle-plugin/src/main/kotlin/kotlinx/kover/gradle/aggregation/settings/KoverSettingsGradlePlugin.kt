/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings

import kotlinx.kover.features.jvm.KoverFeatures
import kotlinx.kover.gradle.aggregation.commons.artifacts.*
import kotlinx.kover.gradle.aggregation.commons.artifacts.KoverContentAttr
import kotlinx.kover.gradle.aggregation.commons.artifacts.KoverUsageAttr
import kotlinx.kover.gradle.aggregation.commons.artifacts.asConsumer
import kotlinx.kover.gradle.aggregation.commons.artifacts.asDependency
import kotlinx.kover.gradle.aggregation.commons.names.KoverPaths
import kotlinx.kover.gradle.aggregation.settings.dsl.KoverNames
import kotlinx.kover.gradle.aggregation.settings.dsl.intern.KoverSettingsExtensionImpl
import kotlinx.kover.gradle.aggregation.commons.names.SettingsNames
import kotlinx.kover.gradle.aggregation.project.KoverProjectGradlePlugin
import kotlinx.kover.gradle.aggregation.project.tasks.KoverAgentSearchTask
import kotlinx.kover.gradle.aggregation.settings.dsl.intern.KoverProjectExtensionImpl
import kotlinx.kover.gradle.aggregation.settings.dsl.intern.ProjectVerificationRuleSettingsImpl
import kotlinx.kover.gradle.aggregation.settings.dsl.intern.inheritFrom
import kotlinx.kover.gradle.aggregation.settings.tasks.*
import kotlinx.kover.gradle.aggregation.settings.tasks.KoverHtmlReportTask
import kotlinx.kover.gradle.aggregation.settings.tasks.KoverVerifyTask
import kotlinx.kover.gradle.aggregation.settings.tasks.KoverXmlReportTask
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.language.base.plugins.LifecycleBasePlugin

public class KoverSettingsGradlePlugin: Plugin<Settings> {

    override fun apply(target: Settings) {
        val objects = target.serviceOf<ObjectFactory>()

        val settingsExtension = target.extensions.create<KoverSettingsExtensionImpl>(KoverNames.settingsExtensionName, objects)

        target.gradle.settingsEvaluated {
            KoverParametersProcessor.process(settingsExtension, providers)
        }

        target.gradle.beforeProject {
            if (!settingsExtension.coverageIsEnabled.get()) {
                return@beforeProject
            }

            val agentDependency = configurations.create(SettingsNames.DEPENDENCY_AGENT) {
                asDependency()
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, objects.named(KoverUsageAttr.VALUE))
                }
            }
            dependencies.add(agentDependency.name, rootProject)

            if (path == Project.PATH_SEPARATOR) {
                configureRootProject(target, settingsExtension)
                configureAgentSearch()
            }

            if (settingsExtension.skipProjects.get().none { excluded -> excluded.match(name, path) }) {
                apply<KoverProjectGradlePlugin>()

                val projectExtension = extensions.getByType<KoverProjectExtensionImpl>()
                projectExtension.instrumentation.includedClasses.convention(settingsExtension.instrumentation.includedClasses)
                projectExtension.instrumentation.excludedClasses.convention(settingsExtension.instrumentation.excludedClasses)
            }
        }
    }

    private fun Project.configureRootProject(settings: Settings, settingsExtension: KoverSettingsExtensionImpl) {
        val projectPath = path

        val dependencyConfig = configurations.create(KOVER_DEPENDENCY_NAME) {
            asDependency()
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(KoverUsageAttr.VALUE))
            }
        }

        val eachProjectRules = mutableMapOf<String, List<ProjectVerificationRuleSettingsImpl>>()

        val rootDependencies = dependencies
        settings.rootProject.walkSubprojects { descriptor ->
            if (settingsExtension.skipProjects.get().none { excluded -> excluded.match(descriptor.name, descriptor.path) }) {
                rootDependencies.add(KOVER_DEPENDENCY_NAME, project(descriptor.path))

                val rules = settingsExtension.reports.verify.eachProjectRule.get().map { action ->
                    val eachProjectRule = objects.newInstance<ProjectVerificationRuleSettingsImpl>(
                        descriptor.name,
                        descriptor.path
                    )
                    eachProjectRule.name = "Coverage for project '${descriptor.name}'"
                    eachProjectRule.filters.inheritFrom(settingsExtension.reports)
                    eachProjectRule.disabled.convention(false)
                    eachProjectRule.groupBy.convention(GroupingEntityType.APPLICATION)
                    action.execute(eachProjectRule)
                    eachProjectRule
                }.filter { rule ->
                    rule.bounds.get().isNotEmpty()
                }

                if (rules.isNotEmpty()) {
                    eachProjectRules[descriptor.path] = rules
                }
            }
        }

        val artifacts = configurations.create("koverArtifactsCollector") {
            asConsumer()
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(KoverUsageAttr.VALUE))
                attribute(KoverContentAttr.ATTRIBUTE, KoverContentAttr.LOCAL_ARTIFACT)
            }
            extendsFrom(dependencyConfig)
        }

        val htmlTask = tasks.register<KoverHtmlReportTask>("koverHtmlReport")
        htmlTask.configure {
            dependsOn(artifacts)
            this.artifacts.from(artifacts)
            group = "verification"
            filters.convention(settingsExtension.reports.asInput())
            title.convention(projectPath)

            htmlDir.convention(layout.buildDirectory.dir(KoverPaths.htmlReportPath()))

            this.onlyIf {
                // `onlyIf` is used to ensure that the path is always printed, even when the task is not running and has the FROM-CACHE outcome
                printPath()
                true
            }
        }

        val xmlTask = tasks.register<KoverXmlReportTask>("koverXmlReport")
        xmlTask.configure {
            dependsOn(artifacts)
            this.artifacts.from(artifacts)
            group = "verification"
            filters.convention(settingsExtension.reports.asInput())
            title.convention(projectPath)

            reportFile.convention(layout.buildDirectory.file(KoverPaths.xmlReportPath()))
        }

        val verifyTask = tasks.register<KoverVerifyTask>("koverVerify")
        verifyTask.configure {
            dependsOn(artifacts)
            this.artifacts.from(artifacts)
            group = "verification"
            warningInsteadOfFailure.convention(settingsExtension.reports.verify.warningInsteadOfFailure)
            rules.convention(
                settingsExtension.reports.verify.rules.map { it.map { rule -> rule.asInput() } }
            )
        }

        val projectVerifyTask = tasks.register<KoverReportVerifyTask>("koverProjectVerify")
        projectVerifyTask.configure {
            dependsOn(artifacts)
            this.artifacts.from(artifacts)
            group = "verification"
            warningInsteadOfFailure.convention(settingsExtension.reports.verify.warningInsteadOfFailure)
            rulesByProjectPath.convention(
                eachProjectRules.mapValues { entry -> entry.value.map { rule -> rule.asInput() } }
            )
        }

        // dependency on check
        tasks.configureEach {
            if (name == LifecycleBasePlugin.CHECK_TASK_NAME) {
                dependsOn(verifyTask)
                dependsOn(projectVerifyTask)
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
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(KoverUsageAttr.VALUE))
                attribute(KoverContentAttr.ATTRIBUTE, KoverContentAttr.AGENT_JAR)
            }

            outgoing.artifact(agentJar) {
                // Before resolving this configuration, it is necessary to execute the task of generating an artifact
                builtBy(findAgentTask)
            }
        }
    }

    private fun String.match(projectName: String, projectPath: String): Boolean {
        return if (this.contains(':')) {
            val correctedPath = if (this.startsWith(':')) {
                this
            } else {
                ":$this"
            }
            correctedPath == projectPath
        } else {
            this == projectName
        }
    }

    private fun ProjectDescriptor.walkSubprojects(block: (ProjectDescriptor) -> Unit) {
        block(this)
        children.forEach { child ->
            child.walkSubprojects(block)
        }
    }

    private val KOVER_DEPENDENCY_NAME = "kover"
}

