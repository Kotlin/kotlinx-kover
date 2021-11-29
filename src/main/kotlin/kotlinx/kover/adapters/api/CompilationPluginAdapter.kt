/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.adapters.api

import org.gradle.api.*
import java.io.*

interface CompilationPluginAdapter {
    fun findDirs(project: Project): PluginDirs
}


data class PluginDirs(val sources: List<File>, val output: List<File>)

internal inline fun safe(project: Project, block: Project.() -> PluginDirs): PluginDirs {
    return try {
        project.block()
    } catch (e: Throwable) {
        when (e) {
            is NoSuchMethodError, is NoSuchFieldError, is ClassNotFoundException, is NoClassDefFoundError -> {
                project.logger.info("Problem occurred in Kover source set adapter", e)
                PluginDirs(emptyList(), emptyList())
            }
            else -> throw e
        }
    }
}
