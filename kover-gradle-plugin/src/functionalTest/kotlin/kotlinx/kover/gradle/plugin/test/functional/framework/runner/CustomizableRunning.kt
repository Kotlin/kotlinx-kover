/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.runner

import kotlinx.kover.gradle.plugin.test.functional.framework.common.*
import kotlinx.kover.gradle.plugin.test.functional.framework.common.androidSdkDirParam
import kotlinx.kover.gradle.plugin.test.functional.framework.common.koverVersionCurrent
import kotlinx.kover.gradle.plugin.test.functional.framework.common.defaultGradleWrapperDir
import kotlinx.kover.gradle.plugin.test.functional.framework.common.examplesDir
import kotlinx.kover.gradle.plugin.test.functional.framework.common.gradleWrapperVersionParam
import kotlinx.kover.gradle.plugin.test.functional.framework.common.localRepositoryPath
import kotlinx.kover.gradle.plugin.test.functional.framework.common.overriddenKotlinVersionParam
import kotlinx.kover.gradle.plugin.test.functional.framework.common.templateBuildsDir
import java.io.File
import java.nio.file.Files

internal fun buildFromTemplate(templateName: String): BuildSource {
    val source = createBuildSource(localRepositoryPath, koverVersionCurrent)
    source.overriddenKotlinVersion = overriddenKotlinVersionParam

    val dir = templateBuildsDir.resolve(templateName)
    if (!dir.exists()) {
        throw Exception("Template not found: '$templateName'")
    }
    source.copyFrom(dir)

    return source
}

internal fun buildFromExample(examplePath: String): BuildSource {
    val source = createBuildSource(localRepositoryPath, koverVersionCurrent)
    source.overriddenKotlinVersion = overriddenKotlinVersionParam

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
    val source = createBuildSource(localRepositoryPath, koverVersionCurrent)
    source.overriddenKotlinVersion = overriddenKotlinVersionParam
    source.from(dir)

    return source
}


internal fun GradleBuild.runWithParams(args: List<String>): BuildResult {
    val wrapperDir =
        if (gradleWrapperVersionParam == null) defaultGradleWrapperDir else getWrapper(gradleWrapperVersionParam)

    val buildEnv = BuildEnv(wrapperDir, androidSdkDirParam)

    return run(args, buildEnv)
}
internal fun GradleBuild.runWithParams(vararg args: String): BuildResult {
    val wrapperDir =
        if (gradleWrapperVersionParam == null) defaultGradleWrapperDir else getWrapper(gradleWrapperVersionParam)

    val buildEnv = BuildEnv(wrapperDir, androidSdkDirParam)

    return run(args.toList(), buildEnv)
}

private fun getWrapper(version: String): File {
    val wrapperDir = gradleWrappersRoot.resolve(version)
    if (!wrapperDir.exists()) throw Exception("Wrapper for Gradle version '$version' is not supported by functional tests")
    return wrapperDir
}
