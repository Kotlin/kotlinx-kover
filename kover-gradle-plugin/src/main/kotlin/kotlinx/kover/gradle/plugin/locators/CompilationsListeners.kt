/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import kotlinx.kover.gradle.plugin.util.DynamicBean
import kotlinx.kover.gradle.plugin.util.bean
import org.gradle.api.*

/**
 * Listener for compilation kits detection events.
 */
internal interface CompilationsListener {
    /**
     * Function called when a JVM target is detected in Kotlin/JVM or Kotlin/MPP project.
     */
    fun onJvmCompilation(kit: JvmCompilationKit)

    /**
     * Function called when an Android build variants are detected in Kotlin/Android or Kotlin/MPP project.
     */
    fun onAndroidCompilations(kits: List<AndroidVariantCompilationKit>)

    /**
     * Function called when the compilations search is completed.
     */
    fun onFinalize()
}

/**
 * A factory that creates a locator suitable for a specific project.
 */
internal object CompilationsListenerManager {

    /**
     * Asynchronously detect Kotlin compilations in the current project.
     */
    fun locate(project: Project, koverExtension: KoverProjectExtensionImpl, listener: CompilationsListener) {
        val wrapper = CompilationsListenerWrapper(listener)

        val context = LocatorContext(project, koverExtension, wrapper)
        context.initNoKotlinPluginLocator()

        project.pluginManager.withPlugin("kotlin") {
            context.initKotlinJvmPluginLocator()
        }
        project.pluginManager.withPlugin("kotlin-multiplatform") {
            context.initKotlinMultiplatformPluginLocator()
        }
        project.pluginManager.withPlugin("kotlin-android") {
            context.initKotlinAndroidPluginLocator()
        }
    }
}

internal class LocatorContext(
    val project: Project,
    val koverExtension: KoverProjectExtensionImpl,
    val listener: CompilationsListenerWrapper
)

/**
 * Locate information about Kotlin project's compilations.
 * This information is necessary for carrying out instrumentation and generating Kover reports.
 *
 * The locator is engaged in reading the settings of the applied plugins.
 */
internal class CompilationsListenerWrapper(private val listener: CompilationsListener) {
    @Volatile
    private var pluginType: KotlinPluginType? = null

    @Volatile
    private var finalized: Boolean = false

    fun onApplyPlugin(type: KotlinPluginType) {
        if (pluginType != null) {
            throw KoverIllegalConfigException(
                "Kover can't work in a project where several different Kotlin plugins are applied.\n" +
                        "Detected plugins ${pluginType?.name} and ${type.name}, remove one of them"
            )
        }
        pluginType = type
    }

    fun jvm(compilation: JvmCompilationKit) {
        checkNotFinalized()

        listener.onJvmCompilation(compilation)
    }

    fun android(compilations: List<AndroidVariantCompilationKit>) {
        checkNotFinalized()

        listener.onAndroidCompilations(compilations)
    }

    fun finalizeIfNoKotlinPlugin() {
        if (pluginType == null) {
            // if no plugin has been applied, then calls the finalization to process dependencies
            finalize()
        }
    }

    fun finalize() {
        checkNotFinalized()
        finalized = true

        listener.onFinalize()
    }

    private fun checkNotFinalized() {
        if (finalized) {
            throw KoverCriticalException("Listener is finalized")
        }
    }
}

internal fun Project.getKotlinExtension(): DynamicBean {
    return extensions.findByName("kotlin")?.bean()
        ?: throw KoverCriticalException("Kover requires extension with name 'kotlin' for project '${project.path}' since it is recognized as Kotlin/Multiplatform project")
}

internal val Project.hasAnyAndroidPlugin: Boolean
    get() =  pluginManager.hasPlugin(ANDROID_APP_PLUGIN_ID) || pluginManager.hasPlugin(ANDROID_LIB_PLUGIN_ID)

internal const val ANDROID_APP_PLUGIN_ID = "com.android.application"

internal const val ANDROID_LIB_PLUGIN_ID = "com.android.library"

internal const val ANDROID_DYNAMIC_PLUGIN_ID = "com.android.dynamic-feature"
