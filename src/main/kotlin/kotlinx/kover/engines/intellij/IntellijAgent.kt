package kotlinx.kover.engines.intellij

import kotlinx.kover.api.*
import org.gradle.api.*
import org.gradle.api.artifacts.*
import java.io.*

internal class IntellijAgent(val config: Configuration) {
    private val trackingPerTest = false // a flag to enable tracking per test coverage
    private val calculateForUnloadedClasses = true // a flag to calculate coverage for unloaded classes
    private val appendToDataFile = false // a flag to use data file as initial coverage
    private val samplingMode = false //a flag to run coverage in sampling mode or in tracing mode otherwise
    private val generateSmapFile = true

    fun buildCommandLineArgs(extension: KoverTaskExtension, task: Task): MutableList<String> {
        val argsFile = File(task.temporaryDir, "intellijagent.args")
        argsFile.writeArgsToFile(extension)
        val jarFile = config.fileCollection { it.name == "intellij-coverage-agent" }.singleFile
        return mutableListOf(
            "-javaagent:${jarFile.canonicalPath}=${argsFile.canonicalPath}",
            "-Didea.coverage.check.inline.signatures=true",
            "-Didea.new.sampling.coverage=true",
            "-Didea.new.tracing.coverage=true"
        )
    }

    private fun File.writeArgsToFile(extension: KoverTaskExtension) {
        val binary = extension.binaryReportFile.get()
        binary.parentFile.mkdirs()
        val binaryPath = binary.canonicalPath

        printWriter().use { pw ->
            pw.appendLine(binaryPath)
            pw.appendLine(trackingPerTest.toString())
            pw.appendLine(calculateForUnloadedClasses.toString())
            pw.appendLine(appendToDataFile.toString())
            pw.appendLine(samplingMode.toString())
            pw.appendLine(generateSmapFile.toString())
            pw.appendLine("$binaryPath.smap")
            extension.includes.forEach { i ->
                pw.appendLine(i)
            }

            if (extension.excludes.isNotEmpty()) {
                pw.appendLine("-exclude")
            }

            extension.excludes.forEach { i ->
                pw.appendLine(i)
            }
        }
    }
}
