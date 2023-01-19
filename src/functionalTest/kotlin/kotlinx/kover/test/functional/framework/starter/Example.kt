/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.starter

import kotlinx.kover.test.functional.framework.checker.*
import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.runner.*
import org.junit.jupiter.api.extension.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import java.io.*
import java.lang.reflect.*
import java.nio.file.*
import java.util.stream.*

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ArgumentsSource(ExampleTestArgumentsProvider::class)
@ParameterizedTest(name = "{0}")
@ExtendWith(ExampleInterceptor::class)
internal annotation class ExamplesTest(
    val includes: Array<String> = [],
    val excludes: Array<String> = [],
    val commands: Array<String> = ["build"]
)

private const val EXAMPLES_DIR = "examples"
private const val TMP_PREFIX = "kover-example-"

private class ExampleTestArgumentsProvider : ArgumentsProvider {

    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
        val annotation = (context.element.orElse(null)?.getAnnotation(ExamplesTest::class.java)
            ?: throw IllegalStateException("Test not marked by '${ExamplesTest::class.qualifiedName}' annotation"))

        val excludes = annotation.excludes.toList()
        val includes = annotation.includes.toList()
        val commands = annotation.commands.toList()

        val files = File(EXAMPLES_DIR).listFiles { it ->
            val name = it.name
            it.isDirectory
                    && !excludes.contains(name)
                    && (includes.isEmpty() || includes.contains(name))
        }?.toList() ?: emptyList()

        return files.stream().map { ExampleArgs(it, commands) }
    }
}

private class ExampleArgs(private val exampleDir: File, private val commands: List<String>) : Arguments {
    override fun get(): Array<Any> {
        val example = exampleDir.name

        val dir = Files.createTempDirectory("$TMP_PREFIX$example-").toFile()
        logInfo("Copy example '$example' into target directory ${dir.uri}")
        exampleDir.copyRecursively(dir)
        dir.patchSettingsFile("example '$example', project dir: ${dir.canonicalPath}")

        logInfo("Starting build example '$example' with commands '${commands.joinToString(" ")}'")
        val runResult = dir.runGradleBuild(commands)
        val checkerContext = dir.createCheckerContext(runResult)

        return arrayOf(NamedCheckerContext(checkerContext, example, dir))
    }
}

/**
 * Wrapper over Checker Context to show example name in JUnit and delete target dir
 */
private class NamedCheckerContext(origin: CheckerContext, val name: String, val targetDir: File) :
    CheckerContextWrapper(origin) {
    override fun toString(): String {
        return name
    }
}

private class ExampleInterceptor : InvocationInterceptor {
    override fun interceptTestTemplateMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        if (invocationContext.arguments.isEmpty()) {
            throw IllegalStateException(
                "Parameter with type '${CheckerContext::class.qualifiedName}' is expected for a test. " +
                        "Moreover, it can be a receiver, for example 'fun ${CheckerContext::class.simpleName}.myTest()'"
            )
        }
        val checkerContext = invocationContext.arguments[0] as NamedCheckerContext

        val dir = checkerContext.targetDir
        logInfo("Starting checking example '${checkerContext.name}'")
        checkerContext.check("example '${checkerContext.name}'\nProject dir: ${dir.uri}") {
            invocation.proceed()
        }

        // clear directory if where are no errors
        logInfo("Example '${checkerContext.name}' successfully checked, deleting the directory ${dir.uri}")
        dir.deleteRecursively()
    }
}


