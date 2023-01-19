/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.ReportFilters
import kotlinx.kover.gradle.plugin.util.*
import kotlinx.kover.gradle.plugin.util.json.*
import java.io.*

internal fun ReportContext.aggregateRawReports(filters: List<ReportFilters>): List<AggregationGroup> {
    val aggRequestFile = tempDir.resolve("agg-request.json")

    val aggGroups = filters.mapIndexed { index: Int, reportFilters: ReportFilters ->
        val filePrefix = if (filters.size > 1) "-$index" else ""
        AggregationGroup(
            tempDir.resolve("agg-ic$filePrefix.ic"),
            tempDir.resolve("agg-smap$filePrefix.smap"),
            reportFilters
        )
    }

    aggRequestFile.writeAggJson(files, aggGroups)
    services.exec.javaexec {
        mainClass.set("com.intellij.rt.coverage.aggregate.Main")
        this@javaexec.classpath = this@aggregateRawReports.classpath
        args = mutableListOf(aggRequestFile.canonicalPath)
    }

    return aggGroups
}

internal class AggregationGroup(val ic: File, val smap: File, val filters: ReportFilters)

/*
{
  "reports": [{"ic": "path"}, ...],
  "modules": [{"output": ["path1", "path2"], "sources": ["source1",...]},... ],
  "result": [{
    "filters": {   // optional
      "include": { // optional
        "classes": [ String,... ]
      },
      "exclude": { // optional
        "classes": [ String,... ]
        "annotations": [String,...]
      }
    },
    "aggregatedReportFile": String,
    "smapFile": String
  },...
  ]

  }
}
 */
private fun File.writeAggJson(
    buildFiles: BuildFiles,
    groups: List<AggregationGroup>
) {
    writeJsonObject(mapOf(
        "reports" to buildFiles.reports.map { mapOf("ic" to it) },
        "modules" to listOf(mapOf("sources" to buildFiles.sources, "output" to buildFiles.outputs)),
        "result" to groups.map { group ->
            mapOf(
                "aggregatedReportFile" to group.ic,
                "smapFile" to group.smap,
                "filters" to mutableMapOf<String, Any>().also {
                    if (group.filters.includesClasses.isNotEmpty()) {
                        it["include"] =
                            mapOf("classes" to group.filters.includesClasses.map { c -> c.wildcardsToRegex() })
                    }
                    val excludes = mutableMapOf<String, Any>()
                    if (group.filters.excludesClasses.isNotEmpty()) {
                        excludes += "classes" to group.filters.excludesClasses.map { c -> c.wildcardsToRegex() }
                    }
                    if (group.filters.excludesAnnotations.isNotEmpty()) {
                        excludes += "annotations" to group.filters.excludesAnnotations.map { c -> c.wildcardsToRegex() }
                    }
                    if (excludes.isNotEmpty()) {
                        it["exclude"] = excludes
                    }
                }
            )
        }
    ))
}
