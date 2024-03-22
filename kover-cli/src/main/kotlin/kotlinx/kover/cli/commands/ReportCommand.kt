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

import kotlinx.kover.features.jvm.ClassFilters
import kotlinx.kover.features.jvm.KoverLegacyFeatures
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

    @Option(name = "--title", usage = "title in the HTML or XML report", metaVar = "<title>")
    private var title: String? = null

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
        usage = "filter to exclude classes and functions marked by this annotation, wildcards `*` and `?` are acceptable",
        metaVar = "<exclude-annotation-name>"
    )
    private var excludeAnnotation: MutableList<String> = ArrayList()

    @Option(
        name = "--includeAnnotation",
        usage = "filter to include classes by this annotation, wildcards `*` and `?` are acceptable",
        metaVar = "<include-annotation-name>"
    )
    private var includeAnnotation: MutableList<String> = ArrayList()

    @Option(
        name = "--excludeInheritedFrom",
        usage = "filter to exclude classes inheriting the specified class or implementing an interface, wildcards `*` and `?` are acceptable",
        metaVar = "<exclude-ancestor-name>"
    )
    private var excludeInheritedFrom: MutableList<String> = ArrayList()

    @Option(
        name = "--includeInheritedFrom",
        usage = "filter to include only classes inheriting the specified class or implementing an interface, wildcards `*` and `?` are acceptable",
        metaVar = "<include-ancestor-name>"
    )
    private var includeInheritedFrom: MutableList<String> = ArrayList()

    override val name: String = "report"

    override val description: String = "Generates human-readable reports in various formats from binary report files"


    override fun call(output: PrintWriter, errorWriter: PrintWriter): Int {
        val filters = ClassFilters(
            includeClasses.toSet(),
            excludeClasses.toSet(),
            includeAnnotation.toSet(),
            excludeAnnotation.toSet(),
            includeInheritedFrom.toSet(),
            excludeInheritedFrom.toSet()
        )

        var fail = false
        if (xmlFile != null) {
            try {
                KoverLegacyFeatures.generateXmlReport(xmlFile!!, binaryReports, outputRoots, sourceRoots, title ?: "Kover XML Report", filters)
            } catch (e: IOException) {
                fail = true
                errorWriter.println("XML generation failed: " + e.message)
            }
        }
        if (htmlDir != null) {
            try {
                KoverLegacyFeatures.generateHtmlReport(htmlDir!!, null, binaryReports, outputRoots, sourceRoots, title ?: "Kover HTML Report", filters)
            } catch (e: IOException) {
                fail = true
                errorWriter.println("HTML generation failed: " + e.message)
            }
        }
        if (xmlFile == null && htmlDir == null) {
            errorWriter.println("At least one format must be used: XML, HTML.")
            fail = true
        }
        return if (fail) -1 else 0
    }
}
