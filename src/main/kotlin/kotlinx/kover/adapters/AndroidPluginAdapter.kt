package kotlinx.kover.adapters

import com.android.build.gradle.BaseExtension
import kotlinx.kover.adapters.api.CompilationPluginAdapter
import kotlinx.kover.adapters.api.PluginDirs
import kotlinx.kover.adapters.api.safe
import org.gradle.api.Project

class AndroidPluginAdapter : CompilationPluginAdapter {

    override fun findDirs(project: Project): PluginDirs {
        return safe(project) {
            this.plugins.findPlugin("android") ?: return@safe PluginDirs(emptyList(), emptyList())

            val extension = project.extensions.findByType(BaseExtension::class.java) ?: return@safe PluginDirs(
                emptyList(),
                emptyList()
            )

            val sourceDirs = extension.sourceSets.asSequence()
                .filter { !it.name.startsWith("test") && !it.name.startsWith("androidTest") }
                .map { it.java }.toList()
                .flatMap { it.srcDirs }



            return@safe PluginDirs(sourceDirs, emptyList())
        }
    }

}
