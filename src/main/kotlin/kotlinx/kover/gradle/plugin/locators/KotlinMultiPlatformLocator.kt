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

// TODO comments why using reflective locator, not static (no copy-paste,  easier to refactor)
internal class KotlinMultiPlatformLocator(private val project: Project) : SetupLocator {
    companion object {
        fun isApplied(project: Project): Boolean {
            return project.plugins.hasPlugin("kotlin-multiplatform")
        }
    }

    override val kotlinPlugin = AppliedKotlinPlugin(KotlinPluginType.MULTI_PLATFORM)

    override fun locate(koverExtension: KoverProjectExtensionImpl): List<KoverSetup<*>> {
        val kotlinExtension = project.extensions.findByName("kotlin")?.bean()
            ?: throw KoverCriticalException("Kover requires extension with name 'kotlin' for project '${project.path}' since it is recognized as Kotlin/Multi-Platform project")

        val build = project.provider {
            extractBuildReflective(koverExtension, kotlinExtension)
        }

        val tests = project.tasks.withType<Test>().matching {
            it.hasSuperclass("KotlinJvmTest")
                    // skip all tests from instrumentation if Kover Plugin is disabled for the project
                    && !koverExtension.isDisabled
                    // skip this test if it disabled by name
                    && it.name !in koverExtension.tests.tasksNames
                    // skip this test if it disabled by its JVM target name
                    && it.bean().property("targetName") !in koverExtension.tests.kmpTargetNames
        }

        return listOf(KoverSetup(build, tests))
    }

    private fun extractBuildReflective(
        koverExtension: KoverProjectExtensionImpl,
        kmpExtension: DynamicBean
    ): KoverSetupBuild {
        if (koverExtension.isDisabled) {
            // TODO
            return KoverSetupBuild()
        }

        val targets = kmpExtension.propertyBeans("targets").filter { it["platformType"].property<String>("name") == "jvm" }

        val byTarget = koverExtension.sources.kmpCompilationsByTarget

        val compilations = targets.flatMap { it.propertyBeans("compilations") }.filter {
            val name = it.property<String>("name")
            val targetName = it["target"].property<String>("name")

            // always ignore test source set by default
            name != SourceSet.TEST_SOURCE_SET_NAME
                    // ignore compilation for all JVM targets
                    && name !in koverExtension.sources.kmpCompilationsForAllTargets
                    // ignore compilation for specified JVM target
                    && byTarget[targetName]?.contains(name) != true
        }

        val sources = compilations.flatMap {
            // TODO only one source set for KMP
            it.propertyBeans("kotlinSourceSets").first()["kotlin"].propertyCollection<File>("srcDirs")
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
            if (!koverExtension.sources.excludeJavaCode && it.origin.hasSuperclass("KotlinWithJavaCompilation")) {
                tasks += it.property<TaskProvider<Task>>("compileJavaTaskProvider").get()
            }
            tasks
        }

        return KoverSetupBuild(sources, outputs, compileTasks)
    }

}

/*

    ORIGINAL
    ============

        override fun locateStatic(koverExtension: KoverProjectExtensionImpl): List<KoverSetup<*>> {
        val kotlinExtension = project.extensions.findByType<KotlinMultiplatformExtension>()
            ?: throw KoverCriticalException("Kover requires extension with type '${KotlinMultiplatformExtension::class.qualifiedName}' for project '${project.path}' since it is recognized as Kotlin/Multi-Platform project")

        val build = project.provider {
            extractBuild(koverExtension, kotlinExtension)
        }

        val tests = project.tasks.withType<KotlinJvmTest>().matching {
            // skip all tests from instrumentation if Kover Plugin is disabled for the project
            !koverExtension.isDisabled
                    // skip this test if it disabled by name
                    && it.name !in koverExtension.tests.tasksNames
                    // skip this test if it disabled by its JVM target name
                    && it.targetName !in koverExtension.tests.kmpTargetNames
        }

        return listOf(KoverSetup(build, tests))
    }

    private fun extractBuild(
        koverExtension: KoverProjectExtensionImpl,
        kmpExtension: KotlinMultiplatformExtension
    ): KoverSetupBuild {
        if (koverExtension.isDisabled) {
            // TODO
            return KoverSetupBuild()
        }

        val targets = kmpExtension.targets.filter { it.platformType == KotlinPlatformType.jvm }

        val byTarget = koverExtension.sources.kmpCompilationsByTarget

        val compilations = targets.flatMap { it.compilations }.filter {
            // always ignore test source set by default
            it.name != SourceSet.TEST_SOURCE_SET_NAME
                    // ignore compilation for all JVM targets
                    && it.name !in koverExtension.sources.kmpCompilationsForAllTargets
                    // ignore compilation for specified JVM target
                    && byTarget[it.target.name]?.contains(it.name) != true
        }

        val sources = compilations.flatMap {
            // TODO only one source set for KMP
            it.kotlinSourceSets.first().kotlin.srcDirs
        }.toSet()

        val outputs = compilations.flatMap {
            it.output.classesDirs.files
        }.filterNot {
            // exclude java classes from report. Expected java class files are placed in directories like
            //   build/classes/kotlin/foo/main
            koverExtension.sources.excludeJavaCode && it.parentFile.parentFile.name == "java"
        }.toSet()

        val compileTasks = compilations.flatMap {
            val tasks = mutableListOf<Task>()
            tasks += it.compileKotlinTask
            if (!koverExtension.sources.excludeJavaCode && it is KotlinWithJavaCompilation<*>) {
                tasks += it.compileJavaTaskProvider.get()
            }
            tasks
        }

        return KoverSetupBuild(sources, outputs, compileTasks)
    }
 */
