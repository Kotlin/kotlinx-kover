/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import kotlinx.kover.gradle.plugin.util.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import java.io.*

/*
Since the Kover and Android plug-ins can be in different class loaders (declared in different projects), the plug-ins are stored in a single instance in the loader of the project where the plug-in was used for the first time.
Because of this, Kover may not have direct access to the Android plugin classes, and variables and literals of this types cannot be declared .

To work around this limitation, working with objects is done through reflection, using a dynamic Gradle wrapper.
 */
internal class KotlinAndroidLocator(private val project: Project) : SetupLocator {
    companion object {
        fun isApplied(project: Project): Boolean {
            return project.plugins.hasPlugin("kotlin-android")
        }
    }

    override val kotlinPlugin = AppliedKotlinPlugin(KotlinPluginType.ANDROID)

    override fun locateAll(koverExtension: KoverProjectExtensionImpl): List<KoverSetup<*>> {
        val androidExtension = project.extensions.findByName("android")?.bean()
            ?: throw KoverCriticalException("Kover requires extension with name 'android' for project '${project.path}' since it is recognized as Kotlin/Android project")

        val kotlinExtension = project.extensions.findByName("kotlin")?.bean()
            ?: throw KoverCriticalException("Kover requires extension with name 'kotlin' for project '${project.path}' since it is recognized as Kotlin/Android project")


        val variants = if ("applicationVariants" in androidExtension) {
            androidExtension.propertyBeans("applicationVariants")
        } else {
            androidExtension.propertyBeans("libraryVariants")
        }

        return variants.map { variant ->
            val variantName = variant.property<String>("name")
            val build = project.provider {
                extractBuild(koverExtension, kotlinExtension, variantName)
            }

            val tests = project.tasks.withType<Test>().matching {
                // use only Android unit tests (local tests)
                it.hasSuperclass("AndroidUnitTest")
                        // skip all tests from instrumentation if Kover Plugin is disabled for the project
                        && !koverExtension.allTestsExcluded
                        // skip this test if it disabled by name
                        && it.name !in koverExtension.tests.tasksNames
                        // only tests of current application build variant
                        && it.bean().property<String>("variantName") == variant["unitTestVariant"].property<String>("name")
            }

            KoverSetup(build, tests, SetupId(variantName))
        }
    }

    private fun extractBuild(
        koverExtension: KoverProjectExtensionImpl,
        kotlinExtension: DynamicBean,
        variantName: String
    ): SetupLazyInfo {
        if (koverExtension.allTestsExcluded) {
            // If the Kover plugin is disabled, then it does not provide any directories and compilation tasks to its setup artifacts.
            return SetupLazyInfo()
        }

        val compilation = kotlinExtension["target"].propertyBeans("compilations").first {
            it.property<String>("name") == variantName
        }

        // examples of kotlinSourceSets: debug, main
        val sources = compilation.propertyBeans("allKotlinSourceSets").flatMap {
            it["kotlin"].propertyCollection<File>("srcDirs")
        }.toSet()

        val outputs = compilation["output"].property<ConfigurableFileCollection>("classesDirs").files.filterNot {
            // exclude java classes from report. Expected java class files are placed in directories like
            //   build/intermediates/javac/debug/classes
            koverExtension.sources.excludeJavaCode && it.parentFile.parentFile.name == "javac"
        }.toSet()

        val compileTasks = mutableListOf<Task>()
        compileTasks += compilation.property<Task>("compileKotlinTask")
        if (!koverExtension.sources.excludeJavaCode) {
            compilation.propertyOrNull<TaskProvider<Task>?>("compileJavaTaskProvider")?.orNull?.let { task -> compileTasks += task }
        }

        return SetupLazyInfo(sources, outputs, compileTasks)
    }

    override fun locateRegular(koverExtension: KoverProjectExtensionImpl): KoverSetup<*> {
        throw KoverCriticalException("Kover Android Locator does not support a single setup")
    }
}
