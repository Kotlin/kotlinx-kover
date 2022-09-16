/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.starter

import kotlinx.kover.test.functional.framework.common.*
import kotlinx.kover.test.functional.framework.common.logInfo
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.*
import java.io.*
import java.lang.reflect.*
import java.nio.file.*


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Test
@Extensions(
    ExtendWith(SimpleTestParams::class),
    ExtendWith(SimpleTestInterceptor::class)
)
internal annotation class SimpleTest

private const val TMP_PREFIX = "kover-simple-"
private const val DIR_PARAM = "directory"

private class SimpleTestParams : ParameterResolver {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.index == 0 && File::class.java.isAssignableFrom(parameterContext.parameter.type)
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        val dir = Files.createTempDirectory(TMP_PREFIX).toFile()

        logInfo("Starting simple test with directory ${dir.uri}")

        // we don't have to check the index, because only one parameter is used
        val store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL)
        store.put(DIR_PARAM, dir)
        return dir
    }
}

private class SimpleTestInterceptor : InvocationInterceptor {
    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        val store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL)
        val dir = store.get(DIR_PARAM, File::class.java) ?: throw IllegalStateException(
            "Single parameter with type 'java.io.File' is expected for a test. " +
                    "Moreover, it can be a receiver, for example 'fun File.myTest()'"
        )

        try {
            invocation.proceed()
        } catch (e: Throwable) {
            throw AssertionError("${e.message}\nProject dir: ${dir.uri}", e)
        }
        logInfo("Test successfully, deleting directory ${dir.uri}")
        dir.deleteRecursively()
    }
}
