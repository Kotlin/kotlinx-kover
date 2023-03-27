/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.starter

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.check
import kotlinx.kover.gradle.plugin.test.functional.framework.checker.createCheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.common.*
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runGradleBuild
import kotlinx.kover.gradle.plugin.test.functional.framework.writer.*
import org.junit.jupiter.api.extension.*
import java.io.*
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.nio.file.Files

private const val DIR_PARAM = "build-directory"
private const val CHECKER_PARAM = "checker-context"

internal class Foo(val name: String, val dir: File, val commands: List<String>)

internal abstract class DirectoryBasedGradleTest: BeforeTestExecutionCallback, InvocationInterceptor, ParameterResolver {
    protected abstract fun readAnnotationArgs(element: AnnotatedElement?): Foo

    protected abstract val testType: String

    // BeforeTestExecutionCallback
    override fun beforeTestExecution(context: ExtensionContext) {
        val args = readAnnotationArgs(context.element.orElse(null))
        val targetDir = Files.createTempDirectory("${args.name}-").toFile()

        if (!args.dir.exists()) {
            error("Could not find the $testType '${args.name}' with directory ${args.dir.uri}")
        }

        logInfo("Before building $testType '${args.name}' in target directory ${targetDir.uri}")

        args.dir.copyRecursively(targetDir)
        targetDir.patchSettingsFile("$testType '${args.name}', project dir: ${targetDir.uri}")

        logInfo("Starting build $testType '${args.name}' with commands '${args.commands.joinToString(" ")}'")
        val runResult = targetDir.runGradleBuild(args.commands)
        logInfo("Success build $testType '${args.name}'")
        val checkerContext = targetDir.createCheckerContext(runResult)

        val store = context.getStore(ExtensionContext.Namespace.GLOBAL)
        store.put(DIR_PARAM, targetDir)
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
        val dir = store.get(DIR_PARAM, File::class.java)
        val checker = store.get(CHECKER_PARAM, CheckerContext::class.java)

        logInfo("Before checking $testType '$templateName'")

        checker.check("$testType '$templateName'\nProject dir: ${dir.uri}") {
            invocation.proceed()
        }
        logInfo("Deleting the directory ${dir.uri}")
        dir.deleteRecursively()
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
internal fun File.patchSettingsFile(description: String) {
    val settingsFile = (listFiles()?.firstOrNull { it.name == "settings.gradle" || it.name == "settings.gradle.kts" }
        ?: throw Exception("No Gradle settings file in project ${this.canonicalPath}"))
    val language = if (settingsFile.name.endsWith(".kts")) ScriptLanguage.KOTLIN else ScriptLanguage.GROOVY

    val originLines = settingsFile.readLines()

    settingsFile.bufferedWriter().use { writer ->
        var firstStatement = true
        originLines.forEach { line ->

            if (firstStatement && line.isNotBlank()) {
                val isPluginManagement = line.trimStart().startsWith("pluginManagement")

                writer.appendLine("pluginManagement {")

                val pluginManagementWriter = FormattedWriter { l -> writer.append(l) }
                pluginManagementWriter.writePluginManagement(language)

                if (!isPluginManagement) {
                    writer.appendLine("}")
                }

                firstStatement = false
            } else {
                writer.appendLine(line)
            }

        }

    }
}


