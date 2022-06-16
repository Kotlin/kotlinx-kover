/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.lookup.adapters

import com.android.build.gradle.*
import kotlinx.kover.api.*
import kotlinx.kover.lookup.*
import org.gradle.api.*

internal class AndroidPluginAdapter : LookupAdapter() {
    override fun lookup(project: Project, sourceSetFilters: KoverSourceSetFilter): Dirs {
        project.plugins.findPlugin("android") ?: return Dirs()

        val extension = project.extensions.findByType(BaseExtension::class.java) ?: return Dirs()

        val sourceDirs = extension.sourceSets.asSequence()
            .filter { filterSourceSet(it.name, sourceSetFilters) }
            .map { it.java }.toList()
            .flatMap { it.srcDirs }

        return Dirs(sourceDirs, emptyList())
    }
}
