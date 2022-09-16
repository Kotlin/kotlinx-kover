/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.writer

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.framework.common.*
import java.io.*

internal fun File.writeScript(
    language: ScriptLanguage,
    type: KotlinPluginType,
    overriddenEngine: CoverageEngineVendor?,
    block: FormattedScriptAppender.() -> Unit
) {
    this.printWriter().use {
        FormattedScriptAppender(language, type, overriddenEngine) { string ->
           it.print(string)
        }.block()
    }
}

internal class FormattedScriptAppender(
    val language: ScriptLanguage,
    val type: KotlinPluginType,
    val overriddenEngine: CoverageEngineVendor?,
    val appender: (String) -> Unit
) {
    private var indents: Int = 0

    fun line(line: String) {
        appender(indent(indents))
        appender(line)
        appender("\n")
    }

    inline fun lineIf(shouldWrite: Boolean, producer: () -> String) {
        if (shouldWrite) line(producer())
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun lineIf(shouldWrite: Boolean, line: String) {
        if (shouldWrite) line(line)
    }

    inline fun block(name: String, shouldWrite: Boolean = true, content: FormattedScriptAppender.() -> Unit) {
        if (!shouldWrite) return
        line("$name {")
        indents++
        content()
        indents--
        line("}")
    }

    inline fun <T> blockForEach(values: Iterable<T>, prefixLine: String,  shouldWrite: Boolean = true, content: FormattedScriptAppender.(T) -> Unit) {
        values.forEach { value -> block(prefixLine, shouldWrite) { content(value) } }
    }

    private fun indent(count: Int): String {
        // In most cases, the nesting depth is no more than 5, so for optimization we use literals for frequently used indents
        return when (count) {
            0 -> ""
            1 -> "    "
            2 -> "        "
            3 -> "            "
            4 -> "                "
            5 -> "                    "
            else -> "    ".repeat(count)
        }
    }
}
