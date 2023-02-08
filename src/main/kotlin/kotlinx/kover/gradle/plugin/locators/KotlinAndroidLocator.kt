/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import com.android.build.gradle.tasks.factory.*
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import kotlinx.kover.gradle.plugin.util.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.*
import java.io.*


internal class KotlinAndroidLocator(private val project: Project) : SetupLocator {
    companion object {
        fun isApplied(project: Project): Boolean {
            return project.plugins.hasPlugin("kotlin-android")
        }
    }

    override val kotlinPlugin = AppliedKotlinPlugin(KotlinPluginType.ANDROID)

    override fun locateMultiple(koverExtension: KoverProjectExtensionImpl): List<KoverSetup<*>> {
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

            val tests = project.tasks.withType<AndroidUnitTest>().matching {
                // skip all tests from instrumentation if Kover Plugin is disabled for the project
                !koverExtension.isDisabled
                        // skip this test if it disabled by name
                        && it.name !in koverExtension.tests.tasksNames
                        // only tests of current application build variant
                        && it.variantName == variant["unitTestVariant"].property<String>("name")
            }

            KoverSetup(build, tests, SetupId(variantName))
        }
    }

    private fun extractBuild(
        koverExtension: KoverProjectExtensionImpl,
        kotlinExtension: DynamicBean,
        variantName: String
    ): SetupLazyInfo {
        if (koverExtension.isDisabled) {
            // TODO
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
        val javaTaskProvider = compilation.property<TaskProvider<Task>?>("compileJavaTaskProvider")
        if (!koverExtension.sources.excludeJavaCode && javaTaskProvider != null) {
            compileTasks += javaTaskProvider.get()
        }

        return SetupLazyInfo(sources, outputs, compileTasks)
    }

    override fun locateSingle(koverExtension: KoverProjectExtensionImpl): KoverSetup<*> {
        throw KoverCriticalException("Kover Android Locator does not support a single setup")
    }
}

/*
    ORIGINAL
    ================

    override fun locateMultiple(koverExtension: KoverProjectExtensionImpl): List<KoverSetup<*>> {
        val androidExtension = project.extensions.findByType<BaseAppModuleExtension>()
            ?: throw KoverCriticalException("Kover requires extension with type '${BaseAppModuleExtension::class.qualifiedName}' for project '${project.path}' since it is recognized as Kotlin/Android project")

        val kotlinExtension = project.extensions.findByType<KotlinAndroidProjectExtension>()
            ?: throw KoverCriticalException("Kover requires extension with type '${KotlinAndroidProjectExtension::class.qualifiedName}' for project '${project.path}' since it is recognized as Kotlin/Android project")


        return androidExtension.applicationVariants.map { variant ->
            val build = project.provider {
                extractBuild(koverExtension, kotlinExtension, variant.name)
            }

            val tests = project.tasks.withType<AndroidUnitTest>().matching {
                // skip all tests from instrumentation if Kover Plugin is disabled for the project
                !koverExtension.isDisabled
                        // skip this test if it disabled by name
                        && it.name !in koverExtension.tests.tasksNames
                        // only tests of current application build variant
                        && it.variantName == variant.unitTestVariant.name
            }

            KoverSetup(build, tests, SetupId(variant.name))
        }
    }


    private fun extractBuild(
        koverExtension: KoverProjectExtensionImpl,
        kotlinExtension: KotlinAndroidProjectExtension,
        variantName: String
    ): SetupLazyInfo {
        if (koverExtension.isDisabled) {
            // TODO
            return SetupLazyInfo()
        }

        val compilation = kotlinExtension.target.compilations.findByName(variantName)!!

        // examples of kotlinSourceSets: debug, main
        val sources = compilation.kotlinSourceSets.flatMap { it.kotlin.srcDirs }.toSet()
        val outputs = compilation.output.classesDirs.files.filterNot {
            // exclude java classes from report. Expected java class files are placed in directories like
            //   build/intermediates/javac/debug/classes
            koverExtension.sources.excludeJavaCode && it.parentFile.parentFile.name == "javac"
        }.toSet()

        val compileTasks = mutableListOf<Task>()
        compileTasks += compilation.compileKotlinTask
        val javaTaskProvider = compilation.compileJavaTaskProvider
        if (!koverExtension.sources.excludeJavaCode && javaTaskProvider != null) {
            compileTasks += javaTaskProvider.get()
        }

        return SetupLazyInfo(sources, outputs, compileTasks)
    }
 */

