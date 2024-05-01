/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.features.jvm.impl

import com.intellij.rt.coverage.report.api.Filters
import kotlinx.kover.features.jvm.ClassFilters
import java.util.regex.Pattern

internal fun ClassFilters.convert(): Filters {
    return Filters(
        convert(includeClasses),
        convert(excludeClasses),
        emptyList(),
        convert(excludeAnnotation),
        emptyList(),
        emptyList()
    )
}

private fun convert(templates: Set<String>): List<Pattern> {
    val patterns = ArrayList<Pattern>(templates.size)
    for (template in templates) {
        patterns.add(Pattern.compile(template.wildcardsToRegex()))
    }
    return patterns
}

/**
 * Replaces characters `*` to `.*`, `#` to `[^.]*` and `?` to `.` regexp characters and also add escape char '\' before regexp metacharacters (see [regexMetacharactersSet]).
 */
internal fun String.wildcardsToRegex(): String {
    // in most cases, the characters `*` or `.` will be present therefore, we increase the capacity in advance
    val builder = StringBuilder(length * 2)

    forEach { char ->
        when (char) {
            in regexMetacharactersSet -> builder.append('\\').append(char)
            '*' -> builder.append('.').append("*")
            '?' -> builder.append('.')
            '#' -> builder.append("[^.]*")
            else -> builder.append(char)
        }
    }

    return builder.toString()
}

private val regexMetacharactersSet = "<([{\\^-=$!|]})+.>".toSet()
