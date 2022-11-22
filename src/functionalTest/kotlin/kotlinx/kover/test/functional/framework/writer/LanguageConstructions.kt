/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.writer

import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.common.ScriptLanguage.GROOVY
import kotlinx.kover.test.functional.framework.common.ScriptLanguage.KOTLIN
import kotlin.reflect.*


internal fun Iterable<String>.formatList(language: ScriptLanguage): String {
    val prefix = if (language == KOTLIN) "listOf(" else "["
    val postfix = if (language == KOTLIN) ")" else "]"

    return prefix + this.joinToString(separator = ",") { "\"$it\"" } + postfix
}

internal fun String.addAllList(list: Iterable<String>, language: ScriptLanguage): String {
    val listString = list.formatList(language)
    return this + if (language == KOTLIN) " += $listString" else ".addAll($listString)"
}

internal fun KClass<*>.obj(language: ScriptLanguage): String {
    return if (language == KOTLIN) {
        qualifiedName!!
    } else {
        "$qualifiedName.INSTANCE"
    }
}

internal fun String.setProperty(value: String, language: ScriptLanguage): String {
    return this + if (language == KOTLIN) ".set($value)" else " = $value"
}

internal fun String.asTextLiteral(): String {
    return "\"$this\""
}

internal fun String.asUri(language: ScriptLanguage): String {
    return if (language == GROOVY) "\"$this\"" else "uri(\"$this\")"
}

internal fun Enum<*>.enum(language: ScriptLanguage): String {
    return if (language == KOTLIN) {
        this::class.qualifiedName + '.' + this.name
    } else {
        "\"$name\""
    }
}
