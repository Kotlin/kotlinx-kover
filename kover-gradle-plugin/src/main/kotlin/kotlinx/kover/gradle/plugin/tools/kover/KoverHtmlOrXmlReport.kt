/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.kover

import com.intellij.rt.coverage.report.api.Filters
import com.intellij.rt.coverage.report.api.ReportApi
import kotlinx.kover.gradle.plugin.commons.ReportContext
import kotlinx.kover.gradle.plugin.util.asPatterns
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkQueue
import java.io.File


internal fun ReportContext.koverHtmlReport(htmlReportDir: File, htmlTitle: String, charsetName: String?) {
    val workQueue: WorkQueue = services.workerExecutor.classLoaderIsolation {
        this.classpath.from(this@koverHtmlReport.classpath)
    }

    workQueue.submit(HtmlReportAction::class.java) {
        htmlDir.set(htmlReportDir)
        title.convention(htmlTitle)
        charset.convention(charsetName)
        filters.convention(this@koverHtmlReport.filters)

        files.convention(this@koverHtmlReport.files)
        tempDir.set(this@koverHtmlReport.tempDir)
        projectPath.convention(this@koverHtmlReport.projectPath)
    }}

internal fun ReportContext.koverXmlReport(xmlReportFile: File) {
    val workQueue: WorkQueue = services.workerExecutor.classLoaderIsolation {
        classpath.from(this@koverXmlReport.classpath)
    }

    workQueue.submit(XmlReportAction::class.java) {
        xmlFile.set(xmlReportFile)
        filters.convention(this@koverXmlReport.filters)

        files.convention(this@koverXmlReport.files)

        tempDir.set(this@koverXmlReport.tempDir)
        projectPath.convention(this@koverXmlReport.projectPath)
    }
}

internal interface XmlReportParameters: ReportParameters {
    val xmlFile: RegularFileProperty
}

internal interface HtmlReportParameters: ReportParameters {
    val htmlDir: DirectoryProperty
    val title: Property<String>
}

internal abstract class XmlReportAction : WorkAction<XmlReportParameters> {
    override fun execute() {
        val files = parameters.files.get()
        val filtersIntern = parameters.filters.get()
        val filters = Filters(
            filtersIntern.includesClasses.toList().asPatterns(),
            filtersIntern.excludesClasses.toList().asPatterns(),
            filtersIntern.excludesAnnotations.toList().asPatterns()
        )

        ReportApi.xmlReport(
            parameters.xmlFile.get().asFile,
            files.reports.toList(),
            files.outputs.toList(),
            files.sources.toList(),
            filters
        )
    }
}

internal abstract class HtmlReportAction : WorkAction<HtmlReportParameters> {
    override fun execute() {
        val htmlDir = parameters.htmlDir.get().asFile
        htmlDir.mkdirs()

        val files = parameters.files.get()
        val filtersIntern = parameters.filters.get()
        val filters = Filters(
            filtersIntern.includesClasses.toList().asPatterns(),
            filtersIntern.excludesClasses.toList().asPatterns(),
            filtersIntern.excludesAnnotations.toList().asPatterns()
        )

        ReportApi.htmlReport(
            parameters.htmlDir.get().asFile,
            parameters.title.get(),
            parameters.charset.orNull,
            files.reports.toList(),
            files.outputs.toList(),
            files.sources.toList(),
            filters
        )
    }
}
