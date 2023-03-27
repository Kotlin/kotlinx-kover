/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.CompilationUnit
import kotlinx.kover.gradle.plugin.util.DynamicBean
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.TaskProvider
import java.io.File

/**
 * Extract JVM compilation unit form Kotlin compilation ([this]).
 */
internal inline fun DynamicBean.asJvmCompilationUnit(
    excludeJava: Boolean,
    isJavaOutput: (File) -> Boolean
): CompilationUnit {
    val sources = propertyBeans("allKotlinSourceSets").flatMap {
        it["kotlin"].propertyCollection<File>("srcDirs")
    }.toSet()

    val outputs = get("output").property<ConfigurableFileCollection>("classesDirs").files.filterNot {
        excludeJava && isJavaOutput(it)
    }.toSet()

    val compileTasks = mutableListOf<Task>()
    compileTasks += property<Task>("compileKotlinTask")
    if (!excludeJava) {
        propertyOrNull<TaskProvider<Task>?>("compileJavaTaskProvider")?.orNull?.let { task -> compileTasks += task }
    }

    return CompilationUnit(sources, outputs, compileTasks)
}