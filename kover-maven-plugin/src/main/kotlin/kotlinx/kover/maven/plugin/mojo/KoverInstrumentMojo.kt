/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.mojo

import kotlinx.kover.maven.plugin.Constants
import kotlinx.kover.maven.plugin.mojo.abstracts.AbstractKoverMojo
import org.apache.maven.artifact.Artifact
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

/**
 * Add JVM agent argument to the JVM in which tests are running.
 */
@Mojo(name = "instrumentation", defaultPhase = LifecyclePhase.INITIALIZE, threadSafe = true)
class KoverInstrumentMojo : AbstractKoverMojo() {
    @Parameter(property = "plugin.artifactMap", required = true, readonly = true)
    private lateinit var pluginArtifactMap: Map<String, Artifact>

    /**
     * Property name to pass java agent argument to the JVM in which tests are running.
     *
     * `argLine` by default.
     */
    @Parameter(property = "kover.agentPropertyName", defaultValue = Constants.AGENT_ARG_PARAMETER)
    private lateinit var agentPropertyName: String

    @Parameter
    var uninstrumentedClasses: List<String>? = null

    @Parameter(defaultValue = "\${project.build.directory}/${Constants.BIN_REPORT_PATH}", readonly = true)
    lateinit var binaryReportFile: File

    override fun doExecute() {
        val propertyName = agentPropertyName
        val agentArg = buildAgentArgument()

        val oldValue = project.properties.getProperty(propertyName)
        val newValue = oldValue appendArg agentArg

        project.properties.setProperty(propertyName, newValue)
        log.info("Test property '$propertyName' set to $newValue")
    }

    private fun buildAgentArgument(): String {
        return "-javaagent:${getAgentJar().canonicalPath}=file:${buildAgentArgsFile().canonicalPath}"
    }

    private fun buildAgentArgsFile(): File {
        binaryReportFile.parentFile.mkdirs()

        val file = File(project.build.directory).resolve(Constants.AGENT_ARGUMENTS_PATH)
        file.parentFile.mkdirs()
        file.printWriter().use { writer ->
            writer.append("report.file=").appendLine(binaryReportFile.canonicalPath)
            uninstrumentedClasses?.forEach { e ->
                writer.append("exclude=").appendLine(e)
            }
        }

        return file
    }

    private fun getAgentJar(): File {
        return pluginArtifactMap[Constants.AGENT_ARTIFACT]?.file ?: throw MojoExecutionException("Artifact ${Constants.AGENT_ARTIFACT} not found")
    }

    private infix fun String?.appendArg(newArg: String): String {
        val escaped = newArg.toEscapeString()
        return if (this == null) {
            escaped
        } else {
            "$this $escaped"
        }

    }

    /**
     * Escaping the string so as not to pass special characters `\` and `"`, also quoted by `"` quotes to avoid passing spaces.
     */
    private fun String.toEscapeString(): String {
        val builder = StringBuilder(this.length + 2)
        builder.append('"')
        this.toCharArray().forEach { char ->
            if (char == '"' || char == '\\') {
                builder.append('\\')
            }
            builder.append(char)
        }
        builder.append('"')

        return builder.toString()
    }
}