/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import kotlinx.kover.gradle.plugin.commons.KOVER_PLUGIN_ID
import kotlinx.kover.gradle.plugin.commons.KoverIllegalConfigException
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.specs.Spec

internal fun KoverContext.prepareMerging() {
    if (!projectExtension.merge.configured) return

    val projects = selectProjects()
    configSelectedProjects(projects)
}


private fun KoverContext.selectProjects(): List<Project> {
    val result = linkedMapOf(project.path to project)

    fun addProjectIfFiltered(project: Project, filters: List<Spec<Project>>) {
        if (result.containsKey(project.path)) return

        val pass = filters.any { it.isSatisfiedBy(project) }
        if (pass) {
            result[project.path] = project
        }
    }

    val mergeConfig = projectExtension.merge
    val subprojectsFilters = mergeConfig.subprojectsFilters
    val allProjectsFilters = mergeConfig.allProjectsFilters

    // by default if no filters are explicitly specified, then throw exception
    if (subprojectsFilters.isEmpty() && allProjectsFilters.isEmpty()) {
        throw KoverIllegalConfigException("No projects have been specified to be used in the merged report in '${project.path}'. Please specify 'subprojects()', 'projects()', or 'allProjects()' in 'merge {}' block or delete this block.")
    }

    project.subprojects.forEach { subproject ->
        addProjectIfFiltered(subproject, subprojectsFilters)
    }
    project.rootProject.allprojects.forEach { subproject ->
        addProjectIfFiltered(subproject, allProjectsFilters)
    }

    return result.values.toList()
}

private fun KoverContext.configSelectedProjects(targetProjects: List<Project>) {
    val koverExtension = projectExtension

    targetProjects.forEach { targetProject ->
        if (targetProject.path != project.path) {
            // is another project used as dependency

            // apply plugin in dependency
            targetProject.pluginManager.apply(KOVER_PLUGIN_ID)
            // add dependency to kover configuration
            project.dependencies.add(koverBucketConfiguration.name, targetProject)
            // apply configs
            koverExtension.configBeforeFinalize(targetProject)
        } else {
            // apply configs without JaCoCo
            koverExtension.configBeforeFinalize(targetProject, false)
        }
    }
}

private fun KoverProjectExtensionImpl.configBeforeFinalize(targetProject: Project, applyJacoco: Boolean = true) {
    val targetExtension = targetProject.extensions.getByType(KoverProjectExtensionImpl::class.java)

    targetExtension.beforeFinalize {
        if (applyJacoco) {
            // Propagate whether jacoco used or not
            targetExtension.useJacoco.set(useJacoco)
            targetExtension.jacocoVersion.set(jacocoVersion)
        }

        merge.sourcesAction?.execute(targetExtension.currentProject.sources.wrap(targetProject))
        merge.instrumentationAction?.execute(targetExtension.currentProject.instrumentation.wrap(targetProject))
        merge.variantsAction.forEach { (variantName, action) ->
            targetExtension.currentProject.createVariant(variantName) {
                action.execute(wrap(targetProject))
            }
        }
    }
}

private fun KoverVariantSources.wrap(project: Project): KoverMergingVariantSources {
    return object : KoverMergingVariantSources {
        override val excludeJava: Property<Boolean> = this@wrap.excludeJava
        override val excludedSourceSets: SetProperty<String> = this@wrap.excludedSourceSets
        override val excludedOtherSourceSets: SetProperty<String> = this@wrap.excludedOtherSourceSets
        override val project: Project = project
    }
}

private fun KoverProjectInstrumentation.wrap(project: Project): KoverMergingInstrumentation {
    return object : KoverMergingInstrumentation {
        override val disabledForAll: Property<Boolean> = this@wrap.disabledForAll
        override val disabledForTestTasks: SetProperty<String> = this@wrap.disabledForTestTasks
        override val excludedClasses: SetProperty<String> = this@wrap.excludedClasses
        override val includedClasses: SetProperty<String> = this@wrap.includedClasses
        override val project: Project = project
    }
}
private fun KoverVariantCreateConfig.wrap(project: Project): KoverMergingVariantCreate {
    return object : KoverMergingVariantCreate {
        override val sources: KoverVariantSources = this@wrap.sources
        override fun sources(block: Action<KoverVariantSources>) = this@wrap.sources(block)
        override fun add(vararg variantNames: String, optional: Boolean) = this@wrap.add(*variantNames, optional = optional)
        override fun addWithDependencies(vararg variantNames: String, optional: Boolean) = this@wrap.addWithDependencies(*variantNames, optional = optional)
        override fun add(variantNames: Iterable<String>, optional: Boolean) = this@wrap.add(variantNames, optional = optional)
        override fun addWithDependencies(variantNames: Iterable<String>, optional: Boolean) = this@wrap.addWithDependencies(variantNames, optional = optional)

        override val project: Project = project
    }
}
