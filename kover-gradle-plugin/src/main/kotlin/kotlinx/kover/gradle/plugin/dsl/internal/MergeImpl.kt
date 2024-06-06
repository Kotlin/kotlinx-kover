/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.internal

import kotlinx.kover.gradle.plugin.commons.KoverIllegalConfigException
import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.specs.Spec

internal abstract class KoverMergingConfigImpl: KoverMergingConfig {
    internal val subprojectsFilters: MutableList<Spec<Project>> = mutableListOf()
    internal val allProjectsFilters: MutableList<Spec<Project>> = mutableListOf()
    internal var sourcesAction: Action<KoverMergingVariantSources>? = null
    internal var instrumentationAction: Action<KoverMergingInstrumentation>? = null
    internal val variantsAction: MutableMap<String, Action<KoverMergingVariantCreate>> = mutableMapOf()

    internal var configured: Boolean = false

    override fun subprojects() {
        subprojectsFilters += Spec<Project> { true }
        configured = true
    }

    override fun subprojects(filter: Spec<Project>) {
        subprojectsFilters += filter
        configured = true
    }

    override fun allProjects() {
        allProjectsFilters += Spec<Project> { true }
        configured = true
    }

    override fun allProjects(filter: Spec<Project>) {
        allProjectsFilters += filter
        configured = true
    }

    override fun projects(vararg projectNameOrPath: String) {
        allProjectsFilters += Spec<Project> { project -> project.name in projectNameOrPath || project.path in projectNameOrPath }
        configured = true
    }

    override fun sources(config: Action<KoverMergingVariantSources>) {
        if (sourcesAction != null) {
            throw KoverIllegalConfigException("An attempt to re-invoke the 'sources' block in merging config. Only one usage is allowed")
        }
        sourcesAction = config
        configured = true
    }

    override fun instrumentation(config: Action<KoverMergingInstrumentation>) {
        if (instrumentationAction != null) {
            throw KoverIllegalConfigException("An attempt to re-invoke the 'instrumentation' block in merging config. Only one usage is allowed")
        }
        instrumentationAction = config
        configured = true
    }

    override fun createVariant(variantName: String, config: Action<KoverMergingVariantCreate>) {
        val prev = variantsAction.put(variantName, config)
        if (prev != null) {
            throw KoverIllegalConfigException("Variant '$variantName' has already been added in merging config. Re-creating a variant with the same name is not allowed")
        }
        configured = true
    }

}
