/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import com.intellij.rt.coverage.report.api.ReportApi
import kotlinx.kover.gradle.plugin.commons.ReportContext
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import java.io.File


internal fun ReportContext.koverHtmlReport(htmlReportDir: File, htmlTitle: String, charsetName: String?) {
    submitAction<HtmlReportAction, HtmlReportParameters> {
        htmlDir.set(htmlReportDir)
        title.convention(htmlTitle)
        charset.convention(charsetName)
        filters.convention(this@koverHtmlReport.filters)

        files.convention(this@koverHtmlReport.files)
        tempDir.set(this@koverHtmlReport.tempDir)
        projectPath.convention(this@koverHtmlReport.projectPath)
    }
}

internal fun ReportContext.koverXmlReport(xmlReportFile: File) {
    submitAction<XmlReportAction, XmlReportParameters> {
        xmlFile.set(xmlReportFile)
        filters.convention(this@koverXmlReport.filters)

        files.convention(this@koverXmlReport.files)

        tempDir.set(this@koverXmlReport.tempDir)
        projectPath.convention(this@koverXmlReport.projectPath)
    }
}

internal interface XmlReportParameters : ReportParameters {
    val xmlFile: RegularFileProperty
}

internal interface HtmlReportParameters : ReportParameters {
    val htmlDir: DirectoryProperty
    val title: Property<String>
}

internal abstract class XmlReportAction : AbstractReportAction<XmlReportParameters>() {
    override fun generate() {
        val files = parameters.files.get()
        val filters = parameters.filters.get()

        ReportApi.xmlReport(
            parameters.xmlFile.get().asFile,
            files.reports.toList(),
            files.outputs.toList(),
            files.sources.toList(),
            filters.toIntellij()
        )
    }
}

internal abstract class HtmlReportAction : AbstractReportAction<HtmlReportParameters>() {
    override fun generate() {
        val htmlDir = parameters.htmlDir.get().asFile
        htmlDir.mkdirs()

        val files = parameters.files.get()
        val filters = parameters.filters.get()

        ReportApi.htmlReport(
            parameters.htmlDir.get().asFile,
            parameters.title.get(),
            parameters.charset.orNull,
            files.reports.toList(),
            files.outputs.toList(),
            files.sources.toList(),
            filters.toIntellij()
        )
    }
}
