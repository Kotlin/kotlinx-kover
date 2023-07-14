/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import kotlinx.kover.gradle.plugin.util.*
import org.gradle.api.tasks.testing.*
import org.gradle.kotlin.dsl.*

/*
Since the Kover and Kotlin Multiplatform plug-ins can be in different class loaders (declared in different projects), the plug-ins are stored in a single instance in the loader of the project where the plug-in was used for the first time.
Because of this, Kover may not have direct access to the K/MPP plugin classes, and variables and literals of this types cannot be declared .

To work around this limitation, working with objects is done through reflection, using a dynamic Gradle wrapper.
 */

internal fun LocatorContext.initKotlinMultiplatformPluginLocator() {
    listener.onApplyPlugin(KotlinPluginType.MULTIPLATFORM)

    project.pluginManager.withPlugin(ANDROID_APP_PLUGIN_ID) {
        project.afterAndroidPluginApplied(::processWithAndroidTarget)
    }
    project.pluginManager.withPlugin(ANDROID_LIB_PLUGIN_ID) {
        project.afterAndroidPluginApplied(::processWithAndroidTarget)
    }

    project.afterEvaluate {
        if (!hasAnyAndroidPlugin) {
            // In case the Android plugin is not applied, then we are only looking for JVM compilations
            processJvmTarget()
        }
    }
}

private fun LocatorContext.processWithAndroidTarget() {
    val kotlinExtension = project.getKotlinExtension()

    locateJvmCompilations(kotlinExtension)
    locateAndroidCompilations(kotlinExtension)

    listener.finalize()
}

private fun LocatorContext.processJvmTarget() {
    val kotlinExtension = project.getKotlinExtension()

    locateJvmCompilations(kotlinExtension)

    listener.finalize()
}

private fun LocatorContext.locateAndroidCompilations(kotlinExtension: DynamicBean) {
    // only one Android target is allowed, so we can take the first one
    val androidTarget = kotlinExtension.beanCollection("targets").firstOrNull {
        it["platformType"].value<String>("name") == "androidJvm"
    }

    if (androidTarget != null) {
        val androidExtension = project.extensions.findByName("android")?.bean()
            ?: throw KoverCriticalException("Kover requires extension with name 'android' for project '${project.path}' since it is recognized as Kotlin/Android project")

        val androidCompilations = project.androidCompilationKits(androidExtension, koverExtension, androidTarget)

        listener.android(androidCompilations)
    }
}

private fun LocatorContext.locateJvmCompilations(kotlinExtension: DynamicBean) {
    // only one JVM target is allowed, so we can take the first one
    val jvmTarget = kotlinExtension.beanCollection("targets").firstOrNull {
        it["platformType"].value<String>("name") == "jvm"
    }

    if (jvmTarget != null) {
        val jvmCompilations = extractJvmCompilations(koverExtension, jvmTarget)
        listener.jvm(jvmCompilations)
    }
}


private fun LocatorContext.extractJvmCompilations(
    koverExtension: KoverProjectExtensionImpl,
    target: DynamicBean
): JvmCompilationKit {
    val targetName = target.value<String>("targetName")

    // TODO check android tests are not triggered
    val tests = project.tasks.withType<Test>().matching {
        it.hasSuperclass("KotlinJvmTest")
                // skip all tests from instrumentation if Kover Plugin is disabled for the project
                && !koverExtension.disabled
                // skip this test if it disabled by name
                && it.name !in koverExtension.tests.tasksNames
                // skip this test if it disabled by its JVM target name
                && it.bean().value<String>("targetName") == targetName
    }

    val compilations = project.provider {
        target.extractJvmCompilations(koverExtension) {
            // exclude java classes from report. Expected java class files are placed in directories like
            //   build/classes/java/main
            it.parentFile.name == "java"
        }
    }

    return JvmCompilationKit(tests, compilations)
}
