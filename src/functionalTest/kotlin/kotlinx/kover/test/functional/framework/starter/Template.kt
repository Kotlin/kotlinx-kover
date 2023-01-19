/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.starter

import kotlinx.kover.test.functional.framework.checker.*
import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.common.logInfo
import kotlinx.kover.test.functional.framework.runner.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.*
import org.opentest4j.*
import java.io.*
import java.lang.reflect.*
import java.nio.file.*

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Test
@Extensions(
    ExtendWith(Params::class),
    ExtendWith(TemplateTestBefore::class),
    ExtendWith(TemplateTestInterceptor::class)
)
internal annotation class TemplateTest(val templateName: String, val commands: Array<String>)

private const val TMP_PREFIX = "kover-template-"
private const val DIR_PARAM = "build-directory"
private const val CHECKER_PARAM = "checker-context"

private const val TEMPLATES_DIR = "src/functionalTest/templates/builds"

private class TemplateTestBefore : BeforeTestExecutionCallback {
    override fun beforeTestExecution(context: ExtensionContext) {
        val annotation = context.element.orElse(null)?.getAnnotation(TemplateTest::class.java)
            ?: throw IllegalStateException("Test not marked by '${TemplateTest::class.qualifiedName}' annotation")

        val templateName = annotation.templateName
        val commands = annotation.commands.toList()
        val dir = Files.createTempDirectory("$TMP_PREFIX$templateName-").toFile()

        logInfo("Before building template '$templateName' in target directory ${dir.uri}")

        val templateDir = File(TEMPLATES_DIR).resolve(templateName)
        if (!templateDir.exists() || !templateDir.isDirectory) {
            throw IllegalStateException("Template build '$templateName' not found")
        }
        templateDir.copyRecursively(dir)
        dir.patchSettingsFile("template '$templateName', project dir: ${dir.canonicalPath}")

        logInfo("Starting build with commands '${commands.joinToString(" ")}'")
        val runResult = dir.runGradleBuild(commands)
        val checkerContext = dir.createCheckerContext(runResult)

        val store = context.getStore(ExtensionContext.Namespace.GLOBAL)
        store.put(DIR_PARAM, dir)
        store.put(CHECKER_PARAM, checkerContext)
    }
}

private class TemplateTestInterceptor : InvocationInterceptor {
    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        val annotation = extensionContext.element.orElse(null)?.getAnnotation(TemplateTest::class.java)
            ?: throw IllegalStateException("Test not marked by '${TemplateTest::class.qualifiedName}' annotation")
        val templateName = annotation.templateName

        val store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL)
        val dir = store.get(DIR_PARAM, File::class.java)
        val checker = store.get(CHECKER_PARAM, CheckerContext::class.java)

        logInfo("Before checking")

        checker.check("template '$templateName'\nProject dir: ${dir.uri}") {
            invocation.proceed()
        }
        logInfo("Deleting the directory ${dir.uri}")
        dir.deleteRecursively()
    }
}

class Params : ParameterResolver {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.index == 0 && CheckerContext::class.java.isAssignableFrom(parameterContext.parameter.type)
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        // we don't have to check the index, because only one parameter is used
        val store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL)
        return store.get(CHECKER_PARAM, CheckerContext::class.java)
    }
}
