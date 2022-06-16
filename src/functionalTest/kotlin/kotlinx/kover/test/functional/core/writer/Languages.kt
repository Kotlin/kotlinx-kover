/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.core.writer

import kotlinx.kover.test.functional.core.GradleScriptLanguage
import kotlinx.kover.test.functional.core.ProjectSlice
import java.io.PrintWriter
import kotlin.reflect.*

internal val ProjectSlice.scriptExtension get() = if (language == GradleScriptLanguage.KOTLIN) "gradle.kts" else "gradle"

internal fun Iterable<String>.formatList(language: GradleScriptLanguage): String {
    val prefix = if (language == GradleScriptLanguage.KOTLIN) "listOf(" else "["
    val postfix = if (language == GradleScriptLanguage.KOTLIN) ")" else "]"

    return prefix + this.joinToString(separator = ",") { "\"$it\"" } + postfix
}

internal fun String.addAllList(list: Iterable<String>, language: GradleScriptLanguage): String {
    val listString = list.formatList(language)
    return this + if (language == GradleScriptLanguage.KOTLIN) " += $listString" else ".addAll($listString)"
}

internal fun KClass<*>.obj(language: GradleScriptLanguage): String {
    return if (language == GradleScriptLanguage.KOTLIN) {
        qualifiedName!!
    } else {
        "$qualifiedName.INSTANCE"
    }
}

internal fun String.setProperty(value: String, language: GradleScriptLanguage): String {
    return this + if (language == GradleScriptLanguage.KOTLIN) ".set($value)" else " = $value"
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

@Suppress("UNUSED_PARAMETER")
internal fun String.text(language: GradleScriptLanguage): String {
    return "\"$this\""
}

internal fun Enum<*>.enum(language: GradleScriptLanguage): String {
    return if (language == GradleScriptLanguage.KOTLIN) {
        this::class.qualifiedName + '.' + this.name
    } else {
        "\"$name\""
    }
}

internal fun PrintWriter.indented(indents: Int, content: String) {
    println(indent(indents) + content)
}
