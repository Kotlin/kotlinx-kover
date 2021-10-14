package kotlinx.kover.engines.intellij

import kotlinx.kover.api.KoverTaskExtension
import org.gradle.api.artifacts.Configuration

internal class IntellijAgent(val config: Configuration) {
    private val trackingPerTest = false // a flag to enable tracking per test coverage
    private val calculateForUnloadedClasses = true // a flag to calculate coverage for unloaded classes
    private val appendToDataFile = false // a flag to use data file as initial coverage
    private val samplingMode = false //a flag to run coverage in sampling mode or in tracing mode otherwise
    private val generateSmapFile = true

    fun buildCommandLineArgs(extension: KoverTaskExtension): MutableList<String> {
        val jarFile = config.fileCollection { it.name == "intellij-coverage-agent" }.singleFile
        return mutableListOf(
            "-javaagent:${jarFile.canonicalPath}=${agentArgs(extension)}",
            "-Didea.coverage.check.inline.signatures=true",
            "-Didea.new.sampling.coverage=true",
            "-Didea.new.tracing.coverage=true"
        )
    }

    private fun agentArgs(extension: KoverTaskExtension): String {
        val includesString = extension.includes.joinToString(" ", " ")
        val excludesString = extension.excludes.let { if (it.isNotEmpty()) it.joinToString(" ", " -exclude ") else "" }

        val binary = extension.binaryReportFile.get()
        binary.parentFile.mkdirs()
        val binaryPath = binary.canonicalPath
        return "\"$binaryPath\" $trackingPerTest $calculateForUnloadedClasses $appendToDataFile $samplingMode $generateSmapFile \"$binaryPath.smap\"$includesString$excludesString"
    }
}
