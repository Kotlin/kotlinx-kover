/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.runner

import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import kotlinx.kover.gradle.plugin.test.functional.framework.common.BuildSlice
import kotlinx.kover.gradle.plugin.test.functional.framework.common.ScriptLanguage
import kotlinx.kover.gradle.plugin.test.functional.framework.common.isDebugEnabled
import kotlinx.kover.gradle.plugin.test.functional.framework.common.logInfo
import kotlinx.kover.gradle.plugin.test.functional.framework.common.uri
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.ProjectScope
import kotlinx.kover.gradle.plugin.test.functional.framework.mirroring.printGradleDsl
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.buildSrc
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.patchSettingsFile
import kotlinx.kover.gradle.plugin.util.SemVer
import org.opentest4j.TestAbortedException
import java.io.File
import java.nio.file.Files


internal fun createBuildSource(snapshotRepos: List<String>, koverVersion: String): BuildSource {
    return BuildSourceImpl(snapshotRepos, koverVersion)
}

internal interface BuildSource {
    var overriddenKotlinVersion: String?

    var buildName: String

    var buildType: String

    fun copyFrom(rootProjectDir: File)

    fun from(rootProjectDir: File)

    fun kover(config: KoverProjectExtension.(ProjectScope) -> Unit)

    fun generate(): GradleBuild
}

internal interface GradleBuild {
    val targetDir: File

    fun run(args: List<String>, env: BuildEnv): BuildResult

    fun clear()
}

internal data class BuildEnv(
    val gradleVersion: SemVer,
    val wrapperDir: File,
    val androidSdkDir: String? = null,
    val disableBuildCacheByDefault: Boolean = true
)

private class BuildSourceImpl(val snapshotRepos: List<String>, val koverVersion: String) : BuildSource {
    private var dir: File? = null

    private var copy: Boolean = false

    private val koverBlocks: MutableList<(ScriptLanguage, String) -> String> = mutableListOf()

    override var buildName: String = "default"

    override var buildType: String = "default"

    override var overriddenKotlinVersion: String? = null

    override fun copyFrom(rootProjectDir: File) {
        dir = rootProjectDir
        copy = true
    }

    override fun from(rootProjectDir: File) {
        dir = rootProjectDir
        copy = false
    }

    override fun kover(config: KoverProjectExtension.(ProjectScope) -> Unit) {
        koverBlocks += { language, gradle ->
            printGradleDsl<KoverProjectExtension, ProjectScope>(language, gradle, "kover", config)
        }
    }

    override fun generate(): GradleBuild {
        val actualDir = dir ?: throw Exception("No source was specified for the build")
        val targetDir = if (copy) {
            val tmpDir = Files.createTempDirectory("${buildName.substringAfterLast('/')}-").toFile()
            actualDir.copyRecursively(tmpDir)
            tmpDir
        } else {
            actualDir
        }

        targetDir.settings.patchSettingsFile(
            "$buildType '$buildName', project dir: ${targetDir.uri}",
            koverVersion, snapshotRepos, overriddenKotlinVersion
        )

        val buildSrcScript = targetDir.buildSrc?.build
        buildSrcScript?.patchKoverDependency(koverVersion)

        val buildScript = targetDir.build
        buildScript.addKoverBlocks(koverBlocks)

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
        val requirements = targetDir.requirements
        requirements.minGradle?.let { minVersion ->
            if (env.gradleVersion < minVersion) {
                throw TestAbortedException("Used Gradle version '${env.gradleVersion}' lower than minimal required '$minVersion'")
            }
        }
        requirements.maxGradle?.let { maxVersion ->
            if (env.gradleVersion >= maxVersion) {
                throw TestAbortedException("Used Gradle version '${env.gradleVersion}' higher or equals than maximal '$maxVersion'")
            }
        }

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

        gradleArgs += "--stacktrace"

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
