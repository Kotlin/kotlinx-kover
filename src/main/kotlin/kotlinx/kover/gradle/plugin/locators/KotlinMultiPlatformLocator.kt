/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import kotlinx.kover.gradle.plugin.util.*
import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.testing.*
import org.gradle.kotlin.dsl.*

/*
Since the Kover and Kotlin Multiplatform plug-ins can be in different class loaders (declared in different projects), the plug-ins are stored in a single instance in the loader of the project where the plug-in was used for the first time.
Because of this, Kover may not have direct access to the K/MPP plugin classes, and variables and literals of this types cannot be declared .

To work around this limitation, working with objects is done through reflection, using a dynamic Gradle wrapper.
 */
internal class KotlinMultiPlatformLocator(private val project: Project) : CompilationKitLocator {
    companion object {
        fun isApplied(project: Project): Boolean {
            return project.plugins.hasPlugin("kotlin-multiplatform")
        }
    }

    override fun locate(koverExtension: KoverProjectExtensionImpl): ProjectCompilation {
        val kotlinExtension = project.extensions.findByName("kotlin")?.bean()
            ?: throw KoverCriticalException("Kover requires extension with name 'kotlin' for project '${project.path}' since it is recognized as Kotlin/Multiplatform project")

        val targets = kotlinExtension.propertyBeans("targets").filter {
            it["platformType"].property<String>("name") == "jvm"
        }

        val androidTarget = kotlinExtension.propertyBeans("targets").firstOrNull {
            it["platformType"].property<String>("name") == "androidJvm"
        }

        val androidCompilationKits = if (androidTarget != null) {
            val androidExtension = project.extensions.findByName("android")?.bean()
                ?: throw KoverCriticalException("Kover requires extension with name 'android' for project '${project.path}' since it is recognized as Kotlin/Android project")

            project.androidCompilationKits(androidExtension, koverExtension, androidTarget)
        } else {
            emptyList()
        }

        val jvmKits = targets.map {
            extractJvmKit(koverExtension, it)
        }

        return ProjectCompilation(
            AppliedKotlinPlugin(KotlinPluginType.MULTIPLATFORM),
            jvmKits,
            androidCompilationKits
        )
    }

    private fun extractJvmKit(
        koverExtension: KoverProjectExtensionImpl,
        target: DynamicBean
    ): JvmCompilationKit {
        val targetName = target.property<String>("targetName")

        // TODO check android tests are not triggered
        val tests = project.tasks.withType<Test>().matching {
            it.hasSuperclass("KotlinJvmTest")
                    // skip all tests from instrumentation if Kover Plugin is disabled for the project
                    && !koverExtension.disabled
                    // skip this test if it disabled by name
                    && it.name !in koverExtension.tests.tasksNames
                    // skip this test if it disabled by its JVM target name
                    && it.bean().property<String>("targetName") == targetName
        }

        val compilations = project.provider {
            target.extractJvmCompilations(koverExtension) {
                // exclude java classes from report. Expected java class files are placed in directories like
                //   build/classes/java/main
                it.parentFile.name == "java"
            }
        }

        return JvmCompilationKit(targetName, tests, compilations)
    }

}
