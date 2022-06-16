/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.intellij

import kotlinx.kover.api.*
import kotlinx.kover.engines.commons.*
import kotlinx.kover.json.*
import kotlinx.kover.tasks.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.process.ExecOperations
import java.io.*

internal fun Task.intellijReport(
    exec: ExecOperations,
    projectFiles: Map<String, ProjectFiles>,
    filters: KoverClassFilters,
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

    val argsFile = File(temporaryDir, "intellijreport.json")
    argsFile.writeReportsJson(projectFiles, filters, xmlFile, htmlDir)

    exec.javaexec { e ->
        e.mainClass.set("com.intellij.rt.coverage.report.Main")
        e.classpath = classpath
        e.args = mutableListOf(argsFile.canonicalPath)
    }
}

/*
JSON format:
```
{
  reports: [{ic: "path", smap: "path" [OPTIONAL]}, ...],
  modules: [{output: ["path1", "path2"], sources: ["source1", â€¦]}, {â€¦}],
  xml: "path" [OPTIONAL],
  html: "directory" [OPTIONAL],
  include: {
        classes: ["regex1", "regex2"] [OPTIONAL]
   } [OPTIONAL],
  exclude: {
        classes: ["regex1", "regex2"] [OPTIONAL]
   } [OPTIONAL],
}
```


JSON example:
```
{
  "reports": [
        {"ic": "/path/to/binary/report/result.ic"}
  ],
  "html": "/path/to/html",
  "modules": [
    {
      "output": [
        "/build/output"
      ],
      "sources": [
        "/sources/java",
        "/sources/kotlin"
      ]
    }
  ]
}
```
 */
private fun File.writeReportsJson(
    projectFiles: Map<String, ProjectFiles>,
    classFilters: KoverClassFilters,
    xmlFile: File?,
    htmlDir: File?
) {
    writeJsonObject(mutableMapOf<String, Any>(
        "reports" to projectFiles.flatMap { it.value.binaryReportFiles }.map { mapOf("ic" to it) },
        "modules" to projectFiles.map { mapOf("sources" to it.value.sources, "output" to it.value.outputs) },
    ).also {
        if (classFilters.includes.isNotEmpty()) {
            it["include"] = mapOf("classes" to classFilters.includes.map { c -> c.wildcardsToRegex() })
        }
        if (classFilters.excludes.isNotEmpty()) {
            it["exclude"] = mapOf("classes" to classFilters.excludes.map { c -> c.wildcardsToRegex() })
        }
        xmlFile?.also { f ->
            it["xml"] = f
        }
        htmlDir?.also { d ->
            it["html"] = d
        }
    })
}
