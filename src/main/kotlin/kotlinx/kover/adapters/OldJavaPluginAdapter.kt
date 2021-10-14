package kotlinx.kover.adapters

import kotlinx.kover.adapters.api.*
import org.gradle.api.*
import org.gradle.api.tasks.*

class OldJavaPluginAdapter : CompilationPluginAdapter {

    override fun findDirs(project: Project): PluginDirs {
        return safe(project) {
            this.plugins.findPlugin("java") ?: return@safe PluginDirs(emptyList(), emptyList())

            val sourceSetContainer = project.extensions.findByType(
                SourceSetContainer::class.java
            ) ?: return@safe PluginDirs(emptyList(), emptyList())

            val sourceSets = sourceSetContainer.filter { it.name != SourceSet.TEST_SOURCE_SET_NAME }

            val sourceDirs = sourceSets.flatMap { it.allSource.srcDirs }
            val outputDirs = sourceSets.flatMap { it.output.classesDirs }

            return@safe PluginDirs(sourceDirs, outputDirs)
        }
    }

}
