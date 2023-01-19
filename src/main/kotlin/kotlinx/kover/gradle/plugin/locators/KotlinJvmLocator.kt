/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.KoverSetup
import kotlinx.kover.gradle.plugin.dsl.internal.*
import org.gradle.api.*
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*


internal class KotlinJvmLocator(private val project: Project) : SetupLocator {
    companion object {
        fun isApplied(project: Project): Boolean {
            return project.plugins.hasPlugin("kotlin")
        }
    }

    override val kotlinPlugin = AppliedKotlinPlugin(KotlinPluginType.JVM)

    override fun locate(koverExtension: KoverProjectExtensionImpl): List<KoverSetup<*>> {
        val kotlinExtension = project.extensions.findByType<KotlinJvmProjectExtension>()
            ?: throw KoverCriticalException("Kover requires extension with type '${KotlinJvmProjectExtension::class.qualifiedName}' for project '${project.path}' since it is recognized as Kotlin/JVM project")

        val build = project.provider {
            extractBuild(koverExtension, kotlinExtension)
        }

        val tests = project.tasks.withType<Test>().matching {
            // skip all tests from instrumentation if Kover Plugin is disabled for the project
            !koverExtension.isDisabled
                    // skip this test if it disabled by name
                    && it.name !in koverExtension.tests.tasksNames
        }

        return listOf(KoverSetup(build, tests))
    }

    private fun extractBuild(
        koverExtension: KoverProjectExtensionImpl,
        kotlinExtension: KotlinJvmProjectExtension
    ): KoverSetupBuild {
        if (koverExtension.isDisabled) {
            // TODO
            return KoverSetupBuild()
        }

        val compilations = kotlinExtension.target.compilations.filter {
            // always ignore test source set by default
            it.name != SourceSet.TEST_SOURCE_SET_NAME
                    // ignore specified JVM source sets
                    && it.name !in koverExtension.sources.jvmSourceSets
        }


        val sources = compilations.flatMap {
            // expected only one Kotlin Source Set for Kotlin/JVM
            it.kotlinSourceSets
        }.flatMap {
            it.kotlin.srcDirs
        }.toSet()

        val outputs = compilations.flatMap {
            it.output.classesDirs.files
        }.filterNot {
            // exclude java classes from report. Expected java class files are placed in directories like
            //   build/classes/java/main
            koverExtension.sources.excludeJavaCode && it.parentFile.name == "java"
        }.toSet()

        val compileTasks = compilations.flatMap {
            val tasks = mutableListOf<Task>()
            tasks += it.compileKotlinTask
            if (!koverExtension.sources.excludeJavaCode) {
                tasks += it.compileJavaTaskProvider.get()
            }
            tasks
        }

        return KoverSetupBuild(sources, outputs, compileTasks)
    }

}
