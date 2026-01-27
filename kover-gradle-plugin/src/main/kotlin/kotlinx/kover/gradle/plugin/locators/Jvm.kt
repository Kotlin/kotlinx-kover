/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.appliers.origin.LanguageCompilation
import kotlinx.kover.gradle.plugin.appliers.origin.CompilationDetails
import kotlinx.kover.gradle.plugin.util.DynamicBean
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.TaskProvider
import java.io.File

internal fun Iterable<DynamicBean>.jvmCompilations(
    isJavaOutput: (File) -> Boolean
): Map<String, CompilationDetails> {
    return associate { compilation ->
        val name = compilation.value<String>("name")
        name to extractJvmCompilation(compilation, isJavaOutput)
    }
}

private fun extractJvmCompilation(
    compilation: DynamicBean,
    isJavaOutput: (File) -> Boolean
): CompilationDetails {
    val sources = compilation.beanCollection("allKotlinSourceSets").flatMap<DynamicBean, File> {
        it["kotlin"].valueCollection("srcDirs")
    }.toSet()

    val kotlinOutputs = compilation["output"]
        .value<ConfigurableFileCollection>("classesDirs")
        .filter { file -> !isJavaOutput(file) }

    val javaOutputs = compilation["output"]
        .value<ConfigurableFileCollection>("classesDirs")
        .filter { file -> isJavaOutput(file) }

    val kotlinCompileTask = compilation.value<TaskProvider<Task>>("compileTaskProvider")
    val javaCompileTask = compilation.valueOrNull<TaskProvider<Task>>("compileJavaTaskProvider")

    val kotlin = LanguageCompilation(kotlinCompileTask.map { kotlinOutputs }, kotlinCompileTask)

    val java = if (javaCompileTask != null) {
        LanguageCompilation(javaCompileTask.map { javaOutputs }, javaCompileTask)
    } else {
        null
    }

    return CompilationDetails(sources, kotlin, java)
}
