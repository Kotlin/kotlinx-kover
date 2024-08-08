/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools.jacoco

import kotlinx.kover.gradle.plugin.commons.ReportContext
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkQueue
import org.jacoco.report.FileMultiReportOutput
import org.jacoco.report.html.HTMLFormatter
import org.jacoco.report.xml.XMLFormatter
import java.io.File
import java.util.*

internal fun ReportContext.jacocoHtmlReport(htmlReportDir: File, htmlTitle: String, charsetName: String?) {
    val workQueue: WorkQueue = services.workerExecutor.classLoaderIsolation {
        this.classpath.from(this@jacocoHtmlReport.classpath)
    }

    workQueue.submit(JacocoHtmlReportAction::class.java) {
        htmlDir.set(htmlReportDir)
        title.convention(htmlTitle)
        charset.convention(charsetName)

        fillCommonParameters(this@jacocoHtmlReport)
    }
}

internal abstract class JacocoHtmlReportAction : WorkAction<HtmlReportParameters> {
    override fun execute() {
        val htmlDir = parameters.htmlDir.get().asFile
        val output = FileMultiReportOutput(htmlDir)
        val formatter = HTMLFormatter()
        formatter.footerText = ""
        formatter.outputEncoding = parameters.charset.orNull ?: "UTF-8"
        formatter.locale = Locale.getDefault()
        val visitor = formatter.createVisitor(output)
        visitor.loadContent(parameters.title.get(), parameters.files.get(), parameters.filters.get())
        visitor.visitEnd()
    }
}

internal interface HtmlReportParameters : CommonJacocoParameters {
    val htmlDir: DirectoryProperty
    val title: Property<String>
    val charset: Property<String>
}

internal fun ReportContext.jacocoXmlReport(xmlReportFile: File, xmlTitle: String) {
    val workQueue: WorkQueue = services.workerExecutor.classLoaderIsolation {
        classpath.from(this@jacocoXmlReport.classpath)
    }

    workQueue.submit(JacocoXmlReportAction::class.java) {
        xmlFile.set(xmlReportFile)
        title.convention(xmlTitle)

        fillCommonParameters(this@jacocoXmlReport)
    }
}

internal interface XmlReportParameters : CommonJacocoParameters {
    val xmlFile: RegularFileProperty
    val title: Property<String>
}

internal abstract class JacocoXmlReportAction : WorkAction<XmlReportParameters> {
    override fun execute() {
        val xmlFile = parameters.xmlFile.get().asFile
        val stream = xmlFile.outputStream().buffered()

        val xmlFormatter = XMLFormatter()
        xmlFormatter.setOutputEncoding("UTF-8")
        val visitor = xmlFormatter.createVisitor(stream)
        visitor.loadContent(parameters.title.get(), parameters.files.get(), parameters.filters.get())

        visitor.visitEnd()
    }
}
