/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.internal

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.dsl.KoverVersions.KOVER_TOOL_MINIMAL_VERSION
import kotlinx.kover.gradle.plugin.tools.*
import kotlinx.kover.gradle.plugin.util.SemVer
import org.gradle.api.*
import org.gradle.api.model.*
import org.gradle.kotlin.dsl.*
import javax.inject.*


internal open class KoverProjectExtensionImpl @Inject constructor(objects: ObjectFactory) : KoverProjectExtension {

    override var allTestsExcluded: Boolean = false

    internal var toolVariant: CoverageToolVariant? = null

    override fun useKoverTool() {
        toolVariant = KoverToolDefaultVariant
    }

    override fun useJacocoTool() {
        toolVariant = JacocoToolDefaultVariant
    }

    override fun useKoverTool(version: String) {
        val minimal = SemVer.ofThreePartOrNull(KOVER_TOOL_MINIMAL_VERSION)
            ?: throw KoverCriticalException("Incorrect minimal version of kover tool '$KOVER_TOOL_MINIMAL_VERSION'")

        val custom = SemVer.ofThreePartOrNull(version)
            ?: throw KoverIllegalConfigException("Incorrect version of kover tool '$KOVER_TOOL_MINIMAL_VERSION', expected version in format '1.2.3'")

        if (custom < minimal) {
            throw KoverIllegalConfigException("Specified kover tool version '$version' lower then expected minimal '$KOVER_TOOL_MINIMAL_VERSION'")
        }

        toolVariant = KoverToolVariant(version)
    }

    override fun useJacocoTool(version: String) {
        toolVariant = JacocoToolVariant(version)
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

    internal val tests: KoverTestsExclusionsImpl = objects.newInstance()
    internal val sources: KoverSourcesExclusionsImpl = objects.newInstance()
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

internal open class KoverSourcesExclusionsImpl
@Inject constructor(private val objects: ObjectFactory) : KoverSourcesExclusions {

    override var excludeJavaCode: Boolean = false

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
