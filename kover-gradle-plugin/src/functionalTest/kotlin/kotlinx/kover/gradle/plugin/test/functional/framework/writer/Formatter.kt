/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.writer

import kotlinx.kover.gradle.plugin.test.functional.framework.common.ScriptLanguage
import java.io.*

internal fun File.forScript(): String {
    return "file(\"${canonicalPath}\")"
}

internal fun Enum<*>.forScript(): String {
    return "${this::class.qualifiedName}.$name"
}

internal fun String.uriForScript(language: ScriptLanguage): String {
    return if (language == ScriptLanguage.GROOVY) "\"$this\"" else "uri(\"$this\")"
}
