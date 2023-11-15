/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.KoverIllegalConfigException
import kotlinx.kover.gradle.plugin.commons.KoverMarkerAttr
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier


internal class DependencyCheckListener : DependencyResolutionListener {

    override fun beforeResolve(dependencies: ResolvableDependencies) {
        // no-op before
    }

    override fun afterResolve(dependencies: ResolvableDependencies) {
        // Only Kover dependency consumers requests KoverMarkerAttr attribute
        dependencies.attributes.getAttribute(KoverMarkerAttr.ATTRIBUTE) ?: return

        /*
        Gradle can resolve a random artifact from the dependency if the desired option was not found, see:
          https://github.com/Kotlin/kotlinx-kover/issues/478
          https://github.com/gradle/gradle/issues/27019
          https://docs.gradle.org/current/userguide/variant_model.html#sec:variant-aware-matching
        */
        dependencies.artifacts.resolvedArtifacts.get().forEach { artifact ->

            val marker = artifact.variant.attributes.getAttribute(KoverMarkerAttr.ATTRIBUTE)
            if (marker == null) {
                // no Kover marker - Kover plugin don't applied in the dependency
                val dependencyPath = artifact.id.componentIdentifier.projectPath
                val projectPath = dependencies.resolutionResult.rootComponent.get().id.projectPath

                val message = "Kover plugin is not applied in dependency '$dependencyPath' of project '$projectPath'. Apply Kover plugin in the '$dependencyPath' project."
                throw KoverIllegalConfigException(message)
            }

            val requestedBuildType = dependencies.attributes.getAttribute(BuildTypeAttr.ATTRIBUTE)
            val givenBuildType = artifact.variant.attributes.getAttribute(BuildTypeAttr.ATTRIBUTE)
            if (requestedBuildType != null && givenBuildType == null) {
                // consumer expects android variant but default variant were selected - invalid selection
                val dependencyPath = artifact.id.componentIdentifier.projectPath
                val projectPath = dependencies.resolutionResult.rootComponent.get().id.projectPath

                val suffix = dependencies.name.substringAfter("koverExternalArtifacts")
                val variantName = if (suffix.isNotEmpty()) suffix.replaceFirstChar { it.lowercase() } else ""
                val message =
                    "Kover android variant '$variantName' was not matched with any variant from dependency '$dependencyPath' of project '$projectPath'. Check that the Kover plugin is applied in the '$dependencyPath' project and there is a variant compatible with '$variantName' in it."
                throw KoverIllegalConfigException(message)
            }
        }
    }

    private val ComponentIdentifier.projectPath: String
        get() = if (this is ProjectComponentIdentifier) projectPath else displayName
}