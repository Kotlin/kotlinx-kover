/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.appliers.origin.AllVariantOrigins
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.util.DynamicBean
import kotlinx.kover.gradle.plugin.util.bean
import org.gradle.api.*

internal class ProvidedVariantsLocator(
    private val project: Project,
    private val callback: (sources: AllVariantOrigins) -> Unit
) {
    private val reasons: MutableSet<String> = mutableSetOf()
    private val androidVariants: MutableList<AndroidVariantInfo> = mutableListOf()
    private var finalized: Boolean = false

    init {
        simpleEnqueue("no-plugin")

        project.pluginManager.withPlugin(KOTLIN_JVM_PLUGIN_ID) {
            simpleEnqueue(KOTLIN_JVM_PLUGIN_ID)
        }
        project.pluginManager.withPlugin(KOTLIN_MULTIPLATFORM_PLUGIN_ID) {
            simpleEnqueue(KOTLIN_MULTIPLATFORM_PLUGIN_ID)
        }
        project.pluginManager.withPlugin(KOTLIN_ANDROID_PLUGIN_ID) {
            simpleEnqueue(KOTLIN_ANDROID_PLUGIN_ID)
        }
        project.pluginManager.withPlugin(ANDROID_APP_PLUGIN_ID) {
            androidEnqueue(ANDROID_APP_PLUGIN_ID)
        }
        project.pluginManager.withPlugin(ANDROID_LIB_PLUGIN_ID) {
            androidEnqueue(ANDROID_LIB_PLUGIN_ID)
        }
        project.pluginManager.withPlugin(ANDROID_DYNAMIC_PLUGIN_ID) {
            androidEnqueue(ANDROID_DYNAMIC_PLUGIN_ID)
        }
    }

    private fun simpleEnqueue(reason: String) {
        enqueue(reason)
        scheduleDequeue(reason)
    }

    private fun androidEnqueue(reason: String) {
        enqueue(reason)

        val androidComponents = project.extensions.findByName("androidComponents")?.bean()
            ?: throw KoverCriticalException("Kover requires extension with name 'androidComponents' for project '${project.path}'. The minimum supported AGP version is 7.0.0")

        val selector = androidComponents("selector")?.bean()?.invoke("all") ?:
            throw KoverCriticalException("Return value for selector().all() is null for project '${project.path}'")

        /*
         * Here's a little trick:
         * when we process a variant, we don't know if it's the last one and whether these variants are created in one action or different ones.
         * Therefore, we are postponing the finalizing using `afterEvaluate`.
         * Since we have a queue, we fill this queue with each variant, so we can only finalize our configuration in the afterEvaluate created for the most recent variant.
         *
         * Important: In the current implementation of AGP, all `onVariants` are called one after the other in the same thread, however, it's an implementation details and we cannot rely on this.
         */
        val dequeueAction = Action<Any> {
            val variant = bean().convertVariant()

            androidVariants += variant

            val variantReason = "android variant: " + variant.name
            enqueue(variantReason)
            scheduleDequeue(variantReason)
        }

        androidComponents("onVariants",  selector, dequeueAction)

        scheduleDequeue(reason)
    }

    private fun enqueue(reason: String) {
        if (finalized) {
            throw KoverCriticalException("Attempt to queue after finalizing.")
        }
        reasons += reason
    }

    private fun scheduleDequeue(reason: String) {
        project.afterEvaluate {
            if (reasons.isEmpty()) {
                throw KoverCriticalException("Error when searching for finalizing configuration variant.")
            }

            reasons.remove(reason)
            if (reasons.isEmpty()) {
                finalize()
            }
        }
    }

    private fun finalize() {
        finalized = true

        val variants = if (project.pluginManager.hasPlugin(KOTLIN_JVM_PLUGIN_ID)) {
            project.locateKotlinJvmVariants()
        } else if (project.pluginManager.hasPlugin(KOTLIN_ANDROID_PLUGIN_ID) || project.hasAndroid9WithKotlin()) {
            project.locateKotlinAndroidVariants(androidVariants)
        } else if (project.pluginManager.hasPlugin(KOTLIN_MULTIPLATFORM_PLUGIN_ID)) {
            project.locateKotlinMultiplatformVariants()
        } else {
            AllVariantOrigins(emptyList(), emptyList())
        }

        callback(variants)
    }

}

internal fun Project.getKotlinExtension(): DynamicBean {
    return extensions.findByName("kotlin")?.bean()
        ?: throw KoverCriticalException("Kover requires extension with name 'kotlin' for project '${project.path}' since it is recognized as Kotlin/Multiplatform project")
}

