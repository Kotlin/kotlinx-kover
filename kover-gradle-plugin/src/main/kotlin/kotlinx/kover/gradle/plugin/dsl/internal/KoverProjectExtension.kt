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

    override fun excludeSourceSets(config: Action<SourceSetsExclusions>) {
        config(sourceSets)
    }

    override fun excludeInstrumentation(config: Action<KoverInstrumentationExclusions>) {
        config(instrumentation)
    }

    internal val tests: KoverTestsExclusionsImpl = objects.newInstance()
    internal val sourceSets: SourceSetsExclusionsImpl = objects.newInstance()
    internal val instrumentation: KoverInstrumentationExclusionsImpl = objects.newInstance()
}

internal open class KoverTestsExclusionsImpl : KoverTestsExclusions {
    internal val tasksNames: MutableSet<String> = mutableSetOf()

    override fun tasks(vararg name: String) {
        tasksNames.addAll(name)
    }

    override fun tasks(names: Iterable<String>) {
        tasksNames.addAll(names)
    }

}

internal open class SourceSetsExclusionsImpl : SourceSetsExclusions {
    internal val sourceSets: MutableSet<String> = mutableSetOf()

    override fun names(vararg name: String) {
        sourceSets += name
    }

    override fun names(names: Iterable<String>) {
        sourceSets += names
    }
}

internal open class KoverInstrumentationExclusionsImpl : KoverInstrumentationExclusions {
    internal val classes: MutableSet<String> = mutableSetOf()

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
}
