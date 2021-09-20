package kotlinx.kover.adapters

import kotlinx.kover.adapters.api.CompilationPluginAdapter
import kotlinx.kover.adapters.api.PluginDirs
import kotlinx.kover.adapters.api.safe
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class KotlinAndroidPluginAdapter : CompilationPluginAdapter {

    override fun findDirs(project: Project): PluginDirs {
        return safe(project) {
            this.plugins.findPlugin("kotlin-android") ?: return@safe PluginDirs(emptyList(), emptyList())

            val extension = project.extensions.findByType(KotlinAndroidProjectExtension::class.java) ?: return@safe PluginDirs(
                emptyList(),
                emptyList()
            )


            val sourceDirs = extension.target.compilations
                .filter { !it.name.endsWith("Test") }
                .flatMap { it.allKotlinSourceSets }
                .map { it.kotlin }
                .flatMap { it.srcDirs }

            val outputDirs = extension.target.compilations
                .filter { !it.name.endsWith("Test") }
                .flatMap { it.output.classesDirs }

            return@safe PluginDirs(sourceDirs, outputDirs)
        }
    }

}
