/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import kotlinx.kover.gradle.plugin.commons.*
import java.io.*


internal fun ReportContext.jacocoHtmlReport(htmlDir: File, title: String, filters: ReportFilters) {
    callAntReport(filters, title) {
        htmlDir.mkdirs()
        invokeMethod("html", mapOf("destdir" to htmlDir))
    }
}

internal fun ReportContext.jacocoXmlReport(xmlFile: File, filters: ReportFilters) {
    callAntReport(filters, projectPath) {
        xmlFile.parentFile.mkdirs()
        invokeMethod("xml", mapOf("destfile" to xmlFile))
    }
}
