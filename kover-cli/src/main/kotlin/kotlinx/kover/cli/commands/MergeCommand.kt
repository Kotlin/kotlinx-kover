/*
 * Copyright 2000-2024 JetBrains s.r.o.
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


internal class MergeCommand : Command {
    @Argument(usage = "list of binary reports files", metaVar = "<binary-report-path>")
    private var binaryReports: MutableList<File> = ArrayList()

    @Option(
        name = "--target",
        usage = "merged binary report file",
        metaVar = "<merged-report-path>",
        required = true
    )
    private var targetReport: File? = null

    override val name: String = "merge"

    override val description: String = "Merge binary report files into one"


    override fun call(output: PrintWriter, errorWriter: PrintWriter): Int {
        try {
            KoverLegacyFeatures.mergeIc(targetReport!!, binaryReports)
        } catch (e: Exception) {
            errorWriter.println("Binary reports merging failed: " + e.message)
            return -1
        }
        return 0
    }
}
