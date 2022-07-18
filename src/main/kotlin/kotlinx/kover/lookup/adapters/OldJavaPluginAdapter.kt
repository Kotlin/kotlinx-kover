/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.lookup.adapters

import kotlinx.kover.api.*
import kotlinx.kover.lookup.LookupAdapter
import org.gradle.api.*
import org.gradle.api.tasks.*

internal class OldJavaPluginAdapter : LookupAdapter() {

    override fun lookup(project: Project, sourceSetFilters: KoverSourceSetFilter): Dirs {
        project.plugins.findPlugin("java") ?: return Dirs()

        val sourceSetContainer = project.extensions.findByType(
            SourceSetContainer::class.java
        ) ?: return Dirs()

        val sourceSets = sourceSetContainer.filter { filterSourceSet(it.name, sourceSetFilters) }

        val sourceDirs = sourceSets.flatMap { it.allSource.srcDirs }
        val outputDirs = sourceSets.flatMap { it.output.classesDirs }

        return Dirs(sourceDirs, outputDirs)
    }

}
