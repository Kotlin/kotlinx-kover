/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.internal

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.*
import org.gradle.api.model.*
import org.gradle.kotlin.dsl.*
import javax.inject.*


internal open class KoverProjectExtensionImpl @Inject constructor(
    objects: ObjectFactory,
    kotlinPlugin: AppliedKotlinPlugin
) : KoverProjectExtension {

    override var isDisabled: Boolean = false

    internal var toolVariant: CoverageToolVariant = KoverToolDefault

    override fun useKoverToolDefault() {
        toolVariant = KoverToolDefault
    }

    override fun useJacocoToolDefault() {
        toolVariant = JacocoToolDefault
    }

    override fun useKoverTool(version: String) {
        toolVariant = KoverTool(version)
    }

    override fun useJacocoTool(version: String) {
        toolVariant = JacocoTool(version)
    }

    override fun excludeTests(config: Action<KoverTestsExclusions>) {
        config(tests)
    }

    override fun excludeSources(config: Action<KoverSourcesExclusions>) {
        config(sources)
    }

    override fun excludeInstrumentation(config: Action<KoverInstrumentationExclusions>) {
        config(instrumentation)
    }

    internal val tests: KoverTestsExclusionsImpl = objects.newInstance(kotlinPlugin)
    internal val sources: KoverSourcesExclusionsImpl = objects.newInstance(kotlinPlugin)
    internal val instrumentation: KoverInstrumentationExclusionsImpl = objects.newInstance()
}

internal open class KoverTestsExclusionsImpl @Inject constructor(
    private val kotlinPlugin: AppliedKotlinPlugin
) : KoverTestsExclusions {
    override fun taskName(vararg name: String) {
        kotlinPlugin.type ?: KoverIllegalConfigException("TODO message")
        tasksNames.addAll(name)
    }

    override fun taskName(names: Iterable<String>) {
        kotlinPlugin.type ?: KoverIllegalConfigException("TODO message")
        tasksNames.addAll(names)
    }

    override fun kmpTargetName(vararg name: String) {
        if (kotlinPlugin.type != KotlinPluginType.MULTI_PLATFORM) {
            throw KoverIllegalConfigException("TODO message")
        }
        kmpTargetNames.addAll(name)
    }

    internal val tasksNames: MutableSet<String> = mutableSetOf()

    internal val kmpTargetNames: MutableSet<String> = mutableSetOf()
}

internal open class KoverSourcesExclusionsImpl @Inject constructor(
    private val kotlinPlugin: AppliedKotlinPlugin
): KoverSourcesExclusions {
    override var excludeJavaCode: Boolean = false

    override fun jvmSourceSetName(vararg name: String) {
        if (kotlinPlugin.type != KotlinPluginType.JVM) {
            throw KoverIllegalConfigException("TODO message")
        }

        jvmSourceSets += name
    }

    override fun jvmSourceSetName(names: Iterable<String>) {
        if (kotlinPlugin.type != KotlinPluginType.JVM) {
            throw KoverIllegalConfigException("TODO message")
        }

        jvmSourceSets += names
    }

    override fun kmpTargetName(vararg name: String) {
        if (kotlinPlugin.type != KotlinPluginType.MULTI_PLATFORM) {
            throw KoverIllegalConfigException("TODO message")
        }

    }

    override fun kmpCompilation(targetName: String, compilationName: String) {
        if (kotlinPlugin.type != KotlinPluginType.MULTI_PLATFORM) {
            throw KoverIllegalConfigException("TODO message")
        }

        kmpCompilationsByTarget.getOrPut(targetName) { mutableSetOf() } += compilationName
    }

    override fun kmpCompilation(compilationName: String) {
        if (kotlinPlugin.type != KotlinPluginType.MULTI_PLATFORM) {
            throw KoverIllegalConfigException("TODO message")
        }

        kmpCompilationsForAllTargets += compilationName
    }

    internal val jvmSourceSets: MutableSet<String> = mutableSetOf()

    internal val kmpCompilationsForAllTargets: MutableSet<String> = mutableSetOf()

    internal val kmpCompilationsByTarget: MutableMap<String, MutableSet<String>> = mutableMapOf()
}

internal open class KoverInstrumentationExclusionsImpl: KoverInstrumentationExclusions {
    override fun className(vararg name: String) {
        classes += name
    }

    override fun className(names: Iterable<String>) {
        classes += names
    }

    override fun packageName(vararg name: String) {
        name.forEach {
            classes += "$it.*"
        }
    }

    override fun packageName(names: Iterable<String>) {
        names.forEach {
            classes += "$it.*"
        }
    }

    internal val classes: MutableSet<String> = mutableSetOf()
}
