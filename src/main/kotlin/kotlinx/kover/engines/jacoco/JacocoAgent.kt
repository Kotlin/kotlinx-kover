/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.jacoco

import kotlinx.kover.api.*
import org.gradle.api.*
import org.gradle.api.artifacts.*
import java.io.*

internal fun Project.createJacocoAgent(koverExtension: KoverExtension): JacocoAgent {
    val jacocoConfig = createJacocoConfig(koverExtension)
    return JacocoAgent(jacocoConfig, this)
}

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

        return listOfNotNull(
            "destfile=${binary.canonicalPath}",
            "append=false", // Kover don't support parallel execution of one task
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
