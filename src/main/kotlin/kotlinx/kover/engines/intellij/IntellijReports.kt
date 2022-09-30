/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.intellij

import kotlinx.kover.tasks.*
import kotlinx.kover.util.json.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.process.ExecOperations
import java.io.*

internal fun Task.intellijReport(
    exec: ExecOperations,
    projectFiles: Map<String, ProjectFiles>,
    filters: ReportFilters,
    xmlFile: File?,
    htmlDir: File?,
    classpath: FileCollection
) {
    xmlFile?.let {
        xmlFile.parentFile.mkdirs()
    }

    htmlDir?.let {
        htmlDir.mkdirs()
    }


    val aggRequest = File(temporaryDir, "agg-request.json")
    val aggEntry =
        AggregatorEntry(File(temporaryDir, "agg-ic.ic"), File(temporaryDir, "agg-smap.smap"), filters)
    aggRequest.writeAggJson(projectFiles, listOf(aggEntry))
    exec.javaexec {
        mainClass.set("com.intellij.rt.coverage.aggregate.Main")
        this@javaexec.classpath = classpath
        args = mutableListOf(aggRequest.canonicalPath)
    }

    val sources = projectFiles.flatMap { it.value.sources }
    val argsFile = File(temporaryDir, "intellijreport.json")
    argsFile.writeReportsJson(sources, aggEntry, xmlFile, htmlDir)

    exec.javaexec {
        mainClass.set("com.intellij.rt.coverage.report.Main")
        this@javaexec.classpath = classpath
        args = mutableListOf(argsFile.canonicalPath)
    }
}

/*
JSON format:
```
{
  "format": "kover-agg",
  "reports": [{ic: String, "smap": String}], // single element
  "modules": [{sources: [String...]}],       // single element
  "xml": String, // optional
  "html": String // optional
}
```
 */
private fun File.writeReportsJson(
    sources: List<File>,
    aggregatorEntry: AggregatorEntry,
    xmlFile: File?,
    htmlDir: File?
) {
    writeJsonObject(mutableMapOf(
        "format" to "kover-agg",
        "reports" to listOf(mapOf("ic" to aggregatorEntry.ic, "smap" to aggregatorEntry.smap)),
        "modules" to listOf(mapOf("sources" to sources) ),
    ).also {
        xmlFile?.also { f ->
            it["xml"] = f
        }
        htmlDir?.also { d ->
            it["html"] = d
        }
    })
}
