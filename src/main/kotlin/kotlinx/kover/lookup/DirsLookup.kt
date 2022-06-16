/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.lookup

import kotlinx.kover.api.*
import kotlinx.kover.lookup.adapters.*
import org.gradle.api.Project
import org.gradle.api.file.*
import java.io.File


open class ProjectDirectories(val sources: FileCollection, val outputs: FileCollection)

internal object DirsLookup {
    private val adapters: List<LookupAdapter> = listOf(
        OldJavaPluginAdapter(),
        AndroidPluginAdapter(),
        KotlinMultiplatformPluginAdapter(),
        KotlinAndroidPluginAdapter()
    )

    @Suppress("UNUSED_PARAMETER")
    fun lookup(project: Project, sourceSetFilters: KoverSourceSetFilters): ProjectDirectories {
        val srcDirs = HashMap<String, File>()
        val outDirs = HashMap<String, File>()

        adapters.forEach {
            val (src, out) = it.lookupSafe(project, sourceSetFilters)
            srcDirs += src.map { file -> file.canonicalPath to file }
            outDirs += out.map { file -> file.canonicalPath to file }
        }

        val src = srcDirs.values.filter { it.exists() && it.isDirectory }
        val out = outDirs.values.filter { it.exists() && it.isDirectory }

        return ProjectDirectories(project.files(src), project.files(out))
    }


}


internal abstract class LookupAdapter {
    protected abstract fun lookup(project: Project, sourceSetFilters: KoverSourceSetFilters): Dirs

    fun lookupSafe(project: Project, sourceSetFilters: KoverSourceSetFilters): Dirs {
        return try {
            lookup(project, sourceSetFilters)
        } catch (e: Throwable) {
            when (e) {
                is NoSuchMethodError, is NoSuchFieldError, is ClassNotFoundException, is NoClassDefFoundError -> {
                    project.logger.info("Problem occurred in Kover source set adapter", e)
                    Dirs()
                }
                else -> throw e
            }
        }
    }

    protected fun filterSourceSet(name: String, sourceSetFilters: KoverSourceSetFilters): Boolean {
        if (sourceSetFilters.excludes.contains(name)) {
            return false
        }
        if (sourceSetFilters.excludeTests && (name == "test" || name.endsWith("Test") || name.startsWith("test") || name.startsWith("androidTest"))) {
            return false
        }
        return true
    }

    data class Dirs(val sources: List<File> = emptyList(), val outputs: List<File> = emptyList())
}
