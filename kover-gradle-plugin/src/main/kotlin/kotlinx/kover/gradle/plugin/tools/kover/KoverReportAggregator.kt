/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import com.intellij.rt.coverage.aggregate.api.AggregatorApi
import com.intellij.rt.coverage.aggregate.api.Request
import com.intellij.rt.coverage.report.api.Filters
import kotlinx.kover.gradle.plugin.commons.ArtifactContent
import kotlinx.kover.gradle.plugin.commons.ReportFilters
import kotlinx.kover.gradle.plugin.util.asPatterns
import java.io.File

internal fun aggregateRawReports(files: ArtifactContent, filters: List<ReportFilters>, tempDir: File): List<AggregationGroup> {
    val aggGroups = filters.mapIndexed { index: Int, reportFilters: ReportFilters ->
        val filePrefix = if (filters.size > 1) "-$index" else ""
        AggregationGroup(
            tempDir.resolve("agg-ic$filePrefix.ic"),
            tempDir.resolve("agg-smap$filePrefix.smap"),
            reportFilters
        )
    }

    val requests = aggGroups.map { group ->
        val filtersR = Filters(
            group.filters.includesClasses.toList().asPatterns(),
            group.filters.excludesClasses.toList().asPatterns(),
            group.filters.excludesAnnotations.toList().asPatterns()
        )
        Request(filtersR, group.ic, group.smap)
    }

    AggregatorApi.aggregate(requests, files.reports.toList(), files.outputs.toList())

    return aggGroups
}

internal class AggregationGroup(val ic: File, val smap: File, val filters: ReportFilters)
