/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.commons

import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.attributes.*

/**
 * Attribute to mark any Kover artifact.
 */
internal interface KoverMarkerAttr : Named {
    companion object {
        val ATTRIBUTE = Attribute.of(
            "kotlinx.kover.marker",
            VariantNameAttr::class.java
        )
    }
}

/**
 * Name of the published artifact.
 */
internal interface VariantNameAttr : Named {
    companion object {
        val ATTRIBUTE = Attribute.of(
            "kotlinx.kover.variant.name",
            VariantNameAttr::class.java
        )
    }
}

/**
 * Build type of the Android project.
 */
internal interface VariantOriginAttr : Named {
    companion object {
        val ATTRIBUTE = Attribute.of(
            "kotlinx.kover.variant.origin",
            VariantOriginAttr::class.java
        )
    }
}

/**
 * Path of the project for which the artifact is published.
 */
internal interface ProjectPathAttr : Named {
    companion object {
        val ATTRIBUTE = Attribute.of(
            "kotlinx.kover.project.path",
            ProjectPathAttr::class.java
        )
    }
}

/**
 * Build type of the Android project.
 */
internal interface BuildTypeAttr : Named {
    companion object {
        val ATTRIBUTE = Attribute.of(
            "kotlinx.kover.variant.android.build-type",
            BuildTypeAttr::class.java
        )
    }
}

/**
 * Product flavor of the Android project.
 */
internal interface ProductFlavorAttr : Named {
    companion object {
        fun of(flavorDimension: String): Attribute<ProductFlavorAttr> {
            return Attribute.of(
                "kotlinx.kover.variant.android.flavor-dimension:$flavorDimension",
                ProductFlavorAttr::class.java
            )
        }
    }
}

/**
 * Mark this [Configuration] as a transitive.
 *
 * Dependencies must be added to this configuration, as a result of its resolution, artifacts from these dependencies are returned.
 *
 * See: https://docs.gradle.org/7.5.1/userguide/declaring_dependencies.html
 */
internal fun Configuration.asTransitiveDependencies() {
    // leave this for compatibility with older versions
    @Suppress("DEPRECATION")
    isVisible = false
    isCanBeConsumed = false
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
    // leave this for compatibility with older versions
    @Suppress("DEPRECATION")
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
    // leave this for compatibility with older versions
    @Suppress("DEPRECATION")
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
    // leave this for compatibility with older versions
    @Suppress("DEPRECATION")
    isVisible = false
    isCanBeResolved = true
    // this config consumes modules from OTHER projects, and cannot be consumed by other projects
    isCanBeConsumed = false
}
