/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.writer

import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.configurator.*
import java.io.*

internal fun File.writeBuild(build: TestBuildConfig, slice: BuildSlice) {
    this.sub("settings.${slice.scriptExtension}").writeSettings(build)
    build.projects.forEach { (path, conf) -> this.subproject(path).writeProject(conf, slice) }
}

private fun File.subproject(projectPath: String): File {
    val filepath = projectPath.replace(':', '/')
    return this.sub(filepath).also { it.mkdirs() }
}

private fun File.writeProject(config: TestProjectConfig, slice: BuildSlice) {
    this.writeSources(config, slice)
    this.sub("build.${slice.scriptExtension}").writeBuildScript(config, slice)
}



private fun File.writeSources(config: TestProjectConfig, slice: BuildSlice) {
    fun File.copyInto(targetFile: File) {
        listFiles()?.forEach { src ->
            val subTarget = File(targetFile, src.name)
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


internal fun File.writeSettings(build: TestBuildConfig) {
    this.printWriter().use {
        it.println("""rootProject.name = "kover-functional-test"""")
        it.println()
        build.projects.keys.forEach { path ->
            if (path != ":") {
                it.println("""include("$path")""")
            }
        }

        if (build.useLocalCache) {
            it.println(LOCAL_CACHE)
        }
    }
}

private const val LOCAL_CACHE = """
buildCache {
    local {
        directory = "${"$"}settingsDir/build-cache"
    }
}
"""
