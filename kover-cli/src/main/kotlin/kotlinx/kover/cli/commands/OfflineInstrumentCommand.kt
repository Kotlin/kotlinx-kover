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

import kotlinx.kover.features.jvm.KoverLegacyFeatures
import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.Option
import java.io.File
import java.io.PrintWriter


internal class OfflineInstrumentCommand : Command {
    // hint: MutableList used to remove variance, args4j accept java.util.List<File> but not java.util.List<? extends File>
    @Argument(usage = "list of the compiled class-files roots", metaVar = "<class-file-path>", required = true)
    private var roots: MutableList<File> = ArrayList()

    @Option(name = "--dest", usage = "path to write instrumented Java classes to", metaVar = "<dir>", required = true)
    private var outputDir: File? = null

    @Option(name = "--hits", usage = "a flag to enable line hits counting")
    private var countHits = false

    @Option(
        name = "--include",
        usage = "instrument only specified classes, wildcards `*` and `?` are acceptable",
        metaVar = "<class-name>"
    )
    private var includeClasses: MutableList<String> = ArrayList()

    @Option(
        name = "--exclude",
        usage = "filter to exclude classes from instrumentation, wildcards `*` and `?` are acceptable. Excludes have priority over includes",
        metaVar = "<class-name>"
    )
    private var excludeClasses: MutableList<String> = ArrayList()

    @Option(
        name = "--excludeAnnotation",
        usage = "filter to exclude annotated classes from instrumentation, wildcards `*` and `?` are acceptable",
        metaVar = "<annotation-name>"
    )
    private var excludeAnnotation: MutableList<String> = ArrayList()

    override val name: String = "instrument"

    override val description: String = "Offline instrumentation of JVM class-files"


    override fun call(output: PrintWriter, errorWriter: PrintWriter): Int {
        val filters = KoverLegacyFeatures.ClassFilters(
            includeClasses.toSet(),
            excludeClasses.toSet(),
            excludeAnnotation.toSet()
        )

        try {
            KoverLegacyFeatures.instrument(outputDir!!, roots, filters, countHits)
        } catch (e: Exception) {
            errorWriter.println("Instrumentation failed: " + e.message)
            return -1
        }

        return 0
    }
}
