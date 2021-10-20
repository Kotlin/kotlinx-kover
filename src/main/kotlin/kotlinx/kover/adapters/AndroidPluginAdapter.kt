/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.adapters

import com.android.build.gradle.*
import kotlinx.kover.adapters.api.*
import org.gradle.api.*

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
