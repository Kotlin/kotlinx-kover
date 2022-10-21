/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.starter

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.runner.*
import kotlinx.kover.test.functional.framework.writer.*
import kotlinx.kover.tools.commons.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.*
import java.lang.reflect.*
import java.nio.file.*

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Test
@Extensions(
    ExtendWith(SingleTestParams::class),
    ExtendWith(SingleTestInterceptor::class)
)
internal annotation class GeneratedTest(
    val language: ScriptLanguage = ScriptLanguage.KOTLIN,
    val type: KotlinPluginType = KotlinPluginType.JVM,
    val tool: CoverageToolVendor = CoverageToolVendor.KOVER,
    // since nullable types are not allowed in annotations, we store the attribute of an unspecified tool separately
    val defaultTool: Boolean = false
)

private const val CONFIGURATOR_PARAM = "configurator"
private const val TMP_PREFIX = "kover-generated-"

private class SingleTestParams : ParameterResolver {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.index == 0 && BuildConfigurator::class.java.isAssignableFrom(parameterContext.parameter.type)
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        val configurator = createConfigurator()

        // we don't have to check the index, because only one parameter is used
        val store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL)
        store.put(CONFIGURATOR_PARAM, configurator)
        return configurator
    }
}


private class SingleTestInterceptor : InvocationInterceptor {
    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        val store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL)
        val configurator = store.get(CONFIGURATOR_PARAM, BuildConfigurator::class.java) ?: throw IllegalStateException(
            "Single parameter with type '${BuildConfigurator::class.qualifiedName}' is expected for a test. " +
                    "Moreover, it can be a receiver, for example 'fun ${BuildConfigurator::class.simpleName}.myTest()'"
        )
        // fill configurator
        invocation.proceed()

        val annotation = (extensionContext.element.orElse(null)?.getAnnotation(GeneratedTest::class.java)
            ?: throw IllegalStateException("Test not marked by '${GeneratedTest::class.qualifiedName}' annotation"))

        val tool = if (annotation.defaultTool) null else annotation.tool
        val slice = BuildSlice(annotation.language, annotation.type, tool)

        val dir = Files.createTempDirectory(TMP_PREFIX).toFile()
        val config = configurator.prepare()
        dir.writeBuild(config, slice)
        logInfo("Build was created for slice ($slice) in directory ${dir.uri}")

        dir.runAndCheck(config.runs)
        // clear directory if where are no errors
        logInfo("Build successfully for slice ($slice), deleting the directory ${dir.uri}")
        dir.deleteRecursively()

        logInfo("Test successfully, deleting directory ${dir.uri}")
        dir.deleteRecursively()
    }
}
