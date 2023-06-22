/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.runner

import kotlinx.kover.gradle.plugin.test.functional.framework.common.isDebugEnabled
import kotlinx.kover.gradle.plugin.test.functional.framework.common.logInfo
import kotlinx.kover.gradle.plugin.test.functional.framework.common.uri
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.patchSettingsFile
import java.io.File
import java.nio.file.Files


internal fun createBuildSource(localMavenDir: String, koverVersion: String): BuildSource {
    return BuildSourceImpl(localMavenDir, koverVersion)
}

internal interface BuildSource {
    var overriddenKotlinVersion: String?

    fun copyFrom(rootProjectDir: File)

    fun from(rootProjectDir: File)

    fun generate(buildName: String, buildType: String): GradleBuild
}

internal interface GradleBuild {
    val targetDir: File

    fun run(args: List<String>, env: BuildEnv): BuildResult

    fun clear()
}

internal data class BuildEnv(
    val wrapperDir: File,
    val androidSdkDir: String? = null,
    val disableBuildCacheByDefault: Boolean = true
)

private class BuildSourceImpl(val localMavenDir: String, val koverVersion: String) : BuildSource {
    private var dir: File? = null

    private var copy: Boolean = false

    override var overriddenKotlinVersion: String? = null

    override fun copyFrom(rootProjectDir: File) {
        dir = rootProjectDir
        copy = true
    }

    override fun from(rootProjectDir: File) {
        dir = rootProjectDir
        copy = false
    }

    override fun generate(buildName: String, buildType: String): GradleBuild {
        val actualDir = dir ?: throw Exception("No source was specified for the build")
        val targetDir = if (copy) {
            val tmpDir = Files.createTempDirectory("${buildName.substringAfterLast('/')}-").toFile()
            actualDir.copyRecursively(tmpDir)
            tmpDir
        } else {
            actualDir
        }

        targetDir.patchSettingsFile(
            "$buildType '$buildName', project dir: ${targetDir.uri}",
            koverVersion, localMavenDir, overriddenKotlinVersion
        )

        return GradleBuildImpl(targetDir, copy, buildName, buildType)
    }
}


private class GradleBuildImpl(
    override val targetDir: File,
    private val delete: Boolean,
    private val buildName: String,
    private val buildType: String
) : GradleBuild {
    private var runCount = 0

    override fun run(args: List<String>, env: BuildEnv): BuildResult {
        logInfo("Starting build $buildType '$buildName' with commands '${args.joinToString(" ")}'")

        val gradleArgs: MutableList<String> = mutableListOf()
        gradleArgs += args

        if (env.disableBuildCacheByDefault) {
            if (args.none { it == "--build-cache" }) gradleArgs += "--no-build-cache"
        }

        if (isDebugEnabled) {
            gradleArgs += "-Dorg.gradle.debug=true"
            gradleArgs += "--no-daemon"
        }

        logInfo("Run Gradle commands $gradleArgs for project '${targetDir.canonicalPath}' with wrapper '${env.wrapperDir.canonicalPath}'")

        val envVars: MutableMap<String, String> = mutableMapOf()
        env.androidSdkDir?.also { dir -> envVars["ANDROID_HOME"] = dir }

        val result = targetDir.buildGradleByShell(runCount++, env.wrapperDir, gradleArgs, envVars)
        logInfo("Success build $buildType '$buildName'")
        return result
    }

    override fun clear() {
        if (delete) {
            logInfo("Deleting build for '$buildName' the directory ${targetDir.uri}")
            targetDir.deleteRecursively()
        }
    }
}
