/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.intellij

import kotlinx.kover.api.KoverClassFilter
import kotlinx.kover.engines.commons.wildcardsToRegex
import kotlinx.kover.json.writeJsonObject
import kotlinx.kover.tasks.ProjectFiles
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.process.ExecOperations
import java.io.File

internal fun Task.intellijReport(
    exec: ExecOperations,
    projectFiles: ProjectFiles,
    filters: KoverClassFilter,
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
    projectFiles: ProjectFiles,
    classFilter: KoverClassFilter,
    xmlFile: File?,
    htmlDir: File?
) {
    writeJsonObject(mutableMapOf<String, Any>(
        "reports" to mapOf("ic" to projectFiles.binaryReportFiles),
        "modules" to mapOf("sources" to projectFiles.sources, "output" to projectFiles.outputs),
    ).also {
        if (classFilter.includes.isNotEmpty()) {
            it["include"] = mapOf("classes" to classFilter.includes.map { c -> c.wildcardsToRegex() })
        }
        if (classFilter.excludes.isNotEmpty()) {
            it["exclude"] = mapOf("classes" to classFilter.excludes.map { c -> c.wildcardsToRegex() })
        }
        xmlFile?.also { f ->
            it["xml"] = f
        }
        htmlDir?.also { d ->
            it["html"] = d
        }
    })
}
