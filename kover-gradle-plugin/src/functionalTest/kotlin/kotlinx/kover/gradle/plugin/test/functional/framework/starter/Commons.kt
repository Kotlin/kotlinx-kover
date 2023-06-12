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
import org.junit.jupiter.api.extension.*
import java.io.*
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

private const val DIR_PARAM = "build-directory"
private const val CHECKER_PARAM = "checker-context"

internal class RunCommand(val name: String, val buildSource: BuildSource, val gradleArgs: List<String>)

internal abstract class DirectoryBasedGradleTest : BeforeTestExecutionCallback, InvocationInterceptor,
    ParameterResolver {
    protected abstract fun readAnnotationArgs(element: AnnotatedElement?): RunCommand

    protected abstract val testType: String

    // BeforeTestExecutionCallback
    override fun beforeTestExecution(context: ExtensionContext) {
        val args = readAnnotationArgs(context.element.orElse(null))

        val build = args.buildSource.generate(args.name, testType)
        logInfo("Before building $testType '${args.name}' in target directory ${build.targetDir.uri}")

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
        val templateName = annotationArgs.name

        val store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL)
        val build = store.get(DIR_PARAM, GradleBuild::class.java)
        val checker = store.get(CHECKER_PARAM, CheckerContext::class.java)

        logInfo("Before checking $testType '$templateName'")

        checker.check("$testType '$templateName'\nProject dir: ${build.targetDir.uri}") {
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


/**
 * Override Kover version and add local repository to find artifact for current build.
 */
@Suppress("UNUSED_PARAMETER")
internal fun File.patchSettingsFile(
    description: String,
    koverVersion: String,
    localRepositoryPath: String,
    overrideKotlinVersion: String?
) {
    val settingsFile = (listFiles()?.firstOrNull { it.name == "settings.gradle" || it.name == "settings.gradle.kts" }
        ?: throw Exception("No Gradle settings file in project ${this.uri}"))
    val language = if (settingsFile.name.endsWith(".kts")) ScriptLanguage.KOTLIN else ScriptLanguage.GROOVY

    val originLines = settingsFile.readLines()

    settingsFile.bufferedWriter().use { writer ->
        var firstStatement = true
        originLines.forEach { line ->

            if (firstStatement && line.isNotBlank()) {
                val isPluginManagement = line.trimStart().startsWith("pluginManagement")

                writer.appendLine("pluginManagement {")

                val pluginManagementWriter = FormattedWriter { l -> writer.append(l) }
                pluginManagementWriter.writePluginManagement(
                    language,
                    koverVersion,
                    localRepositoryPath,
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
            writer.appendLine("pluginManagement {")
            val pluginManagementWriter = FormattedWriter { l -> writer.append(l) }
            pluginManagementWriter.writePluginManagement(
                language,
                koverVersion,
                localRepositoryPath,
                overrideKotlinVersion
            )
            writer.appendLine("}")
        }

    }
}


