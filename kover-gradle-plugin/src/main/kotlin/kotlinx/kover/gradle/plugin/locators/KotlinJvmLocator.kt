/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.appliers.origin.JvmVariantOrigin
import kotlinx.kover.gradle.plugin.appliers.origin.AllVariantOrigins
import kotlinx.kover.gradle.plugin.commons.JVM_VARIANT_NAME
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*

/*
Since the Kover and Kotlin JVM plug-ins can be in different class loaders (declared in different projects), the plug-ins are stored in a single instance in the loader of the project where the plug-in was used for the first time.
Because of this, Kover may not have direct access to the JVM plugin classes, and variables and literals of this types cannot be declared .

To work around this limitation, working with objects is done through reflection, using a dynamic Gradle wrapper.
 */

internal fun Project.locateKotlinJvmVariants(): AllVariantOrigins {
    val kotlinExtension = project.getKotlinExtension()
    val tests = tasks.withType<Test>()

    val compilations = provider {
        kotlinExtension["target"].beanCollection("compilations").jvmCompilations {
            // exclude java classes from report. Expected java class files are placed in directories like
            //   build/classes/java/main
            it.parentFile.name == "java"
        }
    }

    return AllVariantOrigins(listOf(JvmVariantOrigin(tests, compilations, JVM_VARIANT_NAME)), emptyList())
}
