/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.writer

import org.gradle.api.Action
import java.io.*

internal fun File.writeScript(block: FormattedWriter.() -> Unit) {
    this.printWriter().use {
        FormattedWriter { string ->
            it.print(string)
        }.block()
    }
}

internal class FormattedWriter(private val append: (String) -> Unit) {
    private var indents: Int = 0

    fun assign(receiver: String, expression: String) {
        append(indent(indents))
        append(receiver)
        append(" = ")
        append(expression)
        append("\n")
    }

    fun call(functionName: String, vararg args: String) {
        append(indent(indents))
        append(functionName)
        append("(")
        args.forEachIndexed { index, arg ->
            if (index > 0) {
                append(", ")
            }
            append(arg)
        }
        append(")\n")
    }

    fun call(functionName: String, config: FormattedWriter.() -> Unit) {
        append(indent(indents))
        append(functionName)
        append("{\n")
        indents++
        config(this)
        indents--
        line("}")
    }

    fun callStr(functionName: String, stringArgs: Iterable<String>) {
        append(indent(indents))
        append(functionName)
        append("(")
        stringArgs.forEachIndexed { index, arg ->
            if (index > 0) {
                append(", ")
            }
            append("\"")
            append(arg)
            append("\"")
        }
        append(")\n")
    }

    inline fun <T: Any> call(name: String, config: Action<T>, writer: (FormattedWriter) -> T) {
        line("$name {")
        indents++
        config.execute(writer(this))
        indents--
        line("}")
    }

    inline fun <T: Any> callStr(name: String, stringArgs: Iterable<String>, config: Action<T>, writer: (FormattedWriter) -> T) {
        val args = stringArgs.joinToString(", ") { "\"$it\"" }
        line("$name($args) {")
        indents++
        config.execute(writer(this))
        indents--
        line("}")
    }

    fun line(line: String) {
        append(indent(indents))
        append(line)
        append("\n")
    }

    fun text(raw: String) {
        append(raw)
        append("\n")
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
