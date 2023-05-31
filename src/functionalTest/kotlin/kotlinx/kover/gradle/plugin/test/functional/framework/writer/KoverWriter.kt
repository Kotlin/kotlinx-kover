/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.writer

import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.*

internal class KoverWriter(private val writer: FormattedWriter) : KoverProjectExtension {

    override fun disable() {
        writer.call("disable")
    }

    override fun useJacoco() {
        writer.call("useJacoco")
    }


    override fun useJacoco(version: String) {
        writer.call("useJacoco", version)
    }

    override fun excludeJavaCode() {
        writer.call("excludeJavaCode")
    }

    override fun excludeTests(config: Action<KoverTestsExclusions>) {
        writer.call("excludeTests", config) { KoverTestsExclusionsWriter(it) }
    }

    override fun excludeSourceSets(config: Action<SourceSetsExclusions>) {
        writer.call("excludeCompilations", config) { SourceSetsExclusionsWriter(it) }
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
}

private class SourceSetsExclusionsWriter(private val writer: FormattedWriter) : SourceSetsExclusions {

    override fun names(vararg name: String) {
        names(name.toList())
    }

    override fun names(names: Iterable<String>) {
        writer.callStr("names", names)
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
