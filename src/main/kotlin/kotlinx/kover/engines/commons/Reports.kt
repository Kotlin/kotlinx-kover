package kotlinx.kover.engines.commons

import java.io.*

internal class Report(val files: List<File>, val projects: List<ProjectInfo>)
internal class ProjectInfo(val sources: Iterable<File>, val outputs: Iterable<File>)

private val regexMetacharactersSet = "<([{\\^-=$!|]})+.>".toSet()

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
