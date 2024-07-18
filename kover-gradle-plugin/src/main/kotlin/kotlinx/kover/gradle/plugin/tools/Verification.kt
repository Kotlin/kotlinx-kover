/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools

import kotlinx.kover.features.jvm.*
import kotlinx.kover.gradle.plugin.dsl.*
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import java.io.File
import java.io.Serializable
import java.nio.charset.Charset

internal fun Iterable<CoverageValue>.writeToFile(file: File, header: String?, lineFormat: String) {
    file.bufferedWriter(Charset.forName("UTF-8")).use { writer ->
        header?.let { h -> writer.appendLine(h) }

        forEach { coverage ->
            val entityName = coverage.entityName ?: "application"
            writer.appendLine(
                lineFormat.replace("<value>", coverage.value.stripTrailingZeros().toPlainString())
                    .replace("<entity>", entityName)
            )
        }
    }
}

internal fun File.writeNoSources(header: String?) {
    this.bufferedWriter(Charset.forName("UTF-8")).use { writer ->
        header?.let { h -> writer.appendLine(h) }
        writer.appendLine("No sources")
    }
}


internal data class CoverageRequest(
    val entity: GroupingEntityType,
    val metric: CoverageUnit,
    val aggregation: AggregationType,
    val header: String?,
    val lineFormat: String,
): Serializable
