/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.commons

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.attributes.*
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.*

private interface SetupName : Named {
    companion object {
        val ATTRIBUTE = Attribute.of(
            "kotlinx.kover.setup.name",
            SetupName::class.java
        )
    }
}

private interface ProjectPath : Named {
    companion object {
        val ATTRIBUTE = Attribute.of(
            "kotlinx.kover.project.path",
            ProjectPath::class.java
        )
    }
}

private interface KotlinPlugin : Named {
    companion object {
        val ATTRIBUTE = Attribute.of(
            "kotlinx.kover.kotlin.plugin",
            KotlinPlugin::class.java
        )
    }
}


internal fun AttributeContainer.setupName(name: String, objects: ObjectFactory) {
    attribute(SetupName.ATTRIBUTE, objects.named(name))
}

internal fun AttributeContainer.projectPath(path: String, objects: ObjectFactory) {
    attribute(ProjectPath.ATTRIBUTE, objects.named(path))
}

internal fun AttributeContainer.kotlinType(kotlinPlugin: AppliedKotlinPlugin, objects: ObjectFactory) {
    attribute(KotlinPlugin.ATTRIBUTE, objects.named(kotlinPlugin.type?.name ?: "NONE"))
}

/**
 * Mark this [Configuration] as a transitive.
 *
 * Dependencies must be added to this configuration, as a result of its resolution, artifacts from these dependencies are returned.
 *
 * See: https://docs.gradle.org/7.5.1/userguide/declaring_dependencies.html
 */
internal fun Configuration.asTransitiveDependencies() {
    isVisible = false
    isTransitive = true
    isCanBeResolved = true
}

/**
 * Mark this [Configuration] as a bucket for declaring dependencies.
 *
 * Bucket combines artifacts from the specified dependencies,
 * and allows you to resolve in consumer configuration.
 *
 * See: https://docs.gradle.org/7.5.1/userguide/declaring_dependencies.html#sec:resolvable-consumable-configs
 */
internal fun Configuration.asBucket() {
    isVisible = true
    isCanBeResolved = false
    isCanBeConsumed = false
}

/**
 * Mark this [Configuration] as a 'producer' that exposes artifacts and their dependencies for consumption by other
 * projects
 *
 * See: https://docs.gradle.org/7.5.1/userguide/declaring_dependencies.html#sec:resolvable-consumable-configs
 */
internal fun Configuration.asProducer() {
    isVisible = false
    isCanBeResolved = false
    // this configuration produces modules that can be consumed by other projects
    isCanBeConsumed = true
}


/**
 * Mark this [Configuration] as consumable, which means it’s an "exchange" meant for consumers.
 *
 * See: https://docs.gradle.org/7.5.1/userguide/declaring_dependencies.html#sec:resolvable-consumable-configs
 */
internal fun Configuration.asConsumer() {
    isVisible = false

    isCanBeResolved = true
    // this config consumes modules from OTHER projects, and cannot be consumed by other projects
    isCanBeConsumed = false
}
