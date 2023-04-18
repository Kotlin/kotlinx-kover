/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlinx.kover.cli.commands

import com.intellij.rt.coverage.report.ReportLoadStrategy
import com.intellij.rt.coverage.report.ReportLoadStrategy.RawReportLoadStrategy
import com.intellij.rt.coverage.report.Reporter
import com.intellij.rt.coverage.report.data.BinaryReport
import com.intellij.rt.coverage.report.data.Filters
import com.intellij.rt.coverage.report.data.Module
import kotlinx.kover.cli.util.asPatterns
import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.Option
import java.io.File
import java.io.IOException
import java.io.PrintWriter


internal class ReportCommand : Command {
    @Argument(usage = "list of binary reports files", metaVar = "<binary-report-path>")
    private var binaryReports: MutableList<File> = ArrayList()

    @Option(name = "--src", usage = "location of the source files root", metaVar = "<sources-path>", required = true)
    private var sourceRoots: MutableList<File> = ArrayList()

    @Option(
        name = "--classfiles",
        usage = "location of the compiled class-files root (must be original and not instrumented)",
        metaVar = "<class-file-path>",
        required = true
    )
    private var outputRoots: MutableList<File> = ArrayList()

    @Option(name = "--xml", usage = "generate a XML report in the specified path", metaVar = "<xml-file-path>")
    private var xmlFile: File? = null

    @Option(name = "--html", usage = "generate a HTML report in the specified path", metaVar = "<html-dir>")
    private var htmlDir: File? = null

    @Option(name = "--title", usage = "title in the HTML report", metaVar = "<html-title>")
    private var htmlTitle: String? = null

    @Option(
        name = "--include",
        usage = "filter to include classes, wildcards `*` and `?` are acceptable",
        metaVar = "<class-name>"
    )
    private var includeClasses: MutableList<String> = ArrayList()

    @Option(
        name = "--exclude",
        usage = "filter to exclude classes, wildcards `*` and `?` are acceptable",
        metaVar = "<class-name>"
    )
    private var excludeClasses: MutableList<String> = ArrayList()

    @Option(
        name = "--excludeAnnotation",
        usage = "filter to include classes and functions marked by this annotation, wildcards `*` and `?` are acceptable",
        metaVar = "<annotation-name>"
    )
    private var excludeAnnotation: MutableList<String> = ArrayList()

    override val name: String = "report"

    override val description: String = "Generates human-readable reports in various formats from binary report files (*.ic)"


    override fun call(output: PrintWriter, error: PrintWriter): Int {
        val binaryReports: MutableList<BinaryReport> = ArrayList()
        for (binaryReport in this.binaryReports) {
            binaryReports.add(BinaryReport(binaryReport, null))
        }
        val module = Module(outputRoots, sourceRoots)
        val filters = Filters(
            includeClasses.asPatterns(),
            excludeClasses.asPatterns(),
            excludeAnnotation.asPatterns()
        )
        val loadStrategy: ReportLoadStrategy = RawReportLoadStrategy(binaryReports, listOf(module), filters)
        val reporter = Reporter(loadStrategy)
        var fail = false
        if (xmlFile != null) {
            try {
                reporter.createXMLReport(xmlFile)
            } catch (e: IOException) {
                fail = true
                error.println("XML generation failed: " + e.message)
            }
        }
        if (htmlDir != null) {
            try {
                reporter.createHTMLReport(htmlDir, htmlTitle)
            } catch (e: IOException) {
                fail = true
                error.println("HTML generation failed: " + e.message)
            }
        }
        if (xmlFile == null && htmlDir == null) {
            error.println("At least one format must be used: XML, HTML.")
            fail = true
        }
        return if (fail) -1 else 0
    }
}
