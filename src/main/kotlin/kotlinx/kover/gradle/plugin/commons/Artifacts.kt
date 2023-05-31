/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.commons

import java.io.File
import java.io.Serializable

/**
 * The contents of a single Kover artifact.
 */
internal class ArtifactContent(
    val sources: Set<File>,
    val outputs: Set<File>,
    val reports: Set<File>
): Serializable {
    fun joinWith(others: List<ArtifactContent>): ArtifactContent {
        val sources = this.sources.toMutableSet()
        val outputs = this.outputs.toMutableSet()
        val reports = this.reports.toMutableSet()

        others.forEach {
            sources += it.sources
            outputs += it.outputs
            reports += it.reports
        }

        return ArtifactContent(sources, outputs, reports)
    }
}


/**
 * Write Kover artifact content to the file.
 */
internal fun ArtifactContent.write(artifactFile: File) {
    val sources = sources.joinToString("\n") { it.canonicalPath }
    val outputs = outputs.joinToString("\n") { it.canonicalPath }
    val reports = reports.joinToString("\n") { it.canonicalPath }

    artifactFile.writeText("$sources\n\n$outputs\n\n$reports")
}

/**
 * Read Kover artifact content from the file.
 */
internal fun File.parseArtifactFile(): ArtifactContent {
    if (!exists()) return ArtifactContent(emptySet(), emptySet(), emptySet())

    val iterator = readLines().iterator()

    val sources = iterator.groupUntil { it.isEmpty() }.map { File(it) }.filter { it.exists() }.toSet()
    val outputs = iterator.groupUntil { it.isEmpty() }.map { File(it) }.filter { it.exists() }.toSet()
    val reports = iterator.groupUntil { it.isEmpty() }.map { File(it) }.filter { it.exists() }.toSet()

    return ArtifactContent(sources, outputs, reports)
}

private fun <T> Iterator<T>.groupUntil(block: (T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    while (hasNext()) {
        val next = next()
        if (block(next)) break
        result += next
    }
    return result
}