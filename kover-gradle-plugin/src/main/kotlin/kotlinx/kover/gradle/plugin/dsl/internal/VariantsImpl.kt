/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.internal

import kotlinx.kover.gradle.plugin.commons.KoverIllegalConfigException
import kotlinx.kover.gradle.plugin.commons.TOTAL_VARIANT_NAME
import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

internal abstract class KoverCurrentProjectVariantsConfigImpl @Inject constructor(val objects: ObjectFactory) :
    KoverVariantConfigImpl(objects), KoverCurrentProjectVariantsConfig {
    internal val customVariants: MutableMap<String, KoverVariantCreateConfigImpl> = mutableMapOf()
    internal val providedVariants: MutableMap<String, KoverVariantConfigImpl> = mutableMapOf()
    internal val copyVariants: MutableMap<String, String> = mutableMapOf()
    internal val instrumentation: KoverProjectInstrumentation = objects.newInstance()

    init {
        sources.excludeJava.convention(false)
        sources.excludedSourceSets.convention(emptySet())

        instrumentation.disabledForAll.convention(false)
        instrumentation.excludedClasses.convention(emptySet())
        instrumentation.disabledForTestTasks.convention(emptySet())
    }

    override fun createVariant(variantName: String, block: Action<KoverVariantCreateConfig>) {
        if (variantName == TOTAL_VARIANT_NAME) {
            throw KoverIllegalConfigException("The custom variant name cannot be empty.")
        }

        val variantConfig = customVariants.getOrPut(variantName) {
            val variantConfig = objects.newInstance<KoverVariantCreateConfigImpl>(variantName)
            variantConfig.deriveFrom(this)
            variantConfig
        }

        block.execute(variantConfig)
    }

    override fun copyVariant(variantName: String, originalVariantName: String) {
        if (variantName in copyVariants) {
            throw KoverIllegalConfigException("The copy of custom variant with name $variantName already created.")
        }
        copyVariants[variantName] = originalVariantName
    }

    override fun providedVariant(variantName: String, block: Action<KoverVariantConfig>) {
        if (variantName == TOTAL_VARIANT_NAME) {
            throw KoverIllegalConfigException("The provided variant name cannot be empty.")
        }

        val variantConfig = providedVariants.getOrPut(variantName) {
            val variantConfig = objects.newInstance<KoverVariantConfigImpl>()
            variantConfig.deriveFrom(this)
            variantConfig
        }

        block.execute(variantConfig)
    }

    override fun totalVariant(block: Action<KoverVariantConfig>) {
        val variantConfig = providedVariants.getOrPut(TOTAL_VARIANT_NAME) {
            objects.newInstance<KoverVariantConfigImpl>().also { newVariant ->
                newVariant.deriveFrom(this)
            }
        }

        block.execute(variantConfig)
    }

    override fun instrumentation(block: Action<KoverProjectInstrumentation>) {
        block.execute(instrumentation)
    }

}

internal abstract class KoverVariantConfigImpl @Inject constructor(objects: ObjectFactory) : KoverVariantConfig {
    internal val sources: KoverVariantSources = objects.newInstance()

    override fun sources(block: Action<KoverVariantSources>) {
        block.execute(sources)
    }

    internal fun deriveFrom(other: KoverVariantConfigImpl) {
        sources.excludeJava.set(other.sources.excludeJava)
        sources.excludedSourceSets.addAll(other.sources.excludedSourceSets)
    }
}

internal class MergingOptionality(val optional: Boolean, val withDependencies: Boolean)

internal abstract class KoverVariantCreateConfigImpl @Inject constructor(private val objects: ObjectFactory, private val variantName: String) :
    KoverVariantConfigImpl(objects), KoverVariantCreateConfig {
    // variant name -> optionality
    internal val variantsByName: MutableMap<String, MergingOptionality> = mutableMapOf()

    override fun add(vararg variantNames: String, optional: Boolean) {
        add(listOf(*variantNames), optional)
    }

    override fun addWithDependencies(vararg variantNames: String, optional: Boolean) {
        addWithDependencies(listOf(*variantNames), optional)
    }

    override fun add(variantNames: Iterable<String>, optional: Boolean) {
        for (addedVariantName in variantNames) {
            addByName(addedVariantName, variantName, optional, withDependencies = false)
        }
    }

    override fun addWithDependencies(variantNames: Iterable<String>, optional: Boolean) {
        for (addedVariantName in variantNames) {
            addByName(addedVariantName, variantName, optional, withDependencies = true)
        }
    }

    private fun addByName(addedVariantName: String, variantName: String, optional: Boolean, withDependencies: Boolean) {
        val variant = variantsByName[addedVariantName]
        if (variant != null && variant.optional != optional) {
            throw KoverIllegalConfigException("It is not possible to merge variant '$addedVariantName' to '$variantName' with a different optionality. Merging dependency should be either optional or non-optional.")
        }
        variantsByName[addedVariantName] = MergingOptionality(optional, withDependencies)
    }
}
