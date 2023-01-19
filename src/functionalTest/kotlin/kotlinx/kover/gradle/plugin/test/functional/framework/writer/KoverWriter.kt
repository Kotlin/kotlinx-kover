/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.writer

import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.*

internal class KoverWriter(private val writer: FormattedWriter) : KoverProjectExtension {

    override var isDisabled: Boolean = false
        set(value) {
            writer.assign("isDisabled", value.toString())
            field = value
        }

    override fun useKoverToolDefault() {
        writer.call("useKoverToolDefault")
    }

    override fun useJacocoToolDefault() {
        writer.call("useJacocoToolDefault")
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
    override fun taskName(vararg name: String) {
        taskName(name.asIterable())
    }

    override fun taskName(names: Iterable<String>) {
        writer.callStr("taskName", names)
    }

    override fun kmpTargetName(vararg name: String) {
        writer.callStr("kmpTargetName", name.asIterable())
    }
}

private class KoverSourcesExclusionsWriter(private val writer: FormattedWriter) : KoverSourcesExclusions {
    override var excludeJavaCode: Boolean = false
        set(value) {
            writer.assign("excludeJavaCode", value.toString())
            field = value
        }

    override fun jvmSourceSetName(vararg name: String) {
        jvmSourceSetName(name.asIterable())
    }

    override fun jvmSourceSetName(names: Iterable<String>) {
        writer.callStr("jvmSourceSetName", names)
    }

    override fun kmpTargetName(vararg name: String) {
        writer.callStr("kmpTargetName", name.asIterable())
    }

    override fun kmpCompilation(targetName: String, compilationName: String) {
        writer.callStr("kmpCompilation", listOf(targetName, compilationName))
    }

    override fun kmpCompilation(compilationName: String) {
        writer.callStr("kmpCompilation", listOf(compilationName))
    }

}

private class KoverInstrumentationExclusionsWriter(private val writer: FormattedWriter) :
    KoverInstrumentationExclusions {
    override fun className(vararg className: String) {
        this.className(className.asIterable())
    }

    override fun className(classNames: Iterable<String>) {
        writer.callStr("className", classNames)
    }

    override fun packageName(vararg className: String) {
        packageName(className.asIterable())
    }

    override fun packageName(classNames: Iterable<String>) {
        writer.callStr("packageName", classNames)
    }

}
