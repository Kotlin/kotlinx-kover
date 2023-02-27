/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.KoverSetup
import kotlinx.kover.gradle.plugin.dsl.internal.*
import kotlinx.kover.gradle.plugin.util.*
import org.gradle.api.*
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.*
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import java.io.File

/*
Since the Kover and Kotlin JVM plug-ins can be in different class loaders (declared in different projects), the plug-ins are stored in a single instance in the loader of the project where the plug-in was used for the first time.
Because of this, Kover may not have direct access to the JVM plugin classes, and variables and literals of this types cannot be declared .

To work around this limitation, working with objects is done through reflection, using a dynamic Gradle wrapper.
 */
internal class KotlinJvmLocator(private val project: Project) : SetupLocator {
    companion object {
        fun isApplied(project: Project): Boolean {
            return project.plugins.hasPlugin("kotlin")
        }
    }

    override val kotlinPlugin = AppliedKotlinPlugin(KotlinPluginType.JVM)

    override fun locateRegular(koverExtension: KoverProjectExtensionImpl): KoverSetup<*> {
        val kotlinExtension = project.extensions.findByName("kotlin")?.bean()
            ?: throw KoverCriticalException("Kover requires extension with name 'kotlin' for project '${project.path}' since it is recognized as Kotlin/JVM project")

        val build = project.provider {
            extractBuild(koverExtension, kotlinExtension)
        }

        val tests = project.tasks.withType<Test>().matching {
            // skip all tests from instrumentation if Kover Plugin is disabled for the project
            !koverExtension.disabledForProject
                    // skip this test if it disabled by name
                    && it.name !in koverExtension.tests.tasksNames
        }

        return KoverSetup(build, tests)
    }

    private fun extractBuild(
        koverExtension: KoverProjectExtensionImpl,
        kotlinExtension: DynamicBean
    ): SetupLazyInfo {
        if (koverExtension.disabledForProject) {
            // If the Kover plugin is disabled, then it does not provide any directories and compilation tasks to its setup artifacts.
            return SetupLazyInfo()
        }

        val compilations = kotlinExtension["target"].propertyBeans("compilations").filter {
            // always ignore test source set by default
            val name = it.property<String>("name")
            name != SourceSet.TEST_SOURCE_SET_NAME
                    // ignore specified JVM source sets
                    && name !in koverExtension.sources.jvm.sourceSets
        }


        val sources = compilations.flatMap {
            // expected only one Kotlin Source Set for Kotlin/JVM
            it.propertyBeans("allKotlinSourceSets")
        }.flatMap {
            it["kotlin"].propertyCollection<File>("srcDirs")
        }.toSet()

        val outputs = compilations.flatMap {
            it["output"].property<ConfigurableFileCollection>("classesDirs").files
        }.filterNot {
            // exclude java classes from report. Expected java class files are placed in directories like
            //   build/classes/java/main
            koverExtension.sources.excludeJavaCode && it.parentFile.name == "java"
        }.toSet()

        val compileTasks = compilations.flatMap {
            val tasks = mutableListOf<Task>()
            tasks += it.property<Task>("compileKotlinTask")
            if (!koverExtension.sources.excludeJavaCode) {
                it.propertyOrNull<TaskProvider<Task>>("compileJavaTaskProvider")?.orNull?.let { task -> tasks += task }
            }
            tasks
        }

        return SetupLazyInfo(sources, outputs, compileTasks)
    }
}
