/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.writer

import kotlinx.kover.test.functional.framework.common.*
import kotlin.reflect.*


internal fun Iterable<String>.formatList(language: ScriptLanguage): String {
    val prefix = if (language == ScriptLanguage.KOTLIN) "listOf(" else "["
    val postfix = if (language == ScriptLanguage.KOTLIN) ")" else "]"

    return prefix + this.joinToString(separator = ",") { "\"$it\"" } + postfix
}

internal fun String.addAllList(list: Iterable<String>, language: ScriptLanguage): String {
    val listString = list.formatList(language)
    return this + if (language == ScriptLanguage.KOTLIN) " += $listString" else ".addAll($listString)"
}

internal fun KClass<*>.obj(language: ScriptLanguage): String {
    return if (language == ScriptLanguage.KOTLIN) {
        qualifiedName!!
    } else {
        "$qualifiedName.INSTANCE"
    }
}

internal fun String.setProperty(value: String, language: ScriptLanguage): String {
    return this + if (language == ScriptLanguage.KOTLIN) ".set($value)" else " = $value"
}

internal fun String.asTextLiteral(): String {
    return "\"$this\""
}

internal fun Enum<*>.enum(language: ScriptLanguage): String {
    return if (language == ScriptLanguage.KOTLIN) {
        this::class.qualifiedName + '.' + this.name
    } else {
        "\"$name\""
    }
}
