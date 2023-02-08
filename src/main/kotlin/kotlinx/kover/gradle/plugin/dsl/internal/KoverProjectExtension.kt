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

    override var isDisabled: Boolean = false

    internal var toolVariant: CoverageToolVariant? = null

    override fun useKoverToolDefault() {
        toolVariant = KoverToolDefaultVariant
    }

    override fun useJacocoToolDefault() {
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
    override fun taskName(vararg name: String) {
        tasksNames.addAll(name)
    }

    override fun taskName(names: Iterable<String>) {
        tasksNames.addAll(names)
    }

    override fun kmpTargetName(vararg name: String) {
        kmpTargetNames.addAll(name)
    }

    internal val tasksNames: MutableSet<String> = mutableSetOf()

    internal val kmpTargetNames: MutableSet<String> = mutableSetOf()
}

internal open class KoverSourcesExclusionsImpl : KoverSourcesExclusions {
    override var excludeJavaCode: Boolean = false

    override fun jvmSourceSetName(vararg name: String) {
        jvmSourceSets += name
    }

    override fun jvmSourceSetName(names: Iterable<String>) {
        // TODO add check of Kotlin plugin type in afterEvaluate!
        jvmSourceSets += names
    }

    override fun kmpTargetName(vararg name: String) {
        TODO("Not implemented")
    }

    override fun kmpCompilation(targetName: String, compilationName: String) {
        kmpCompilationsByTarget.getOrPut(targetName) { mutableSetOf() } += compilationName
    }

    override fun kmpCompilation(compilationName: String) {
        kmpCompilationsForAllTargets += compilationName
    }

    internal val jvmSourceSets: MutableSet<String> = mutableSetOf()

    internal val kmpCompilationsForAllTargets: MutableSet<String> = mutableSetOf()

    internal val kmpCompilationsByTarget: MutableMap<String, MutableSet<String>> = mutableMapOf()
}

internal open class KoverInstrumentationExclusionsImpl : KoverInstrumentationExclusions {
    override fun className(vararg className: String) {
        classes += className
    }

    override fun className(classNames: Iterable<String>) {
        classes += classNames
    }

    override fun packageName(vararg className: String) {
        className.forEach {
            classes += "$it.*"
        }
    }

    override fun packageName(classNames: Iterable<String>) {
        classNames.forEach {
            classes += "$it.*"
        }
    }

    internal val classes: MutableSet<String> = mutableSetOf()
}
