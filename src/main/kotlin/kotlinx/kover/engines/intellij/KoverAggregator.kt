/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.intellij

import kotlinx.kover.engines.commons.*
import kotlinx.kover.tasks.*
import kotlinx.kover.util.json.*
import java.io.*

internal class AggregatorEntry(val ic: File, val smap: File, val filters: ReportFilters)

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
internal fun File.writeAggJson(
    projectFiles: Map<String, ProjectFiles>,
    entries: List<AggregatorEntry>
) {
    writeJsonObject(mapOf(
        "reports" to projectFiles.flatMap { it.value.binaryReportFiles }.map { mapOf("ic" to it) },
        "modules" to projectFiles.map { mapOf("sources" to it.value.sources, "output" to it.value.outputs) },
        "result" to entries.map { entry ->
            mapOf(
                "aggregatedReportFile" to entry.ic,
                "smapFile" to entry.smap,
                "filters" to mutableMapOf<String, Any>().also {
                    if (entry.filters.includesClasses.isNotEmpty()) {
                        it["include"] =
                            mapOf("classes" to entry.filters.includesClasses.map { c -> c.wildcardsToRegex() })
                    }
                    val excludes = mutableMapOf<String, Any>()
                    if (entry.filters.excludesClasses.isNotEmpty()) {
                        excludes += "classes" to entry.filters.excludesClasses.map { c -> c.wildcardsToRegex() }
                    }
                    if (entry.filters.excludesAnnotations.isNotEmpty()) {
                        excludes += "annotations" to entry.filters.excludesAnnotations.map { c -> c.wildcardsToRegex() }
                    }
                    if (excludes.isNotEmpty()) {
                        it["exclude"] = excludes
                    }
                }
            )
        }
    ))
}
