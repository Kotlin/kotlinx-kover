/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.adapters

import groovy.lang.*
import kotlinx.kover.adapters.api.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.internal.metaobject.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*

class KotlinMultiplatformPluginAdapter : CompilationPluginAdapter {

    override fun findDirs(project: Project): PluginDirs {
        return safe(project) {
            this.plugins.findPlugin("kotlin-multiplatform") ?: return@safe PluginDirs(emptyList(), emptyList())

            val extension = try {
                project.extensions.findByType(
                    KotlinMultiplatformExtension::class.java
                ) ?: return@safe PluginDirs(emptyList(), emptyList())
            } catch (e: ClassNotFoundException) {
                return findByReflection(project)
            } catch (e: NoClassDefFoundError) {
                return findByReflection(project)
            }

            val targets =
                extension.targets.filter { it.platformType == KotlinPlatformType.jvm || it.platformType == KotlinPlatformType.androidJvm }

            val compilations = targets.flatMap { it.compilations.filter { c -> c.name != "test" } }
            val sourceDirs =
                compilations.asSequence().flatMap { it.allKotlinSourceSets }.map { it.kotlin }.flatMap { it.srcDirs }
                    .toList()

            val outputDirs =
                compilations.asSequence().flatMap { it.output.classesDirs }.toList()

            return@safe PluginDirs(sourceDirs, outputDirs)
        }
    }

    /*
     * If Kotlin Multiplatform plugin if the plugin applied not in the same project where Kover is applied,
     * then its classes are in another class loader, and they are not available to kover.
     * Therefore, the only way to work with such an object is to use reflection.
     */
    @Suppress("UNCHECKED_CAST")
    fun findByReflection(project: Project): PluginDirs {
        val extension =
            project.extensions.findByName("kotlin")?.let { BeanDynamicObject(it) } ?: return PluginDirs(
                emptyList(),
                emptyList()
            )

        val targets = (extension.getProperty("targets") as NamedDomainObjectCollection<GroovyObject>).filter {
            val platformTypeName = (it.getProperty("platformType") as Named).name
            platformTypeName == "jvm" || platformTypeName == "androidJvm"
        }

        val compilations = targets.flatMap {
            (it.getProperty("compilations") as NamedDomainObjectCollection<Named>)
                .filter { c -> c.name != "test" }
        }.map { BeanDynamicObject(it) }

        val sourceDirs = compilations.asSequence()
            .flatMap { it.getProperty("allKotlinSourceSets") as Collection<*> }
            .map { BeanDynamicObject(it).getProperty("kotlin") as SourceDirectorySet }.flatMap { it.srcDirs }
            .toList()

        val outputDirs =
            compilations.asSequence().map { BeanDynamicObject(it.getProperty("output")) }
                .flatMap { it.getProperty("classesDirs") as FileCollection }.toList()

        return PluginDirs(sourceDirs, outputDirs)
    }

}
