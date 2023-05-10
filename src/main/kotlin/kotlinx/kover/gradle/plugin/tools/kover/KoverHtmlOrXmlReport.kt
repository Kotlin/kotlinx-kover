/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.ReportFilters
import kotlinx.kover.gradle.plugin.util.json.*
import java.io.*

internal fun ReportContext.koverHtmlReport(htmlDir: File, title: String, charset: String?, filters: ReportFilters) {
    val aggGroups = aggregateRawReports(listOf(filters))
    generateHtmlOrXml(aggGroups.first(), htmlDir = htmlDir, title = title, charset = charset)
}

internal fun ReportContext.koverXmlReport(xmlFile: File, filters: ReportFilters) {
    val aggGroups = aggregateRawReports(listOf(filters))
    generateHtmlOrXml(aggGroups.first(), xmlFile = xmlFile)
}

private fun ReportContext.generateHtmlOrXml(
    aggGroup: AggregationGroup,
    htmlDir: File? = null,
    xmlFile: File? = null,
    title: String? = null,
    charset: String? = null,
) {
    val argsFile = tempDir.resolve("kover-report.json")
    argsFile.writeHtmlOrXmlJson(files.sources, aggGroup, xmlFile, htmlDir, title, charset)

    services.exec.javaexec {
        mainClass.set("com.intellij.rt.coverage.report.Main")
        this@javaexec.classpath = this@generateHtmlOrXml.classpath
        args = mutableListOf(argsFile.canonicalPath)
    }
}


/*
JSON format:
```
{
  "format": "kover-agg",
  "title": "report title"  [OPTIONAL],
  "reports": [{ic: String, "smap": String}], // single element
  "modules": [{sources: [String...]}],       // single element
  "xml": String, // optional
  "html": String // optional
}
```
 */
private fun File.writeHtmlOrXmlJson(
    sources: Set<File>,
    aggregationGroup: AggregationGroup,
    xmlFile: File?,
    htmlDir: File?,
    title: String?,
    charset: String?,
) {
    writeJsonObject(mutableMapOf(
        // required fields

        "format" to "kover-agg",
        "reports" to listOf(mapOf("ic" to aggregationGroup.ic, "smap" to aggregationGroup.smap)),
        "modules" to listOf(mapOf("sources" to sources)),
    ).also {
        // optional fields

        title?.also { t ->
            it["title"] = t
        }
        xmlFile?.also { f ->
            it["xml"] = f
        }
        htmlDir?.also { d ->
            it["html"] = d
        }
        charset?.also { c ->
            it["charset"] = c
        }
    })
}
