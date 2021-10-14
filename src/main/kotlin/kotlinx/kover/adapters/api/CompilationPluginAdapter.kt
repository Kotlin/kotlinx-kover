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
    } catch (e: Exception) {
        when (e) {
            is NoSuchMethodError, is NoSuchFieldError, is ClassNotFoundException ->
                PluginDirs(emptyList(), emptyList())
            else ->
                throw e
        }
    }
}
