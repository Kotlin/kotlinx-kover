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
    engineForced: CoverageEngineVendor?,
    block: ScriptAppender.() -> Unit
) {
    this.printWriter().use {
        object : ScriptAppender(language, type, engineForced, 0) {
            override fun doWrite(text: String) = it.print(text)
        }.block()
    }
}

internal abstract class ScriptAppender(
    val language: ScriptLanguage,
    val type: KotlinPluginType,
    val engineForced: CoverageEngineVendor?,
    private val indents: Int
) {
    abstract fun doWrite(text: String)

    fun line(line: String) {
        doWrite(indent(indents))
        doWrite(line)
        doWrite("\n")
    }

    inline fun lineIf(shouldWrite: Boolean, producer: () -> String) {
        if (shouldWrite) line(producer())
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun lineIf(shouldWrite: Boolean, line: String) {
        if (shouldWrite) line(line)
    }

    inline fun block(name: String, shouldWrite: Boolean = true, content: ScriptAppender.() -> Unit) {
        if (!shouldWrite) return
        line("$name {")
        stepInto().content()
        line("}")
    }

    inline fun <T> blockForEach(values: Iterable<T>, prefixLine: String,  shouldWrite: Boolean = true, content: ScriptAppender.(T) -> Unit) {
        values.forEach { value -> block(prefixLine, shouldWrite) { content(value) } }
    }


    private fun stepInto(): ScriptAppender {
        return object : ScriptAppender(language, type, engineForced, indents + 1) {
            override fun doWrite(text: String) {
                this@ScriptAppender.doWrite(text)
            }
        }
    }

    private fun indent(count: Int): String {
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
