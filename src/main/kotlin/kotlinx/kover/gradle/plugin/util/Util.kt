/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.util

import java.io.File

/**
 * Executes `block` of code only if boolean value is `true`.
 */
internal inline fun <T : Any> Boolean.ifTrue(block: () -> T): T? {
    return if (this) {
        block()
    } else {
        null
    }
}

/**
 * Executes `block` of code only if boolean value is `false`.
 */
internal inline fun <T : Any> Boolean.ifFalse(block: () -> T): T? {
    return if (!this) {
        block()
    } else {
        null
    }
}

/**
 * Replaces characters `.` to `|` or `\` and added `.class` as postfix and `.* /` or `.*\` as prefix.
 */
internal fun String.wildcardsToClassFileRegex(): String {
    val filenameWithWildcards = "*" + File.separatorChar + this.replace('.', File.separatorChar) + ".class"
    return filenameWithWildcards.wildcardsToRegex()
}

/**
 * Replaces characters `*` or `.` to `.*` and `.` regexp characters.
 */
internal fun String.wildcardsToRegex(): String {
    // in most cases, the characters `*` or `.` will be present therefore, we increase the capacity in advance
    val builder = StringBuilder(length * 2)

    forEach { char ->
        when (char) {
            in regexMetacharactersSet -> builder.append('\\').append(char)
            '*' -> builder.append('.').append("*")
            '?' -> builder.append('.')
            else -> builder.append(char)
        }
    }

    return builder.toString()
}

internal val ONE_HUNDRED = 100.toBigDecimal()

private val regexMetacharactersSet = "<([{\\^-=$!|]})+.>".toSet()

internal fun File.subdirs(): List<File> {
    return listFiles { it ->
        it.exists() && it.isDirectory
    }?.toList() ?: emptyList()
}

