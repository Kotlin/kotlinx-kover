package kotlinx.kover.adapters

import kotlinx.kover.adapters.api.*
import org.gradle.api.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*

class KotlinMultiplatformPluginAdapter : CompilationPluginAdapter {

    override fun findDirs(project: Project): PluginDirs {
        return safe(project) {
            this.plugins.findPlugin("kotlin-multiplatform") ?: return@safe PluginDirs(emptyList(), emptyList())

            val extension = project.extensions.findByType(
                KotlinMultiplatformExtension::class.java
            ) ?: return@safe PluginDirs(emptyList(), emptyList())

            val targets =
                extension.targets.filter { it.platformType == KotlinPlatformType.jvm || it.platformType == KotlinPlatformType.androidJvm }

            val compilations = targets.flatMap { it.compilations.filter { c -> c.name != "test" } }
            val sourceDirs = compilations.asSequence().flatMap { it.allKotlinSourceSets }.map { it.kotlin }.flatMap { it.srcDirs }.toList()

            val outputDirs =
                compilations.asSequence().flatMap { it.output.classesDirs }.toList()

            return@safe PluginDirs(sourceDirs, outputDirs)
        }
    }

}
