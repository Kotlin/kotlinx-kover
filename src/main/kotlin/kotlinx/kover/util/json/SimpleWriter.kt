/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.util.json

import java.io.*
import java.math.BigDecimal

/*
 * Simple lightweight converter to JSON string.
 * It does not support all the features of the JSON format, because its purpose is very limited.
 *
 * Type converting rules:
 *  - `Map<String, Any>` converts to JSON object
 *  - `Iterable<Any>` converts to JSON array
 *  - `String` converts to JSON string literal (escapes are supported)
 *  - `Int` converts to JSON number literal
 *  - `BigDecimal` converts to JSON string literal
 *  - `File` converts to JSON string literal with canonicalPath (escapes are supported)
 *  - `Boolean` converts to JSON boolean literal
 *  - `null` values not supported
 */

fun File.writeJsonObject(obj: Map<String, Any>) {
    printWriter().use { pw ->
        pw.writeObject(obj, 0)
    }
}

fun PrintWriter.writeObject(obj: Map<String, Any>, spaces: Int) {
    val indent = "  ".repeat(spaces + 1)

    append('{')
    if (obj.size > 1) {
        appendLine()
        append(indent)
    }

    var first = true
    obj.forEach { (name, value) ->
        if (!first) {
            appendLine(',')
            append(indent)
        }
        append('"').append(name).append("\": ")
        writeValue(value, spaces)
        first = false
    }
    if (obj.size > 1) {
        appendLine()
        append("  ".repeat(spaces))
    }
    append('}')
}

fun PrintWriter.writeArray(obj: Iterable<Any>, spaces: Int) {
    val indent = "  ".repeat(spaces + 1)

    append('[')
    if (obj.count() > 1) {
        appendLine()
        append(indent)
    }

    var first = true
    obj.forEach {
        if (!first) {
            appendLine(',')
            append(indent)
        }
        writeValue(it, spaces)
        first = false
    }
    if (obj.count() > 1) {
        appendLine()
        append("  ".repeat(spaces))
    }
    append(']')
}

@Suppress("UNCHECKED_CAST")
private fun PrintWriter.writeValue(value: Any, spaces: Int) {
    when (value) {
        is String -> append(value.jsonEscapes)
        is Int -> append(value.toString())
        is Map<*, *> -> writeObject(value as Map<String, Any>, spaces + 1)
        is Iterable<*> -> writeArray(value as Iterable<Any>, spaces + 1)
        is File -> append(value.canonicalPath.jsonEscapes)
        is BigDecimal -> {
            append('"')
            append(value.toPlainString())
            append('"')
        }
        is Boolean -> append(value.toString())
        else -> {
            throw Exception("Unsupported type ${value.javaClass}")
        }
    }
}

internal val String.jsonEscapes: String
    get() {
        return '"' + replace("\\", "\\\\").replace("\"", "\\\"") + '"'
    }
