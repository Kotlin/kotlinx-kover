/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.jacoco

import kotlinx.kover.api.*
import kotlinx.kover.engines.commons.*
import kotlinx.kover.engines.commons.CoverageAgent
import org.gradle.api.*
import org.gradle.api.artifacts.*
import org.gradle.api.file.*
import java.io.*

internal fun Project.createJacocoAgent(koverExtension: KoverExtension): CoverageAgent {
    val jacocoConfig = createJacocoConfig(koverExtension)
    return JacocoAgent(jacocoConfig, this)
}

private class JacocoAgent(private val config: Configuration, private val project: Project): CoverageAgent {
    override val engine: CoverageEngine = CoverageEngine.JACOCO

    override val classpath: FileCollection = config

    override fun buildCommandLineArgs(task: Task, extension: KoverTaskExtension): MutableList<String> {
        return mutableListOf("-javaagent:${getJacocoJar().canonicalPath}=${agentArgs(extension)}")
    }

    private fun getJacocoJar(): File {
        val containedJarFile = config.fileCollection { it.name == "org.jacoco.agent" }.singleFile
        return project.zipTree(containedJarFile).filter { it.name == "jacocoagent.jar" }.singleFile
    }

    private fun agentArgs(extension: KoverTaskExtension): String {
        val binary = extension.binaryReportFile.get()
        binary.parentFile.mkdirs()

        return listOfNotNull(
            "destfile=${binary.canonicalPath}",
            "append=true",
            "inclnolocationclasses=false",
            "dumponexit=true",
            "output=file",
            "jmx=false",
            extension.includes.filterString("includes"),
            extension.excludes.filterString("excludes")
        ).joinToString(",")
    }
}

private fun List<String>.filterString(name: String): String? {
    if (isEmpty()) return null
    return name + "=" + joinToString(":")
}

private fun Project.createJacocoConfig(koverExtension: KoverExtension): Configuration {
    val config = project.configurations.create("JacocoKoverConfig")
    config.isVisible = false
    config.isTransitive = true
    config.description = "Kotlin Kover Plugin configuration for JaCoCo agent and reporter"

    config.defaultDependencies { dependencies ->
        dependencies.add(
            this.dependencies.create("org.jacoco:org.jacoco.agent:${koverExtension.jacocoEngineVersion.get()}")
        )
        dependencies.add(
            this.dependencies.create("org.jacoco:org.jacoco.ant:${koverExtension.jacocoEngineVersion.get()}")
        )
    }
    return config
}
