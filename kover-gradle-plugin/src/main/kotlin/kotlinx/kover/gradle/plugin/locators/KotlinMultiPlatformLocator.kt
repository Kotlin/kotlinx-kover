/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.appliers.origin.AndroidVariantOrigin
import kotlinx.kover.gradle.plugin.appliers.origin.JvmVariantOrigin
import kotlinx.kover.gradle.plugin.appliers.origin.AllVariantOrigins
import kotlinx.kover.gradle.plugin.appliers.origin.CompilationDetails
import kotlinx.kover.gradle.plugin.appliers.origin.LanguageCompilation
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.util.*
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.*
import org.gradle.kotlin.dsl.*
import java.io.File
import kotlin.collections.filter
import kotlin.collections.singleOrNull
import kotlin.collections.toSet

/*
Since the Kover and Kotlin Multiplatform plug-ins can be in different class loaders (declared in different projects), the plug-ins are stored in a single instance in the loader of the project where the plug-in was used for the first time.
Because of this, Kover may not have direct access to the K/MPP plugin classes, and variables and literals of this types cannot be declared .

To work around this limitation, working with objects is done through reflection, using a dynamic Gradle wrapper.
 */

internal fun Project.locateKotlinMultiplatformVariants(): AllVariantOrigins {
    val kotlinExtension = getKotlinExtension()

    val jvms = locateAllJvmVariants(kotlinExtension)
    val androids = locateAndroidVariants(kotlinExtension)

    return AllVariantOrigins(jvms, androids)
}

private fun Project.locateAndroidVariants(kotlinExtension: DynamicBean): List<AndroidVariantOrigin> {
    // only one Android target is allowed, so we can take the first one
    val androidTarget = kotlinExtension.beanCollection("targets").firstOrNull {
        it["platformType"].value<String>("name") == "androidJvm"
    } ?: return emptyList()

    val androidExtension = project.extensions.findByName("android")?.bean()
        ?: throw KoverCriticalException("Kover requires extension with name 'android' for project '${project.path}' since it is recognized as Kotlin/Android project")

    return project.androidCompilationKits(androidExtension, androidTarget)
}

private fun Project.locateAllJvmVariants(kotlinExtension: DynamicBean): List<JvmVariantOrigin> {
    val jvmTargets = kotlinExtension.beanCollection("targets").filter {
        it["platformType"].value<String>("name") == "jvm"
    }
    if (jvmTargets.isEmpty()) {
        return emptyList()
    }

    val result = mutableListOf<JvmVariantOrigin>()
    locateJvmVariant(jvmTargets)?.let { result += it }
    locateAndroidLibrary(jvmTargets)?.let { result += it }
    return result
}

private fun Project.locateJvmVariant(jvmTargets: List<DynamicBean>): JvmVariantOrigin? {
    val strictJvmTarget = jvmTargets.singleOrNull {
        !it.origin.hasSuperclass("KotlinMultiplatformAndroidLibraryTargetImpl")
    } ?: return null

    val targetName = strictJvmTarget.value<String>("targetName")

    val tests = tasks.withType<Test>().matching {
        it.hasSuperclass("KotlinJvmTest") && it.bean().value<String>("targetName") == targetName
    }

    val compilations: Provider<Map<String, CompilationDetails>> = provider {
        jvmTargets.extractPlainJvmVariant()
    }

    return JvmVariantOrigin(tests, compilations, targetName)
}

private fun Project.locateAndroidLibrary(jvmTargets: List<DynamicBean>): JvmVariantOrigin? {
    val androidLibrary = jvmTargets.singleOrNull {
        it.origin.hasSuperclass("KotlinMultiplatformAndroidLibraryTargetImpl")
    } ?: return null


    val targetName = androidLibrary.value<String>("targetName")
    val tests = tasks.withType<Test>().matching {
        it.hasSuperclass("AndroidUnitTest") && it.bean().value<String>("variantName") == "${targetName}HostTest"
    }

    val compilations: Provider<Map<String, CompilationDetails>> = provider {
        androidLibrary.extractKmpAndroidLibraryVariant()
    }
    return JvmVariantOrigin(tests, compilations, targetName)
}


private fun List<DynamicBean>.extractPlainJvmVariant(): Map<String, CompilationDetails> {
    return singleOrNull {
        it.origin.hasSuperclass("KotlinJvmTarget")
    }?.beanCollection("compilations")?.jvmCompilations {
        // exclude java classes from report. Expected java class files are placed in directories like
        //   build/classes/java/main
        it.parentFile.name == "java"
    } ?: emptyMap()
}


private fun DynamicBean.extractKmpAndroidLibraryVariant(): Map<String, CompilationDetails> {
    return beanCollection("compilations")
        .filter {
            // exclude test compilations
            val compilationName = it.value<String>("name")
            compilationName != "test" && !compilationName.endsWith("Test")
        }.associate { compilation ->
            val name = compilation.value<String>("name")
            val sources = compilation.beanCollection("allKotlinSourceSets").flatMap<DynamicBean, File> {
                it["kotlin"].valueCollection("srcDirs")
            }.toSet()

            val kotlinOutputs = compilation["output"].value<ConfigurableFileCollection>("classesDirs")
            val kotlinCompileTask = compilation.valueOrNull<TaskProvider<Task>?>("compileTaskProvider")?.orNull
            val kotlin = LanguageCompilation(kotlinOutputs, kotlinCompileTask)
            // at the moment, there is no way to get a task and directives for javac from the compilation
            val java = kotlin

            // since we place compilations from different targets in one map, we should separate it because the original names may overlap (like `main`)
            name to CompilationDetails(sources, kotlin, java)
        }
}
