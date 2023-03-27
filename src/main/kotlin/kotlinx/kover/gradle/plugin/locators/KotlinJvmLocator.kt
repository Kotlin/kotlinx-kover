/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.internal.*
import kotlinx.kover.gradle.plugin.util.*
import org.gradle.api.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*

/*
Since the Kover and Kotlin JVM plug-ins can be in different class loaders (declared in different projects), the plug-ins are stored in a single instance in the loader of the project where the plug-in was used for the first time.
Because of this, Kover may not have direct access to the JVM plugin classes, and variables and literals of this types cannot be declared .

To work around this limitation, working with objects is done through reflection, using a dynamic Gradle wrapper.
 */
internal class KotlinJvmLocator(private val project: Project) : CompilationKitLocator {
    companion object {
        fun isApplied(project: Project): Boolean {
            return project.plugins.hasPlugin("kotlin")
        }
    }

    override fun locate(koverExtension: KoverProjectExtensionImpl): ProjectCompilation {
        val kotlinExtension = project.extensions.findByName("kotlin")?.bean()
            ?: throw KoverCriticalException("Kover requires extension with name 'kotlin' for project '${project.path}' since it is recognized as Kotlin/JVM project")

        val tests = project.tasks.withType<Test>().matching {
            // skip all tests from instrumentation if Kover Plugin is disabled for the project
            !koverExtension.disabledForProject
                    // skip this test if it disabled by name
                    && it.name !in koverExtension.tests.tasksNames
        }

        val compilations = project.provider {
            extractJvmCompilations(koverExtension, kotlinExtension)
        }

        return ProjectCompilation(
            AppliedKotlinPlugin(KotlinPluginType.JVM),
            listOf(JvmCompilationKit("K/JVM", tests, compilations))
        )
    }


    private fun extractJvmCompilations(
        koverExtension: KoverProjectExtensionImpl,
        kotlinExtension: DynamicBean
    ): Map<String, CompilationUnit> {
        if (koverExtension.disabledForProject) {
            // If the Kover plugin is disabled, then it does not provide any directories and compilation tasks to its artifacts.
            return emptyMap()
        }

        val compilations = kotlinExtension["target"].propertyBeans("compilations").filter {
            // always ignore test source set by default
            val name = it.property<String>("name")
            name != SourceSet.TEST_SOURCE_SET_NAME
                    // ignore specified JVM source sets
                    && name !in koverExtension.sources.jvm.sourceSets
        }

        return compilations.associate { compilation ->
            val name = compilation.property<String>("name")
            name to extractJvmCompilation(koverExtension, compilation)
        }
    }

    private fun extractJvmCompilation(
        koverExtension: KoverProjectExtensionImpl,
        compilation: DynamicBean
    ): CompilationUnit {
        return if (koverExtension.disabledForProject) {
            // If the Kover plugin is disabled, then it does not provide any directories and compilation tasks to its artifacts.
            CompilationUnit()
        } else {
            compilation.asJvmCompilationUnit(koverExtension.sources.excludeJavaCode) {
                // exclude java classes from report. Expected java class files are placed in directories like
                //   build/classes/java/main
                it.parentFile.name == "java"
            }
        }
    }
}
