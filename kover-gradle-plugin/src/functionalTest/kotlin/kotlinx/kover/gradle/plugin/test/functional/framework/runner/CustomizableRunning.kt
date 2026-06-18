/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.runner

import kotlinx.kover.gradle.plugin.test.functional.framework.common.*
import kotlinx.kover.gradle.plugin.test.functional.framework.common.androidSdkDir
import kotlinx.kover.gradle.plugin.test.functional.framework.common.koverVersionCurrent
import kotlinx.kover.gradle.plugin.test.functional.framework.common.defaultGradleWrapperDir
import kotlinx.kover.gradle.plugin.test.functional.framework.common.examplesDir
import kotlinx.kover.gradle.plugin.test.functional.framework.common.overriddenGradleVersion
import kotlinx.kover.gradle.plugin.test.functional.framework.common.snapshotRepositoriesPropertyValue
import kotlinx.kover.gradle.plugin.test.functional.framework.common.overriddenKotlinVersion
import kotlinx.kover.gradle.plugin.test.functional.framework.common.templateBuildsDir
import kotlinx.kover.gradle.plugin.util.SemVer
import org.gradle.kotlin.dsl.provideDelegate
import java.io.File
import java.nio.file.Files

internal fun buildFromTemplate(templateName: String): BuildSource {
    val source = createBuildSource(snapshotRepositoriesPropertyValue, koverVersionCurrent)
    source.overriddenKotlinVersion = overriddenKotlinVersion

    val dir = templateBuildsDir.resolve(templateName)
    if (!dir.exists()) {
        throw Exception("Template not found: '$templateName'")
    }
    source.copyFrom(dir)
    source.buildType = "template"
    source.buildName = templateName

    return source
}

internal fun buildFromExample(examplePath: String): BuildSource {
    val source = createBuildSource(snapshotRepositoriesPropertyValue, koverVersionCurrent)
    source.overriddenKotlinVersion = overriddenKotlinVersion

    val exampleDir = examplesDir.resolve(examplePath)
    if (!exampleDir.exists()) {
        throw Exception("Example not found: '$exampleDir'")
    }
    source.copyFrom(exampleDir)

    return source
}

internal fun generateBuild(generator: (File) -> Unit): BuildSource {
    val dir = Files.createTempDirectory("generated-build-").toFile()

    generator(dir)
    val source = createBuildSource(snapshotRepositoriesPropertyValue, koverVersionCurrent)
    source.overriddenKotlinVersion = overriddenKotlinVersion
    source.buildType = "generated"
    source.from(dir)

    return source
}


internal fun GradleBuild.runWithParams(vararg args: String, options: BuildOptions = BuildOptions()): BuildResult {
    return runWithParams(args.toList(), options)
}

internal fun GradleBuild.runWithParams(args: List<String>, options: BuildOptions = BuildOptions()): BuildResult {
    val gradleVersionString = options.gradleVersion ?: overriddenGradleVersion ?: defaultGradleVersion
    val gradleVersion = SemVer.ofVariableOrNull(gradleVersionString) ?: throw IllegalArgumentException("Can not parse Gradle version '$gradleVersionString'")

    val wrapperDir = if (gradleVersionString == defaultGradleVersion) {
        defaultGradleWrapperDir
    } else {
        getWrapper(gradleVersionString)
    }

    val androidSdkDir = options.androidSdkDir ?: androidSdkDir

    val buildEnv = BuildEnv(gradleVersion, wrapperDir, androidSdkDir)

    return run(args, buildEnv)
}

internal class BuildOptions(val gradleVersion: String? = null, val androidSdkDir: String? = null)

private fun getWrapper(gradleVersion: String): File {
    val wrapperDir = gradleWrappersRoot.resolve(gradleVersion)
    if (!wrapperDir.exists()) throw Exception("Wrapper for Gradle version '$gradleVersion' is not supported by functional tests")
    return wrapperDir
}
