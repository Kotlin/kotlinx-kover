/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.starter

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.check
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.createCheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.common.*
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.BuildSource
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.GradleBuild
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runWithParams
import kotlinx.kover.gradle.plugin.test.functional.framework.writer.*
import kotlinx.kover.gradle.plugin.util.SemVer
import org.junit.jupiter.api.extension.*
import java.io.*
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

private const val DIR_PARAM = "build-directory"
private const val CHECKER_PARAM = "checker-context"
private const val GRADLE_WITH_ASSIGN_OPERATOR_VERSION = "8.12"

internal class RunCommand(val buildSource: BuildSource, val gradleArgs: List<String>)

internal abstract class DirectoryBasedGradleTest : BeforeTestExecutionCallback, InvocationInterceptor,
    ParameterResolver {
    protected abstract fun readAnnotationArgs(element: AnnotatedElement?): RunCommand

    // BeforeTestExecutionCallback
    override fun beforeTestExecution(context: ExtensionContext) {
        val args = readAnnotationArgs(context.element.orElse(null))
        val build = args.buildSource.generate()
        logInfo("Before building ${args.buildSource.buildType} '${args.buildSource.buildName}' in target directory ${build.targetDir.uri}")

        val runResult = build.runWithParams(args.gradleArgs)
        val checkerContext = build.createCheckerContext(runResult)

        val store = context.getStore(ExtensionContext.Namespace.GLOBAL)
        store.put(DIR_PARAM, build)
        store.put(CHECKER_PARAM, checkerContext)
    }

    // InvocationInterceptor
    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        val annotationArgs = readAnnotationArgs(extensionContext.element.orElse(null))

        val store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL)
        val build = store.get(DIR_PARAM, GradleBuild::class.java)
        val checker = store.get(CHECKER_PARAM, CheckerContext::class.java)

        logInfo("Before checking ${annotationArgs.buildSource.buildType} '${annotationArgs.buildSource.buildName}'")

        checker.check("${annotationArgs.buildSource.buildType} '${annotationArgs.buildSource.buildName}'\nProject dir: ${build.targetDir.uri}") {
            invocation.proceed()
        }
        logInfo("Deleting the directory ${build.targetDir.uri}")
        build.clear()
    }

    // ParameterResolver
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.index == 0 && CheckerContext::class.java.isAssignableFrom(parameterContext.parameter.type)
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        // we don't have to check the index, because only one parameter is used
        val store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL)
        return store.get(CHECKER_PARAM, CheckerContext::class.java)
    }
}

internal val File.requirements: BuildRequirements
    get() {
        val file = resolve("requires")
        if (!(file.exists() && file.isFile)) return BuildRequirements()

        var minGradle: SemVer? = null
        var maxGradle: SemVer? = null

        file.readLines().forEach { line ->
            when {
                line.startsWith("MIN_GRADLE=") -> minGradle =
                    SemVer.ofVariableOrNull(line.substringAfter("MIN_GRADLE="))
                line.startsWith("MAX_GRADLE=") -> maxGradle =
                    SemVer.ofVariableOrNull(line.substringAfter("MAX_GRADLE="))

            }
        }

        return BuildRequirements(minGradle, maxGradle)
    }

internal data class BuildRequirements(val minGradle: SemVer? = null, val maxGradle: SemVer? = null)

internal val File.buildSrc: File?
    get() = listFiles()?.firstOrNull { it.isDirectory && it.name == "buildSrc" }

internal val File.settings: File
    get() = listFiles()?.firstOrNull { it.name == "settings.gradle" || it.name == "settings.gradle.kts" }
        ?: throw Exception("No Gradle settings file in project ${this.uri}")
internal val File.build: File
    get() = listFiles()?.firstOrNull { it.name == "build.gradle" || it.name == "build.gradle.kts" }
        ?: throw Exception("No Gradle build file in project ${this.uri}")



/**
 * Override Kover version and add local repository to find artifact for current build.
 */
@Suppress("UNUSED_PARAMETER")
internal fun File.patchSettingsFile(
    description: String,
    koverVersion: String,
    snapshotRepos: List<String>,
    overrideKotlinVersion: String?
) {
    val language = if (name.endsWith(".kts")) ScriptLanguage.KTS else ScriptLanguage.GROOVY

    val originLines = readLines()

    bufferedWriter().use { writer ->
        var firstStatement = true
        originLines.forEach { line ->

            if (firstStatement && line.isNotBlank()) {
                val isPluginManagement = line.trimStart().startsWith("pluginManagement")

                writer.appendLine("pluginManagement {")

                val pluginManagementWriter = FormattedWriter { l -> writer.append(l) }
                pluginManagementWriter.writePluginManagement(
                    language,
                    koverVersion,
                    snapshotRepos,
                    overrideKotlinVersion
                )

                if (!isPluginManagement) {
                    writer.appendLine("}")
                }

                firstStatement = false
            } else {
                writer.appendLine(line)
            }
        }

        if (originLines.isEmpty()) {
            val pluginManagementWriter = FormattedWriter { l -> writer.append(l) }
            pluginManagementWriter.call("pluginManagement") {
                pluginManagementWriter.writePluginManagement(
                    language,
                    koverVersion,
                    snapshotRepos,
                    overrideKotlinVersion
                )
            }
        }

        val pluginManagementWriter = FormattedWriter { l -> writer.append(l) }
        pluginManagementWriter.writeDependencyManagement(language, snapshotRepos)
    }
}

/**
 * Override Kover version
 */
internal fun File.patchKoverDependency(koverVersion: String) {
    val originLines = readLines()
    bufferedWriter().use { writer ->
        originLines.forEach { line ->
            val lineToWrite = if (line.contains("org.jetbrains.kotlinx:kover-gradle-plugin:TEST")) {
                line.replace("org.jetbrains.kotlinx:kover-gradle-plugin:TEST", "org.jetbrains.kotlinx:kover-gradle-plugin:$koverVersion")
            } else {
                line
            }
            writer.appendLine(lineToWrite)
        }
    }
}

internal fun File.addKoverBlocks(koverBlocks: MutableList<(ScriptLanguage, String) -> String>) {
    if (koverBlocks.isEmpty()) return

    val language = if (name.endsWith(".kts")) ScriptLanguage.KTS else ScriptLanguage.GROOVY

    val builder = StringBuilder()
    koverBlocks.forEach { block ->
        builder.appendLine()
        builder.append(block(language, GRADLE_WITH_ASSIGN_OPERATOR_VERSION))
        builder.appendLine()
    }
    appendText(builder.toString())
}
