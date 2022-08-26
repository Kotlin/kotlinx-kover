/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.lookup.adapters

import kotlinx.kover.api.*
import kotlinx.kover.lookup.LookupAdapter
import org.gradle.api.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.gradle.kotlin.dsl.*

/**
 * Adapter to get sources and outputs of Kotlin Android Gradle plugin.
 * Required to support kotlin sources for android.
 */
internal class KotlinAndroidPluginAdapter : LookupAdapter() {

    override fun lookup(project: Project, sourceSetFilters: KoverSourceSetFilter): Dirs {
        project.plugins.findPlugin("kotlin-android") ?: return Dirs()

        val extension = project.extensions.findByType<KotlinAndroidProjectExtension>() ?: return Dirs()

        val sourceDirs = extension.target.compilations
            .filter { !it.name.endsWith("Test") }
            .flatMap { it.allKotlinSourceSets }
            .map { it.kotlin }
            .flatMap { it.srcDirs }

        val outputDirs = extension.target.compilations
            .filter { !it.name.endsWith("Test") }
            .flatMap { it.output.classesDirs }

        return Dirs(sourceDirs, outputDirs)

    }

}
