/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.starter

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.test.functional.framework.common.*
import kotlinx.kover.gradle.plugin.test.functional.framework.configurator.*
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.*
import kotlinx.kover.gradle.plugin.test.functional.framework.writer.*
import org.junit.jupiter.api.extension.*
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.*
import java.lang.reflect.*
import java.util.stream.*

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ArgumentsSource(SlicedTestArgumentsProvider::class)
@ParameterizedTest(name = "{0}")
@ExtendWith(SlicedTestInterceptor::class)
internal annotation class SlicedGeneratedTest(
    val all: Boolean = false,
    val allLanguages: Boolean = false,
    val allTypes: Boolean = false,
    val allTools: Boolean = false
)

internal interface SlicedBuildConfigurator : BuildConfigurator {
    val slice: BuildSlice
}

private val ALL_LANGUAGES = listOf(ScriptLanguage.KTS, ScriptLanguage.GROOVY)
private val ALL_TOOLS = listOf(CoverageToolVendor.KOVER, CoverageToolVendor.JACOCO)
private val ALL_TYPES = listOf(KotlinPluginType.JVM, KotlinPluginType.MULTIPLATFORM)


private class SlicedTestInterceptor : InvocationInterceptor {
    override fun interceptTestTemplateMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        if (invocationContext.arguments.isEmpty()) {
            throw IllegalStateException(
                "Parameter with type '${SlicedBuildConfigurator::class.qualifiedName}' or '${BuildConfigurator::class.qualifiedName}' is expected for a test. " +
                        "Moreover, it can be a receiver, for example 'fun ${SlicedBuildConfigurator::class.simpleName}.myTest()'"
            )
        }

        val configurator = invocationContext.arguments[0]
        if (configurator !is SlicedBuildConfigurator) {
            throw IllegalArgumentException("The type of the first parameter should be '${SlicedBuildConfigurator::class.qualifiedName}', but the actual '${configurator::class}'")
        }
        val slice = configurator.slice

        logInfo("Starting configuration for slice ($slice)")
        invocation.proceed()
        logInfo("Starting writing build for slice ($slice)")
        val config = configurator.prepare()

        val buildSource = generateBuild { dir ->
            dir.writeBuild(config, slice)
            logInfo("Build was created for slice ($slice) in directory ${dir.uri}")
        }
        buildSource.buildType = "sliced generated"
        buildSource.buildName = slice.toString()
        val build = buildSource.generate()
        build.runAndCheck(config.steps)
        // clear directory if where are no errors
        build.clear()
    }
}


private class SlicedTestArgumentsProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
        val languages = mutableSetOf<ScriptLanguage>()
        val types = mutableSetOf<KotlinPluginType>()
        val tools = mutableSetOf<CoverageToolVendor?>()

        val annotation = context.element.get().annotations.filterIsInstance<SlicedGeneratedTest>().firstOrNull()
            ?: throw IllegalStateException("Expected annotation '${SlicedGeneratedTest::class.qualifiedName}' not applied")

        if (!annotation.all && !annotation.allLanguages && !annotation.allTypes && !annotation.allTools) {
            throw IllegalStateException("No slice is specified in '${SlicedGeneratedTest::class.qualifiedName}'")
        }

        if (annotation.all) {
            languages += ALL_LANGUAGES
            types += ALL_TYPES
            tools += ALL_TOOLS
        }
        if (annotation.allLanguages) {
            languages += ALL_LANGUAGES
        }
        if (annotation.allTypes) {
            types += ALL_TYPES
        }
        if (annotation.allTools) {
            tools += ALL_TOOLS
        }

        // filling default values
        if (languages.isEmpty()) languages += ScriptLanguage.KTS
        if (types.isEmpty()) types += KotlinPluginType.JVM
        if (tools.isEmpty()) tools += null

        val slices = mutableListOf<BuildSlice>()
        languages.forEach { language ->
            types.forEach { type ->
                tools.forEach { tool ->
                    slices += BuildSlice(language, type, tool)
                }
            }
        }

        return slices.stream().map {
            Arguments { arrayOf(SlicedBuilderConfiguratorWrapper(it)) }
        }
    }
}


private class SlicedBuilderConfiguratorWrapper(override val slice: BuildSlice) :
    BuilderConfiguratorWrapper(createConfigurator()), SlicedBuildConfigurator {

    override fun toString(): String {
        return slice.toString()
    }
}
