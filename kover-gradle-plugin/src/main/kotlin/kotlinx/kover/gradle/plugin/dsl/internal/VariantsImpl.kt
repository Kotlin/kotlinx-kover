/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.internal

import kotlinx.kover.gradle.plugin.commons.KoverIllegalConfigException
import kotlinx.kover.gradle.plugin.commons.TOTAL_VARIANT_NAME
import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

internal abstract class KoverVariantsRootConfigImpl @Inject constructor(val objects: ObjectFactory) :
    KoverVariantConfigImpl(objects), KoverVariantsRootConfig {
    internal val customVariants: MutableMap<String, KoverVariantCreateConfigImpl> = mutableMapOf()
    internal val providedVariants: MutableMap<String, KoverVariantConfigImpl> = mutableMapOf()

    init {
        classes.excludeJava.convention(false)
        classes.excludedSourceSets.convention(emptySet())

        instrumentation.excludeAll.set(false)
        instrumentation.excludedClasses.addAll(emptySet())

        testTasks.excluded.addAll(emptySet())
    }

    override fun create(variantName: String, block: Action<KoverVariantCreateConfig>) {
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

    override fun provided(variantName: String, block: Action<KoverVariantConfig>) {
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

    override fun total(block: Action<KoverVariantConfig>) {
        val variantConfig = providedVariants.getOrPut(TOTAL_VARIANT_NAME) {
            objects.newInstance<KoverVariantConfigImpl>().also { newVariant ->
                newVariant.deriveFrom(this)
            }
        }

        block.execute(variantConfig)
    }

}

internal abstract class KoverVariantConfigImpl @Inject constructor(objects: ObjectFactory) : KoverVariantConfig {
    internal val classes: KoverVariantSources = objects.newInstance()
    internal val instrumentation: KoverVariantInstrumentation = objects.newInstance()
    internal val testTasks: KoverVariantTestTasks = objects.newInstance()

    override fun sources(block: Action<KoverVariantSources>) {
        block.execute(classes)
    }

    override fun instrumentation(block: Action<KoverVariantInstrumentation>) {
        block.execute(instrumentation)
    }

    override fun testTasks(block: Action<KoverVariantTestTasks>) {
        block.execute(testTasks)
    }

    internal fun deriveFrom(other: KoverVariantConfigImpl) {
        classes.excludeJava.set(other.classes.excludeJava)
        classes.excludedSourceSets.addAll(other.classes.excludedSourceSets)

        instrumentation.excludeAll.set(other.instrumentation.excludeAll)
        instrumentation.excludedClasses.addAll(other.instrumentation.excludedClasses)

        testTasks.excluded.addAll(other.testTasks.excluded)
    }
}

internal class MergingOptionality(val optional: Boolean, val withDependencies: Boolean)

internal abstract class KoverVariantCreateConfigImpl @Inject constructor(private val objects: ObjectFactory, private val variantName: String) :
    KoverVariantConfigImpl(objects), KoverVariantCreateConfig {
    // variant name -> optionality
    internal val variantsByName: MutableMap<String, MergingOptionality> = mutableMapOf()

    override fun add(vararg variantNames: String, optional: Boolean) {
        for (addedVariantName in variantNames) {
            addByName(addedVariantName, variantName, optional, withDependencies = false)
        }
    }

    override fun addWithDependencies(vararg variantNames: String, optional: Boolean) {
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
