/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import kotlinx.kover.gradle.plugin.util.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.testing.*
import org.gradle.kotlin.dsl.*
import java.io.*

/*
Since the Kover and Kotlin Multiplatform plug-ins can be in different class loaders (declared in different projects), the plug-ins are stored in a single instance in the loader of the project where the plug-in was used for the first time.
Because of this, Kover may not have direct access to the K/MPP plugin classes, and variables and literals of this types cannot be declared .

To work around this limitation, working with objects is done through reflection, using a dynamic Gradle wrapper.
 */
internal class KotlinMultiPlatformLocator(private val project: Project) : SetupLocator {
    companion object {
        fun isApplied(project: Project): Boolean {
            return project.plugins.hasPlugin("kotlin-multiplatform")
        }
    }

    override val kotlinPlugin = AppliedKotlinPlugin(KotlinPluginType.MULTIPLATFORM)

    override fun locateRegular(koverExtension: KoverProjectExtensionImpl): KoverSetup<*> {
        val kotlinExtension = project.extensions.findByName("kotlin")?.bean()
            ?: throw KoverCriticalException("Kover requires extension with name 'kotlin' for project '${project.path}' since it is recognized as Kotlin/Multiplatform project")

        val build = project.provider {
            extractBuildReflective(koverExtension, kotlinExtension)
        }

        val tests = project.tasks.withType<Test>().matching {
            it.hasSuperclass("KotlinJvmTest")
                    // skip all tests from instrumentation if Kover Plugin is disabled for the project
                    && !koverExtension.disabledForProject
                    // skip this test if it disabled by name
                    && it.name !in koverExtension.tests.tasksNames
                    // skip this test if it disabled by its JVM target name
                    && it.bean().property("targetName") !in koverExtension.tests.mppTargetNames
        }

        return KoverSetup(build, tests)
    }

    private fun extractBuildReflective(
        koverExtension: KoverProjectExtensionImpl,
        mppExtension: DynamicBean
    ): SetupLazyInfo {
        if (koverExtension.disabledForProject) {
            // If the Kover plugin is disabled, then it does not provide any directories and compilation tasks to its setup artifacts.
            return SetupLazyInfo()
        }

        val targets = mppExtension.propertyBeans("targets").filter { it["platformType"].property<String>("name") == "jvm" }

        val byTarget = koverExtension.sources.mpp.compilationsByTarget

        val compilations = targets.flatMap { it.propertyBeans("compilations") }.filter {
            val name = it.property<String>("name")
            val targetName = it["target"].property<String>("name")

            // always ignore test source set by default
            name != SourceSet.TEST_SOURCE_SET_NAME
                    // ignore compilation for all JVM targets
                    && name !in koverExtension.sources.mpp.compilationsForAllTargets
                    // ignore compilation for specified JVM target
                    && byTarget[targetName]?.contains(name) != true
                    // ignore all compilations fro specified JVM target
                    && targetName !in koverExtension.sources.mpp.allCompilationsInTarget
        }

        val sources = compilations.flatMap {
            it.propertyBeans("allKotlinSourceSets")
        }.flatMap {
            it["kotlin"].propertyCollection<File>("srcDirs")
        }.toSet()

        val outputs = compilations.flatMap {
            it["output"].property<ConfigurableFileCollection>("classesDirs").files
        }.filterNot {
            // exclude java classes from report. Expected java class files are placed in directories like
            //   build/classes/kotlin/foo/main
            koverExtension.sources.excludeJavaCode && it.parentFile.parentFile.name == "java"
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
