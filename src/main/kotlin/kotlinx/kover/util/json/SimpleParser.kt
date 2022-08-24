/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.util.json

import java.io.*
import java.math.BigDecimal

fun File.readJsonObject(): Map<String, Any> {
    return FileJsonReader(this).use {
        it.readObject()!!
    }
}

fun File.readJsonArray(): List<Any> {
    return FileJsonReader(this).use {
        it.readArray()!!
    }
}

internal class FileJsonReader(file: File) : JsonReader() {
    private val buffered = file.bufferedReader()
    private var char: Char? = buffered.read().let { if (it == -1) null else it.toChar() }
    private var pos: Int = 0

    override fun next() {
        char = buffered.read().let { if (it == -1) null else it.toChar() }
        pos++
    }

    override fun eof(): Boolean = char == null

    override fun pos(): Int = pos

    override fun char(): Char = char ?: throw Exception("Can't get current char: JSON reader at the end-of file")

    override fun close() = buffered.close()
}

/**
 * Simple lightweight JSON parser.
 * It does not support all the features of the JSON format, because its purpose is very limited.
 *
 * Type parsing rules:
 *  - JSON object converts to `Map<String, Any>`
 *  - JSON array converts to `List<Any>`
 *  - JSON string literal converts to `String` (escapes are not supported)
 *  - JSON numeric literal converts to `BigDecimal`
 *  - JSON boolean literal not supported
 *  - JSON null literal not supported
 */
internal abstract class JsonReader : Closeable {

    fun readObject(): Map<String, Any>? {
        skipSpaces()
        if (!skipObjectOpen()) {
            return null
        }

        var isFirst = true
        val result = mutableMapOf<String, Any>()
        while (!skipObjectClose()) {
            if (eof()) {
                throw Exception("Unexpected end of file: object is not closed")
            }
            if (!isFirst) {
                if (!skipComma()) {
                    throw Exception("Expected comma between object fields at pos ${pos()}")
                }
            }
            isFirst = false

            val fieldName = readStringLiteral() ?: throw Exception("Can't read name of field at pos ${pos()}")
            if (!skipValueDelimiter()) {
                throw Exception("Expected ':' or '=' char between field name and value inside the object at pos ${pos()}")
            }
            val fieldValue = nextValue() ?: throw Exception("Can't read value of the field at pos ${pos()}")
            result[fieldName] = fieldValue
        }

        return result
    }

    fun readArray(): List<Any>? {
        skipSpaces()
        if (!skipArrayOpen()) {
            return null
        }

        val result = mutableListOf<Any>()
        var isFirst = true
        while (!skipArrayClose()) {
            if (eof()) {
                throw Exception("Unexpected end of file: array is not closed")
            }

            if (!isFirst) {
                if (!skipComma()) {
                    throw Exception("Expected comma between array values at pos ${pos()}")
                }
            }
            isFirst = false

            result += nextValue() ?: throw Exception("Can't read value of in the array at pos ${pos()}")
        }
        return result
    }


    private fun nextValue(): Any? {
        readStringLiteral()?.let { return it }
        readObject()?.let { return it }
        readArray()?.let { return it }
        readNumber()?.let { return it }
        return null
    }

    private fun readStringLiteral(): String? {
        skipSpaces()
        if (char() != '"') {
            return null
        }

        var buffer = CharArray(16)
        var size = 0
        next()

        while (char() != '"') {
            if (size == buffer.size) {
                buffer = buffer.copyOf(size * 2)
            }
            buffer[size++] = char()

            next()
            if (eof()) {
                throw Exception("Unexpected end of file: string literal is not terminated")
            }
        }
        next()

        return buffer.concatToString(0, size)
    }

    private fun readNumber(): BigDecimal? {
        skipSpaces()

        if (!char().isDigit() && char() != '-') {
            return null
        }

        var hasDot = false
        var buffer = CharArray(8)
        var size = 0
        buffer[size++] = char()
        next()

        while (!eof()) {
            val char = char()
            if (char.isWhitespace() || char == ',' || char == '}' || char == ']') {
                break
            }

            if (size == buffer.size) {
                buffer = buffer.copyOf(size * 2)
            }

            if (char.isDigit()) {
                buffer[size++] = char
                next()
            } else if (char == '.') {
                if (hasDot) {
                    throw Exception("")
                }
                buffer[size++] = char
                next()
                hasDot = true
            } else {
                throw Exception("")
            }
        }

        return buffer.concatToString(0, size).toBigDecimal()
    }


    private fun skipComma(): Boolean {
        skipSpaces()
        return when (char()) {
            ',' -> {
                next()
                true
            }
            else -> false
        }
    }

    private fun skipValueDelimiter(): Boolean {
        skipSpaces()
        return when (char()) {
            ':' -> {
                next()
                true
            }
            '=' -> {
                next()
                true
            }
            else -> false
        }
    }

    private fun skipArrayOpen(): Boolean {
        skipSpaces()
        if (char() == '[') {
            next()
            return true
        }
        return false
    }

    private fun skipArrayClose(): Boolean {
        skipSpaces()
        if (char() == ']') {
            next()
            return true
        }
        return false
    }


    private fun skipObjectOpen(): Boolean {
        skipSpaces()
        if (char() == '{') {
            next()
            return true
        }
        return false
    }

    private fun skipObjectClose(): Boolean {
        skipSpaces()
        if (char() == '}') {
            next()
            return true
        }
        return false
    }

    private fun skipSpaces(): Boolean {
        var hasSpaces = false
        while (!eof()) {
            if (!char().isWhitespace()) {
                break
            }
            next()
            hasSpaces = true
        }
        return hasSpaces
    }

    protected abstract fun pos(): Int

    protected abstract fun eof(): Boolean

    protected abstract fun next()

    protected abstract fun char(): Char
}
