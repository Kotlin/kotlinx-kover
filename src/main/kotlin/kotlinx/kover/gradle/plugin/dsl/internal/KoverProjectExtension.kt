/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.internal

import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.tools.CoverageToolVariant
import kotlinx.kover.gradle.plugin.tools.JacocoToolDefaultVariant
import kotlinx.kover.gradle.plugin.tools.JacocoToolVariant
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject


internal open class KoverProjectExtensionImpl @Inject constructor(objects: ObjectFactory) : KoverProjectExtension {

    internal var disabled: Boolean = false

    internal var excludeJava: Boolean = false

    internal var toolVariant: CoverageToolVariant? = null
    override fun disable() {
        disabled = true
    }

    override fun useJacoco() {
        toolVariant = JacocoToolDefaultVariant
    }

    override fun useJacoco(version: String) {
        toolVariant = JacocoToolVariant(version)
    }

    override fun excludeJavaCode() {
        excludeJava = true
    }

    override fun excludeTests(config: Action<KoverTestsExclusions>) {
        config(tests)
    }

    internal fun excludeCompilations(config: Action<KoverCompilationsExclusions>) {
        config(compilations)
    }

    override fun excludeInstrumentation(config: Action<KoverInstrumentationExclusions>) {
        config(instrumentation)
    }

    internal val tests: KoverTestsExclusionsImpl = objects.newInstance()
    internal val compilations: KoverCompilationsExclusionsImpl = objects.newInstance()
    internal val instrumentation: KoverInstrumentationExclusionsImpl = objects.newInstance()
}

internal open class KoverTestsExclusionsImpl : KoverTestsExclusions {
    override fun tasks(vararg name: String) {
        tasksNames.addAll(name)
    }

    override fun tasks(names: Iterable<String>) {
        tasksNames.addAll(names)
    }

    override fun mppTargetName(vararg name: String) {
        mppTargetNames.addAll(name)
    }

    internal val tasksNames: MutableSet<String> = mutableSetOf()

    internal val mppTargetNames: MutableSet<String> = mutableSetOf()
}

internal open class KoverCompilationsExclusionsImpl
@Inject constructor(private val objects: ObjectFactory) : KoverCompilationsExclusions {

    override fun jvm(config: Action<KoverJvmSourceSet>) {
        config(jvm)
    }

    override fun mpp(config: Action<KoverMppSourceSet>) {
        config(mpp)
    }

    internal val jvm: KoverJvmSourceSetImpl = objects.newInstance()
    internal val mpp: KoverMppSourceSetImpl = objects.newInstance()
}

internal open class KoverJvmSourceSetImpl : KoverJvmSourceSet {
    internal val sourceSets: MutableSet<String> = mutableSetOf()
    override fun sourceSetName(vararg name: String) {
        sourceSets += name
    }

    override fun sourceSetName(names: Iterable<String>) {
        sourceSets += names
    }

}

internal open class KoverMppSourceSetImpl : KoverMppSourceSet {
    internal var configured = false
    internal val compilationsForAllTargets: MutableSet<String> = mutableSetOf()
    internal val allCompilationsInTarget: MutableSet<String> = mutableSetOf()
    internal val compilationsByTarget: MutableMap<String, MutableSet<String>> = mutableMapOf()

    override fun targetName(vararg name: String) {
        configured = true
        allCompilationsInTarget += name
    }

    override fun compilation(targetName: String, compilationName: String) {
        configured = true
        compilationsByTarget.getOrPut(targetName) { mutableSetOf() } += compilationName
    }

    override fun compilation(compilationName: String) {
        configured = true
        compilationsForAllTargets += compilationName
    }

}

internal open class KoverInstrumentationExclusionsImpl : KoverInstrumentationExclusions {
    override fun classes(vararg names: String) {
        classes += names
    }

    override fun classes(names: Iterable<String>) {
        classes += names
    }

    override fun packages(vararg names: String) {
        names.forEach {
            classes += "$it.*"
        }
    }

    override fun packages(names: Iterable<String>) {
        names.forEach {
            classes += "$it.*"
        }
    }

    internal val classes: MutableSet<String> = mutableSetOf()
}
