/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.appliers.origin.AndroidVariantOrigin
import kotlinx.kover.gradle.plugin.appliers.origin.JvmVariantOrigin
import kotlinx.kover.gradle.plugin.appliers.origin.AllVariantOrigins
import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.util.*
import org.gradle.api.Project
import org.gradle.api.tasks.testing.*
import org.gradle.kotlin.dsl.*

/*
Since the Kover and Kotlin Multiplatform plug-ins can be in different class loaders (declared in different projects), the plug-ins are stored in a single instance in the loader of the project where the plug-in was used for the first time.
Because of this, Kover may not have direct access to the K/MPP plugin classes, and variables and literals of this types cannot be declared .

To work around this limitation, working with objects is done through reflection, using a dynamic Gradle wrapper.
 */

internal fun Project.locateKotlinMultiplatformVariants(): AllVariantOrigins {
    val kotlinExtension = getKotlinExtension()

    val jvm = locateJvmVariant(kotlinExtension)
    val androids = locateAndroidVariants(kotlinExtension)

    return AllVariantOrigins(jvm, androids)
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

private fun Project.locateJvmVariant(kotlinExtension: DynamicBean): JvmVariantOrigin? {
    // only one JVM target is allowed, so we can take the first one
    val jvmTarget = kotlinExtension.beanCollection("targets").firstOrNull {
        it["platformType"].value<String>("name") == "jvm"
    } ?: return null

    return extractJvmVariant(jvmTarget)
}


private fun Project.extractJvmVariant(target: DynamicBean): JvmVariantOrigin {
    val targetName = target.value<String>("targetName")

    val tests = tasks.withType<Test>().matching {
        it.hasSuperclass("KotlinJvmTest") && it.bean().value<String>("targetName") == targetName
    }

    val compilations = provider {
        target.beanCollection("compilations").jvmCompilations {
            // exclude java classes from report. Expected java class files are placed in directories like
            //   build/classes/java/main
            it.parentFile.name == "java"
        }
    }

    return JvmVariantOrigin(tests, compilations)
}
