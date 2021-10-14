package kotlinx.kover.engines.jacoco

import kotlinx.kover.api.*
import org.gradle.api.*
import org.gradle.api.artifacts.*
import java.io.*

internal class JacocoAgent(val config: Configuration, private val project: Project) {
    fun buildCommandLineArgs(extension: KoverTaskExtension): MutableList<String> {
        return mutableListOf("-javaagent:${getJacocoJar().canonicalPath}=${agentArgs(extension)}")
    }

    private fun getJacocoJar(): File {
        val containedJarFile = config.fileCollection { it.name == "org.jacoco.agent" }.singleFile
        return project.zipTree(containedJarFile).filter { it.name == "jacocoagent.jar" }.singleFile
    }

    private fun agentArgs(extension: KoverTaskExtension): String {
        val binary = extension.binaryReportFile.get()
        binary.parentFile.mkdirs()

        return listOf(
            "destfile=${binary.canonicalPath}",
            "append=false", // Kover don't support parallel execution of one task
            "inclnolocationclasses=false",
            "dumponexit=true",
            "output=file",
            "jmx=false"
        ).joinToString(",")
    }
}
