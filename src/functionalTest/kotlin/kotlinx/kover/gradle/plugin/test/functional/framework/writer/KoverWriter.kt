/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.writer

import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.*

internal class KoverWriter(private val writer: FormattedWriter) : KoverProjectExtension {

    override var disabledForProject: Boolean = false
        set(value) {
            writer.assign("disabledForProject", value.toString())
            field = value
        }

    override fun useKoverTool() {
        writer.call("useKoverTool")
    }

    override fun useJacocoTool() {
        writer.call("useJacocoTool")
    }

    override fun useKoverTool(version: String) {
        writer.call("useKoverTool")
    }

    override fun useJacocoTool(version: String) {
        writer.call("useJacocoTool")
    }

    override fun excludeTests(config: Action<KoverTestsExclusions>) {
        writer.call("excludeTests", config) { KoverTestsExclusionsWriter(it) }
    }

    override fun excludeSources(config: Action<KoverSourcesExclusions>) {
        writer.call("excludeSources", config) { KoverSourcesExclusionsWriter(it) }
    }

    override fun excludeInstrumentation(config: Action<KoverInstrumentationExclusions>) {
        writer.call("excludeInstrumentation", config) { KoverInstrumentationExclusionsWriter(it) }
    }

}

private class KoverTestsExclusionsWriter(private val writer: FormattedWriter) : KoverTestsExclusions {
    override fun tasks(vararg name: String) {
        tasks(name.asIterable())
    }

    override fun tasks(names: Iterable<String>) {
        writer.callStr("tasks", names)
    }

    override fun mppTargetName(vararg name: String) {
        writer.callStr("mppTargetName", name.asIterable())
    }
}

private class KoverSourcesExclusionsWriter(private val writer: FormattedWriter) : KoverSourcesExclusions {
    override var excludeJavaCode: Boolean = false
        set(value) {
            writer.assign("excludeJavaCode", value.toString())
            field = value
        }

    override fun jvm(config: Action<KoverJvmSourceSet>) {
        writer.call("jvm", config) { KoverJvmSourceSetWriter(it) }
    }

    override fun mpp(config: Action<KoverMppSourceSet>) {
        writer.call("mpp", config) { KoverMppSourceSetWriter(it) }
    }

}

private class KoverJvmSourceSetWriter(private val writer: FormattedWriter): KoverJvmSourceSet {
    override fun sourceSetName(vararg name: String) {
        sourceSetName(name.asIterable())
    }

    override fun sourceSetName(names: Iterable<String>) {
        writer.callStr("sourceSetName", names)
    }

}

private class KoverMppSourceSetWriter(private val writer: FormattedWriter): KoverMppSourceSet {
    override fun targetName(vararg name: String) {
        writer.callStr("targetName", name.asIterable())
    }

    override fun compilation(targetName: String, compilationName: String) {
        writer.callStr("compilation", listOf(targetName, compilationName))
    }

    override fun compilation(compilationName: String) {
        writer.callStr("compilation", listOf(compilationName))
    }

}

private class KoverInstrumentationExclusionsWriter(private val writer: FormattedWriter) :
    KoverInstrumentationExclusions {
    override fun classes(vararg names: String) {
        this.classes(names.asIterable())
    }

    override fun classes(names: Iterable<String>) {
        writer.callStr("classes", names)
    }

    override fun packages(vararg names: String) {
        packages(names.asIterable())
    }

    override fun packages(names: Iterable<String>) {
        writer.callStr("packages", names)
    }

}
