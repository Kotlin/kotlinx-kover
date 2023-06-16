/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.writer

import kotlinx.kover.gradle.plugin.test.functional.framework.common.*
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import java.io.*

internal fun File.writeBuild(build: TestBuildConfig, slice: BuildSlice) {
    this.resolve("settings.${slice.scriptExtension}").writeSettings(build)
    build.projects.forEach { (path, conf) -> this.subproject(path).writeProject(conf, slice) }
}

private fun File.subproject(projectPath: String): File {
    val path = projectPath.removePrefix(":")
    return if (path.isEmpty()) {
        return this
    } else {
        val filepath = path.replace(':', File.separatorChar)
        this.resolve(filepath).also { it.mkdirs() }
    }
}

private fun File.writeProject(config: TestProjectConfigurator, slice: BuildSlice) {
    this.writeSources(config, slice)
    this.resolve("build.${slice.scriptExtension}").writeBuildScript(config, slice)
}

private fun File.writeSources(config: TestProjectConfigurator, slice: BuildSlice) {
    fun File.copyInto(targetFile: File) {
        listFiles()?.forEach { src ->
            val subTarget = targetFile.resolve(src.name)
            if (src.isDirectory) {
                subTarget.mkdirs()
                src.copyInto(subTarget)
            } else if (src.exists() && src.length() > 0) {
                src.copyTo(subTarget)
            }
        }
    }

    config.sourceTemplates.forEach { template ->
        File(SAMPLES_SOURCES_PATH, "$template/main").copyInto(File(this, slice.mainPath))
        File(SAMPLES_SOURCES_PATH, "$template/test").copyInto(File(this, slice.testPath))
    }
}


private const val SAMPLES_SOURCES_PATH = "src/functionalTest/templates/sources"
